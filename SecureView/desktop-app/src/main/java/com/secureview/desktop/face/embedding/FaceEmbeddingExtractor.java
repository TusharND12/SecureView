package com.secureview.desktop.face.embedding;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Extracts face embeddings using a deep learning model.
 * Uses OpenCV DNN module with a pre-trained face recognition model.
 */
public class FaceEmbeddingExtractor {
    private static final Logger logger = LoggerFactory.getLogger(FaceEmbeddingExtractor.class);
    
    private Net faceNet = null;
    private String modelType = null; // "facenet", "openface", "sface", or null
    
    // FaceNet uses 512-dim embeddings and 160x160 input
    // Other models (OpenFace, SFace) use 128-dim and 96x96 input
    private int embeddingSize = 128;
    private Size inputSize = new Size(96, 96);
    
    public void initialize() throws Exception {
        logger.info("Initializing Face Embedding Extractor...");
        
        // Try to load a pre-trained face recognition model
        // Supported models: FaceNet, OpenFace, ArcFace, SFace (OpenCV)
        // Model formats: .onnx, .pb (TensorFlow), .t7/.net (Torch)
        
        // Prioritize FaceNet - check common model locations
        String[] possiblePaths = {
            System.getProperty("user.home") + "/.secureview/models/facenet.onnx",
            System.getProperty("user.home") + "/.secureview/models/facenet.pb",
            System.getProperty("user.home") + "/.secureview/models/sface.onnx",
            System.getProperty("user.home") + "/.secureview/models/face_recognition_sface_2021dec.onnx", // Original SFace filename
            System.getProperty("user.home") + "/.secureview/models/openface.t7",
            "models/facenet.onnx",
            "models/facenet.pb",
            "models/sface.onnx",
            "models/openface.t7"
        };
        
        boolean modelLoaded = false;
        for (String modelPath : possiblePaths) {
            File modelFile = new File(modelPath);
            if (modelFile.exists()) {
                try {
                    loadModel(modelPath);
                    logger.info("Face recognition model loaded successfully from: {}", modelPath);
                    modelLoaded = true;
                    break;
                } catch (Exception e) {
                    logger.warn("Failed to load model from: {}", modelPath, e);
                }
            }
        }
        
        if (!modelLoaded) {
            logger.warn("No face recognition model found. Using simplified embedding extraction.");
            logger.warn("For better accuracy, download FaceNet model:");
            logger.warn("  - FaceNet ONNX: Download from TensorFlow Hub and convert to ONNX");
            logger.warn("  - FaceNet TensorFlow: https://tfhub.dev/google/facenet/1");
            logger.warn("  - Alternative: SFace (OpenCV): https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface");
            logger.warn("Place the model file in: ~/.secureview/models/facenet.onnx or facenet.pb");
        } else {
            logger.info("Model loaded successfully. Type: {}, Embedding size: {}, Input size: {}x{}", 
                       modelType != null ? modelType : "unknown", 
                       embeddingSize, 
                       (int)inputSize.width, 
                       (int)inputSize.height);
        }
        
        logger.info("Face Embedding Extractor initialized");
    }
    
    /**
     * Extracts face embedding from a face image.
     * @param faceImage Pre-processed face image
     * @return Face embedding vector
     */
    public double[] extractEmbedding(Mat faceImage) {
        if (faceImage.empty()) {
            logger.warn("Empty face image provided");
            return null;
        }
        
        try {
            // Preprocess face image
            Mat processed = preprocessFace(faceImage);
            
            // Extract embedding
            double[] embedding;
            
            if (faceNet != null) {
                // Use DNN model if available
                embedding = extractWithDNN(processed);
                
                // If DNN extraction failed (e.g., OpenCV DNN not available), fallback
                if (embedding == null || embedding.length == 0) {
                    logger.warn("DNN embedding extraction failed or returned empty result. Falling back to simplified embedding.");
                    embedding = extractSimplified(processed);
                }
            } else {
                // Fallback to simplified feature extraction
                embedding = extractSimplified(processed);
            }
            
            processed.release();
            return embedding;
            
        } catch (Exception e) {
            logger.error("Error extracting face embedding", e);
            return null;
        }
    }
    
    /**
     * Preprocesses face image for embedding extraction.
     * FaceNet requires 160x160 input with specific preprocessing.
     */
    private Mat preprocessFace(Mat faceImage) {
        Mat resized = new Mat();
        Imgproc.resize(faceImage, resized, inputSize);
        
        // FaceNet and most models expect RGB/BGR color images, not grayscale
        // Keep color if available, convert to BGR if needed
        Mat processed = new Mat();
        if (resized.channels() == 1) {
            // Convert grayscale to BGR
            Imgproc.cvtColor(resized, processed, Imgproc.COLOR_GRAY2BGR);
            resized.release();
        } else if (resized.channels() == 4) {
            // Convert RGBA to BGR
            Imgproc.cvtColor(resized, processed, Imgproc.COLOR_BGRA2BGR);
            resized.release();
        } else {
            processed = resized;
        }
        
        // FaceNet expects pixel values normalized to [-1, 1] range
        // Other models typically use [0, 1]
        Mat normalized = new Mat();
        if ("facenet".equals(modelType)) {
            // FaceNet: normalize to [-1, 1] range
            processed.convertTo(normalized, CvType.CV_32F, 2.0 / 255.0, -1.0);
        } else {
            // Other models: normalize to [0, 1] range
            processed.convertTo(normalized, CvType.CV_32F, 1.0 / 255.0);
        }
        
        // Release intermediate matrices
        processed.release();
        
        return normalized;
    }
    
