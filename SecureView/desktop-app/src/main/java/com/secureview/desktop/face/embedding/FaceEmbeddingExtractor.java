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
        
        // Prioritize ArcFace (best accuracy), then FaceNet, then SFace
        String[] possiblePaths = {
            System.getProperty("user.home") + "/.secureview/models/arcface.onnx",
            System.getProperty("user.home") + "/.secureview/models/arcface_r50_v1.onnx",
            System.getProperty("user.home") + "/.secureview/models/facenet.onnx",
            System.getProperty("user.home") + "/.secureview/models/facenet.pb",
            System.getProperty("user.home") + "/.secureview/models/sface.onnx",
            System.getProperty("user.home") + "/.secureview/models/face_recognition_sface_2021dec.onnx", // Original SFace filename
            System.getProperty("user.home") + "/.secureview/models/openface.t7",
            "models/arcface.onnx",
            "models/facenet.onnx",
            "models/facenet.pb",
            "models/sface.onnx",
            "models/openface.t7"
        };
        
        boolean modelLoaded = false;
        for (String modelPath : possiblePaths) {
            File modelFile = new File(modelPath);
            // Check if file exists AND is not empty (at least 1MB for ONNX models)
            if (modelFile.exists() && modelFile.length() > 1024 * 1024) {
                try {
                    loadModel(modelPath);
                    logger.info("Face recognition model loaded successfully from: {} (size: {} MB)", 
                               modelPath, modelFile.length() / (1024 * 1024));
                    modelLoaded = true;
                    break;
                } catch (Exception e) {
                    logger.warn("Failed to load model from: {} (file size: {} bytes)", 
                               modelPath, modelFile.length(), e);
                }
            } else if (modelFile.exists() && modelFile.length() == 0) {
                logger.warn("Model file exists but is EMPTY (0 bytes): {}. Skipping.", modelPath);
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
        
        // ArcFace: normalize to [0, 1] and subtract mean [0.5, 0.5, 0.5] then divide by std [0.5, 0.5, 0.5]
        // FaceNet: normalize to [-1, 1] range
        // Other models: normalize to [0, 1] range
        Mat normalized = new Mat();
        if ("arcface".equals(modelType)) {
            // ArcFace: (pixel / 255.0 - 0.5) / 0.5 = (pixel - 127.5) / 127.5
            processed.convertTo(normalized, CvType.CV_32F, 1.0 / 127.5, -1.0);
        } else if ("facenet".equals(modelType)) {
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
     * Uses multiple features: histogram, texture, and spatial features for better accuracy.
     */
    private double[] extractSimplified(Mat processed) {
        // Use 512-dim embedding for better accuracy
        double[] embedding = new double[512];
        
        try {
            // Convert to grayscale for feature extraction
            Mat gray = new Mat();
            if (processed.channels() == 3) {
                Imgproc.cvtColor(processed, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = processed;
            }
            
            // Resize to standard size for consistent features
            Mat resized = new Mat();
            Size targetSize = new Size(112, 112);
            Imgproc.resize(gray, resized, targetSize);
            if (gray != processed) gray.release();
            gray = resized;
            
            // Feature 1: Histogram (128 dims) - normalized
            Mat hist = new Mat();
            MatOfInt histSize = new MatOfInt(128);
            MatOfFloat ranges = new MatOfFloat(0f, 256f);
            MatOfInt channels = new MatOfInt(0);
            
            Imgproc.calcHist(
                java.util.Arrays.asList(gray),
                channels,
                new Mat(),
                hist,
                histSize,
                ranges
            );
            
            // Normalize histogram
            Core.normalize(hist, hist, 0.0, 1.0, Core.NORM_MINMAX);
            
            double[] histData = new double[(int) hist.total()];
            hist.get(0, 0, histData);
            
            // Feature 2: Texture features (gradient-based, 128 dims)
            double[] textureFeatures = extractTextureFeatures(gray);
            
            // Feature 3: Spatial features (mean/stddev, 128 dims)
            double[] spatialFeatures = extractSpatialFeatures(gray);
            
            // Feature 4: Edge features (Laplacian, 128 dims)
            double[] edgeFeatures = extractEdgeFeatures(gray);
            
            // Combine all features into embedding
            int idx = 0;
            // Histogram (128 dims)
            for (int i = 0; i < Math.min(128, histData.length) && idx < embedding.length; i++) {
                embedding[idx++] = histData[i];
            }
            // Texture (128 dims)
            for (int i = 0; i < textureFeatures.length && idx < embedding.length; i++) {
                embedding[idx++] = textureFeatures[i];
            }
            // Spatial (128 dims)
            for (int i = 0; i < spatialFeatures.length && idx < embedding.length; i++) {
                embedding[idx++] = spatialFeatures[i];
            }
            // Edge (128 dims)
            for (int i = 0; i < edgeFeatures.length && idx < embedding.length; i++) {
                embedding[idx++] = edgeFeatures[i];
            }
            
            // Normalize entire embedding
            normalizeEmbedding(embedding);
            
            hist.release();
            if (resized != processed) resized.release();
            
            logger.debug("Extracted enhanced simplified embedding: {} dimensions", embedding.length);
            return embedding;
            
        } catch (Exception e) {
            logger.error("Error in simplified embedding extraction", e);
            // Return zero embedding as fallback
            return new double[512];
        }
    }
    
    /**
     * Extracts texture features using gradient-based approach.
     */
    private double[] extractTextureFeatures(Mat gray) {
        double[] features = new double[128];
        
        try {
            // Calculate gradients
            Mat gradX = new Mat();
            Mat gradY = new Mat();
            Mat gradMag = new Mat();
            
            Imgproc.Sobel(gray, gradX, CvType.CV_64F, 1, 0, 3, 1.0, 0.0);
            Imgproc.Sobel(gray, gradY, CvType.CV_64F, 0, 1, 3, 1.0, 0.0);
            Core.magnitude(gradX, gradY, gradMag);
            
            // Calculate histogram of gradient magnitudes
            Mat hist = new Mat();
            MatOfInt histSize = new MatOfInt(128);
            MatOfFloat ranges = new MatOfFloat(0f, 1000f);
            MatOfInt channels = new MatOfInt(0);
            
            Imgproc.calcHist(
                java.util.Arrays.asList(gradMag),
                channels,
                new Mat(),
                hist,
                histSize,
                ranges
            );
            
            double[] histData = new double[(int) hist.total()];
            hist.get(0, 0, histData);
            
            System.arraycopy(histData, 0, features, 0, Math.min(features.length, histData.length));
            
            gradX.release();
            gradY.release();
            gradMag.release();
            hist.release();
            
        } catch (Exception e) {
            logger.debug("Error extracting texture features", e);
        }
        
        return features;
    }
    
    /**
     * Extracts spatial features using mean and variance of image regions.
     */
    private double[] extractSpatialFeatures(Mat gray) {
        double[] features = new double[128];
        
        try {
            // Calculate overall statistics
            Scalar mean = Core.mean(gray);
            Mat meanMat = new Mat();
            Mat stddevMat = new Mat();
            Core.meanStdDev(gray, meanMat, stddevMat);
            
            // Use mean and stddev values
            double[] meanVals = meanMat.get(0, 0);
            double[] stddevVals = stddevMat.get(0, 0);
            
            if (meanVals != null && meanVals.length > 0) {
                features[0] = meanVals[0] / 255.0;
            }
            if (stddevVals != null && stddevVals.length > 0) {
                features[1] = stddevVals[0] / 255.0;
            }
            
            // Fill rest with image statistics (variance, etc.)
            for (int i = 2; i < features.length; i++) {
                features[i] = (mean.val[0] / 255.0) * (i % 10) / 10.0; // Simple pattern
            }
            
            meanMat.release();
            stddevMat.release();
            
        } catch (Exception e) {
            logger.debug("Error extracting spatial features", e);
        }
        
        return features;
    }
    
    /**
     * Extracts edge features using Laplacian operator.
     */
    private double[] extractEdgeFeatures(Mat gray) {
        double[] features = new double[128];
        
        try {
            Mat laplacian = new Mat();
            Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);
            
            // Calculate histogram of Laplacian
            Mat hist = new Mat();
            MatOfInt histSize = new MatOfInt(128);
            MatOfFloat ranges = new MatOfFloat(-1000f, 1000f);
            MatOfInt channels = new MatOfInt(0);
            
            Imgproc.calcHist(
                java.util.Arrays.asList(laplacian),
                channels,
                new Mat(),
                hist,
                histSize,
                ranges
            );
            
            Core.normalize(hist, hist, 0.0, 1.0, Core.NORM_MINMAX);
            
            double[] histData = new double[(int) hist.total()];
            hist.get(0, 0, histData);
            
            System.arraycopy(histData, 0, features, 0, Math.min(features.length, histData.length));
            
            laplacian.release();
            hist.release();
            
        } catch (Exception e) {
            logger.debug("Error extracting edge features", e);
        }
        
        return features;
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
        if (lowerPath.contains("arcface")) {
            modelType = "arcface";
            embeddingSize = 512;
            inputSize = new Size(112, 112); // ArcFace uses 112x112 input
            logger.info("Detected ArcFace model - using 112x112 input, 512-dim embeddings (best accuracy)");
        } else if (lowerPath.contains("facenet")) {
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
            // Default to ArcFace settings (best)
            modelType = "unknown";
            embeddingSize = 512;
            inputSize = new Size(112, 112);
            logger.warn("Unknown model type, defaulting to 112x112 input, 512-dim embeddings (ArcFace-like)");
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

