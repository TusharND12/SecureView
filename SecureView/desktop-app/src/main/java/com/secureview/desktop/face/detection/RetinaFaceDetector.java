package com.secureview.desktop.face.detection;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced face detection using RetinaFace deep learning model.
 * Provides high accuracy face detection with landmark extraction.
 */
public class RetinaFaceDetector {
    private static final Logger logger = LoggerFactory.getLogger(RetinaFaceDetector.class);
    
    private Net retinaFaceNet;
    private boolean initialized = false;
    private static final String MODEL_PATH = System.getProperty("user.home") + "/.secureview/models/retinaface.onnx";
    private static final double CONFIDENCE_THRESHOLD = 0.5;
    private static final Size INPUT_SIZE = new Size(640, 640);
    
    /**
     * Face detection result with bounding box and landmarks
     */
    public static class FaceDetection {
        public Rect boundingBox;
        public double confidence;
        public Point[] landmarks; // 5 landmarks: left eye, right eye, nose, left mouth, right mouth
        
        public FaceDetection(Rect box, double conf, Point[] land) {
            this.boundingBox = box;
            this.confidence = conf;
            this.landmarks = land;
        }
    }
    
    public void initialize() throws Exception {
        if (initialized) {
            return;
        }
        
        logger.info("Initializing RetinaFace Detector...");
        
        File modelFile = new File(MODEL_PATH);
        if (!modelFile.exists()) {
            throw new Exception("RetinaFace model not found at: " + MODEL_PATH + 
                "\nPlease download the model from: https://github.com/opencv/opencv_zoo/tree/master/models/face_detection_retinaface" +
                "\nSave as: ~/.secureview/models/retinaface.onnx");
        }
        
        try {
            retinaFaceNet = Dnn.readNetFromONNX(MODEL_PATH);
            if (retinaFaceNet.getRealInstance() == null) {
                throw new Exception("Failed to load RetinaFace model. OpenCV DNN may not be available.");
            }
            
            // Set backend and target
            try {
                Class<?> netClass = retinaFaceNet.getRealInstance().getClass();
                java.lang.reflect.Method setBackendMethod = netClass.getMethod("setPreferableBackend", int.class);
                java.lang.reflect.Method setTargetMethod = netClass.getMethod("setPreferableTarget", int.class);
                
                // DNN_BACKEND_OPENCV = 0, DNN_TARGET_CPU = 0
                setBackendMethod.invoke(retinaFaceNet.getRealInstance(), 0);
                setTargetMethod.invoke(retinaFaceNet.getRealInstance(), 0);
            } catch (Exception e) {
                logger.warn("Could not set DNN backend preferences", e);
            }
            
            initialized = true;
            logger.info("RetinaFace Detector initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize RetinaFace detector", e);
            throw new Exception("RetinaFace initialization failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Detects faces in the image using RetinaFace.
     * @param image Input image
     * @return List of detected faces with bounding boxes and landmarks
     */
    public List<FaceDetection> detectFaces(Mat image) {
        if (!initialized) {
            logger.error("RetinaFace detector not initialized");
            return new ArrayList<>();
        }
        
        if (image == null || image.empty()) {
            logger.warn("Empty or null image provided");
            return new ArrayList<>();
        }
        
        try {
            // Preprocess: resize to 640x640 and normalize
            Mat resized = new Mat();
            Imgproc.resize(image, resized, INPUT_SIZE);
            
            // Normalize to [0, 1] range
            Mat normalized = new Mat();
            resized.convertTo(normalized, CvType.CV_32F, 1.0 / 255.0);
            
            // Create blob: scale=1.0, mean=(0,0,0), swapRB=false, crop=false
            Mat blob = Dnn.blobFromImage(normalized, 1.0, INPUT_SIZE, new Scalar(0, 0, 0), false, false);
            
            // Run inference
            retinaFaceNet.setInput(blob);
            Mat output = retinaFaceNet.forward();
            
            // Parse output
            List<FaceDetection> detections = parseRetinaFaceOutput(output, image.cols(), image.rows());
            
            // Cleanup
            resized.release();
            normalized.release();
            blob.release();
            output.release();
            
            logger.info("RetinaFace detected {} faces", detections.size());
            return detections;
            
        } catch (Exception e) {
            logger.error("Error during RetinaFace detection", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets the largest face from detection results.
     */
    public Mat detectFace(Mat image) {
        List<FaceDetection> detections = detectFaces(image);
        if (detections.isEmpty()) {
            return null;
        }
        
        // Find largest face
        FaceDetection largest = detections.get(0);
        for (FaceDetection det : detections) {
            if (det.boundingBox.area() > largest.boundingBox.area()) {
                largest = det;
            }
        }
        
        // Extract face region with padding
        int padding = 20;
        Rect box = largest.boundingBox;
        int x = Math.max(0, box.x - padding);
        int y = Math.max(0, box.y - padding);
        int width = Math.min(image.cols() - x, box.width + 2 * padding);
        int height = Math.min(image.rows() - y, box.height + 2 * padding);
        
        Rect faceRect = new Rect(x, y, width, height);
        Mat faceRegion = new Mat(image, faceRect);
        Mat faceCopy = new Mat();
        faceRegion.copyTo(faceCopy);
        
        return faceCopy;
    }
    
    /**
     * Checks if a face is detected.
     */
    public boolean hasFace(Mat image) {
        List<FaceDetection> detections = detectFaces(image);
        return !detections.isEmpty();
    }
    
    /**
     * Parses RetinaFace output format.
     * Output shape: [1, num_detections, 15]
     * Where 15 = [x1, y1, x2, y2, score, landmark_x1, landmark_y1, ..., landmark_x5, landmark_y5]
     */
    private List<FaceDetection> parseRetinaFaceOutput(Mat output, int originalWidth, int originalHeight) {
        List<FaceDetection> detections = new ArrayList<>();
        
        try {
            // Get output dimensions
            int[] shape = getMatShape(output);
            if (shape.length < 2) {
                logger.warn("Unexpected output shape");
                return detections;
            }
            
            int numDetections = shape[1];
            int featuresPerDetection = shape.length > 2 ? shape[2] : 15;
            
            // Scale factors for converting back to original image size
            double scaleX = (double) originalWidth / INPUT_SIZE.width;
            double scaleY = (double) originalHeight / INPUT_SIZE.height;
            
            // Extract detection data
            double[] outputData = new double[(int) output.total()];
            output.get(0, 0, outputData);
            
            for (int i = 0; i < numDetections; i++) {
                int offset = i * featuresPerDetection;
                
                if (offset + 4 >= outputData.length) break;
                
                // Get bounding box (normalized coordinates)
                double x1 = outputData[offset] * originalWidth;
                double y1 = outputData[offset + 1] * originalHeight;
                double x2 = outputData[offset + 2] * originalWidth;
                double y2 = outputData[offset + 3] * originalHeight;
                double score = outputData[offset + 4];
                
                // Filter by confidence
                if (score < CONFIDENCE_THRESHOLD) {
                    continue;
                }
                
                // Create bounding box
                Rect box = new Rect((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1));
                
                // Extract landmarks (5 points: 2 eyes, nose, 2 mouth corners)
                Point[] landmarks = new Point[5];
                if (featuresPerDetection >= 15) {
                    for (int j = 0; j < 5; j++) {
                        double lx = outputData[offset + 5 + j * 2] * originalWidth;
                        double ly = outputData[offset + 5 + j * 2 + 1] * originalHeight;
                        landmarks[j] = new Point(lx, ly);
                    }
                }
                
                detections.add(new FaceDetection(box, score, landmarks));
            }
            
        } catch (Exception e) {
            logger.error("Error parsing RetinaFace output", e);
        }
        
        return detections;
    }
    
    /**
     * Gets the shape/dimensions of a Mat.
     */
    private int[] getMatShape(Mat mat) {
        try {
            Object realMat = mat.getRealInstance();
            if (realMat != null) {
                Class<?> matClass = realMat.getClass();
                java.lang.reflect.Method sizeMethod = matClass.getMethod("size");
                Object sizeObj = sizeMethod.invoke(realMat);
                
                // Get dimensions from Size object
                Class<?> sizeClass = sizeObj.getClass();
                java.lang.reflect.Method heightMethod = sizeClass.getMethod("height");
                java.lang.reflect.Method widthMethod = sizeClass.getMethod("width");
                
                int height = ((Number) heightMethod.invoke(sizeObj)).intValue();
                int width = ((Number) widthMethod.invoke(sizeObj)).intValue();
                
                return new int[]{height, width};
            }
        } catch (Exception e) {
            logger.debug("Could not get Mat shape", e);
        }
        return new int[]{1, 1}; // Default
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}