    /**
     * Extracts embedding using DNN model.
     * Supports FaceNet (512-dim) and other models (128-dim).
     */
    private double[] extractWithDNN(Mat processed) {
        // Create blob from image
        // FaceNet: scale=1.0, mean subtraction not needed (already normalized)
        // swapRB=false (already in BGR), crop=false
        Mat blob = Dnn.blobFromImage(processed, 1.0, inputSize, new Scalar(0, 0, 0), false, false);
        
        // Set input
        faceNet.setInput(blob);
        
        // Forward pass
        Mat output = faceNet.forward();
        
        // Ensure output contains data
        if (output == null || output.empty() || output.total() == 0) {
            logger.error("DNN forward pass returned empty output. Check OpenCV DNN installation.");
            blob.release();
            output.release();
            return null;
        }
        
        // Extract embedding - size depends on model type
        int actualSize = (int) output.total();
        if (actualSize != embeddingSize) {
            logger.debug("Model output size ({}) differs from expected ({}), using actual size", actualSize, embeddingSize);
            embeddingSize = actualSize;
        }
        
        double[] embedding = new double[embeddingSize];
        
        // Extract embedding from output
        double[] flatOutput = new double[(int) output.total()];
        output.get(0, 0, flatOutput);
        
        if (flatOutput.length == 0) {
            logger.error("DNN output array is empty. Falling back to simplified embedding.");
            blob.release();
            output.release();
            return null;
        }
        
        // Copy to embedding array (handle different output shapes)
        int copyLength = Math.min(embeddingSize, flatOutput.length);
        System.arraycopy(flatOutput, 0, embedding, 0, copyLength);
        
        // If output is smaller than expected, pad with zeros
        if (flatOutput.length < embeddingSize) {
            logger.warn("Model output size ({}) is smaller than expected ({}), padding with zeros", 
                       flatOutput.length, embeddingSize);
        }
        
        // Normalize embedding (L2 normalization)
        normalizeEmbedding(embedding);
        
        blob.release();
        output.release();
        
        return embedding;
    }
    
    /**
     * Simplified embedding extraction (fallback method).
     * Uses histogram-based features as a placeholder.
     */
    private double[] extractSimplified(Mat processed) {
        // This is a simplified approach. In production, use a proper DNN model.
        double[] embedding = new double[embeddingSize];
        
        // Extract histogram features
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt channels = new MatOfInt(0);
        
        Imgproc.calcHist(
            java.util.Arrays.asList(processed),
            channels,
            new Mat(),
            hist,
            histSize,
            ranges
        );
        
        // Convert histogram to embedding
        double[] histData = new double[(int) hist.total()];
        hist.get(0, 0, histData);
        
        // Pad or truncate to embedding size
        for (int i = 0; i < Math.min(embedding.length, histData.length); i++) {
            embedding[i] = histData[i];
        }
        
        // Normalize
        normalizeEmbedding(embedding);
        
        hist.release();
        
        return embedding;
    }
    
    /**
     * Normalizes embedding vector to unit length.
     */
    private void normalizeEmbedding(double[] embedding) {
        double norm = 0.0;
        for (double value : embedding) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        
        if (norm > 0.0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= norm;
            }
        }
    }
    
    /**
     * Loads a pre-trained face recognition model.
     * Automatically detects model type (FaceNet, OpenFace, SFace) and configures accordingly.
     * 
     * FaceNet: 160x160 input, 512-dim embeddings
     * OpenFace/SFace: 96x96 input, 128-dim embeddings
     */
    public void loadModel(String modelPath) throws Exception {
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new Exception("Model file not found: " + modelPath);
        }
        
        // Detect model type from filename
        String lowerPath = modelPath.toLowerCase();
        if (lowerPath.contains("facenet")) {
            modelType = "facenet";
            embeddingSize = 512;
            inputSize = new Size(160, 160);
            logger.info("Detected FaceNet model - using 160x160 input, 512-dim embeddings");
        } else if (lowerPath.contains("openface") || lowerPath.contains("sface")) {
            modelType = lowerPath.contains("openface") ? "openface" : "sface";
            embeddingSize = 128;
            inputSize = new Size(96, 96);
            logger.info("Detected {} model - using 96x96 input, 128-dim embeddings", modelType);
        } else {
            // Default to OpenFace/SFace settings
            modelType = "unknown";
            embeddingSize = 128;
            inputSize = new Size(96, 96);
            logger.warn("Unknown model type, defaulting to 96x96 input, 128-dim embeddings");
        }
        
        // Determine model format and load accordingly
        if (modelPath.endsWith(".onnx")) {
            faceNet = Dnn.readNetFromONNX(modelPath);
        } else if (modelPath.endsWith(".pb")) {
            faceNet = Dnn.readNetFromTensorflow(modelPath);
        } else if (modelPath.endsWith(".t7") || modelPath.endsWith(".net")) {
            faceNet = Dnn.readNetFromTorch(modelPath);
        } else {
            throw new Exception("Unsupported model format: " + modelPath);
        }
        
        logger.info("Face recognition model loaded from: {}", modelPath);
        logger.info("Model configuration: {}x{} input, {}-dim embeddings", 
                    (int)inputSize.width, (int)inputSize.height, embeddingSize);
    }
    
    /**
     * Gets the current embedding size (128 for OpenFace/SFace, 512 for FaceNet).
     */
    public int getEmbeddingSize() {
        return embeddingSize;
    }
    
    /**
     * Gets the current model type.
     */
    public String getModelType() {
        return modelType != null ? modelType : "none";
    }
}

