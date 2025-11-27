package com.secureview.desktop.face.detection;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Handles face detection using RetinaFace (preferred) or Haar Cascade (fallback).
 * RetinaFace provides state-of-the-art accuracy for face detection.
 */
public class FaceDetector {
    private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
    
    private CascadeClassifier faceCascade;
    private RetinaFaceDetector retinaFaceDetector;
    private boolean useRetinaFace = false;
    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    
    public void initialize() throws Exception {
        logger.info("Initializing Face Detector...");
        
        // Try to initialize RetinaFace first (better accuracy)
        String retinaFaceModelPath = System.getProperty("user.home") + "/.secureview/models/retinaface.onnx";
        File retinaFaceModel = new File(retinaFaceModelPath);
        
        // Check if file exists AND is not empty (at least 1MB for ONNX models)
        if (retinaFaceModel.exists() && retinaFaceModel.isFile() && retinaFaceModel.length() > 1024 * 1024) {
            try {
                logger.info("RetinaFace model found. Initializing RetinaFace detector...");
                retinaFaceDetector = new RetinaFaceDetector();
                retinaFaceDetector.initialize();
                useRetinaFace = true;
                logger.info("Face Detector initialized with RetinaFace (high accuracy)");
                return;
            } catch (Exception e) {
                logger.warn("RetinaFace initialization failed, falling back to Haar Cascade: {}", e.getMessage());
            }
        } else {
            if (retinaFaceModel.exists() && retinaFaceModel.length() == 0) {
                logger.warn("RetinaFace model file exists but is EMPTY (0 bytes) at: {}. Using Haar Cascade.", retinaFaceModelPath);
                logger.warn("Please delete the empty file and download a valid model.");
            } else {
                logger.info("RetinaFace model not found at: {}. Using Haar Cascade.", retinaFaceModelPath);
            }
            logger.info("For better accuracy, download RetinaFace model to: ~/.secureview/models/retinaface.onnx");
            logger.info("Expected file size: ~1.7 MB. See MODEL_INSTALLATION_GUIDE.md for download instructions.");
        }
        
        // Fallback to Haar Cascade
        faceCascade = new CascadeClassifier();
        String cascadePath = loadCascadeFile();
        String normalizedPath = new File(cascadePath).getAbsolutePath().replace("\\", "/");
        
        logger.info("Attempting to load cascade from: {}", normalizedPath);
        boolean loaded = faceCascade.load(normalizedPath);
        
        if (!loaded) {
            normalizedPath = new File(cascadePath).getAbsolutePath();
            logger.info("Retrying with Windows path format: {}", normalizedPath);
            loaded = faceCascade.load(normalizedPath);
        }
        
        if (!loaded) {
            throw new Exception("Failed to load face cascade classifier from: " + cascadePath + 
                "\nFile exists: " + new File(cascadePath).exists() +
                "\nPlease verify:\n" +
                "1. The file exists and is readable\n" +
                "2. OpenCV native library is loaded\n" +
                "3. OpenCV is properly installed");
        }
        
        logger.info("Face Detector initialized successfully with Haar Cascade: {}", cascadePath);
    }
    
    /**
     * Detects face in the given image.
     * Uses RetinaFace if available, otherwise falls back to Haar Cascade.
     * @param image Input image
     * @return Cropped face region, or null if no face detected
     */
    public Mat detectFace(Mat image) {
        if (image == null || image.empty()) {
            logger.warn("Empty or null image provided for face detection");
            return null;
        }
        
        logger.debug("Starting face detection on image: {}x{}", image.cols(), image.rows());
        
        // Use RetinaFace if available
        if (useRetinaFace && retinaFaceDetector != null) {
            try {
                Mat face = retinaFaceDetector.detectFace(image);
                if (face != null && !face.empty()) {
                    logger.debug("Face detected using RetinaFace");
                    return face;
                }
            } catch (Exception e) {
                logger.warn("RetinaFace detection failed, falling back to Haar Cascade: {}", e.getMessage());
            }
        }
        
        // Fallback to Haar Cascade
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);
        
        logger.debug("Converted to grayscale and equalized histogram");
        
        MatOfRect faces = new MatOfRect();
        logger.debug("Calling detectMultiScale...");
        faceCascade.detectMultiScale(
            gray,
            faces,
            1.1,
            3,
            0,
            new Size(30, 30),
            new Size()
        );
        
        logger.debug("detectMultiScale completed, extracting faces array...");
        Rect[] facesArray = faces.toArray();
        logger.info("Face detection found {} faces", facesArray.length);
        
        if (facesArray.length == 0) {
            logger.debug("No face detected in image");
            gray.release();
            faces.release();
            return null;
        }
        
        // Use the largest face detected
        Rect largestFace = facesArray[0];
        for (Rect face : facesArray) {
            if (face.area() > largestFace.area()) {
                largestFace = face;
            }
        }
        
        // Extract face region with padding
        int padding = 20;
        int x = Math.max(0, largestFace.x - padding);
        int y = Math.max(0, largestFace.y - padding);
        int width = Math.min(image.cols() - x, largestFace.width + 2 * padding);
        int height = Math.min(image.rows() - y, largestFace.height + 2 * padding);
        
        Rect faceRect = new Rect(x, y, width, height);
        Mat faceRegion = new Mat(image, faceRect);
        Mat faceCopy = new Mat();
        faceRegion.copyTo(faceCopy);
        
        gray.release();
        faces.release();
        
        logger.debug("Face detected at ({}, {}) with size {}x{}", 
            largestFace.x, largestFace.y, largestFace.width, largestFace.height);
        
        return faceCopy;
    }
    
    /**
     * Checks if a face is detected in the image.
     */
    public boolean hasFace(Mat image) {
        if (useRetinaFace && retinaFaceDetector != null) {
            try {
                return retinaFaceDetector.hasFace(image);
            } catch (Exception e) {
                logger.debug("RetinaFace hasFace check failed", e);
            }
        }
        
        Mat face = detectFace(image);
        if (face != null) {
            face.release();
            return true;
        }
        return false;
    }
    
    /**
     * Returns true if using RetinaFace, false if using Haar Cascade.
     */
    public boolean isUsingRetinaFace() {
        return useRetinaFace;
    }
    
    private String loadCascadeFile() throws Exception {
        // Detect OS
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");
        boolean isLinux = osName.contains("linux") || osName.contains("unix");
        
        // Try to find cascade file in common locations
        String opencvDir = System.getenv("OPENCV_DIR");
        if (opencvDir == null || opencvDir.isEmpty()) {
            if (isLinux) {
                // Linux common paths
                String[] linuxPaths = {
                    "/usr",
                    "/usr/local",
                    System.getProperty("user.home") + "/opencv"
                };
                for (String path : linuxPaths) {
                    File testDir = new File(path);
                    if (testDir.exists() && testDir.isDirectory()) {
                        opencvDir = path;
                        break;
                    }
                }
            } else {
                // Windows common paths
                String[] commonPaths = {
                    "C:\\Users\\TUSHAR\\Downloads\\opencv",
                    "C:\\opencv",
                    "C:\\opencv4120",
                    System.getProperty("user.home") + "\\Downloads\\opencv"
                };
                for (String path : commonPaths) {
                    File testDir = new File(path);
                    if (testDir.exists() && testDir.isDirectory()) {
                        opencvDir = path;
                        break;
                    }
                }
            }
        }
        
        // Build list of possible paths based on OS
        java.util.List<String> possiblePathsList = new java.util.ArrayList<>();
        
        if (isLinux) {
            // Linux system paths (check these first)
            possiblePathsList.add("/usr/share/opencv4/haarcascades/" + CASCADE_FILE);
            possiblePathsList.add("/usr/share/opencv4/haarcascades/haarcascade_frontalface_alt.xml");
            possiblePathsList.add("/usr/local/share/opencv4/haarcascades/" + CASCADE_FILE);
            possiblePathsList.add("/usr/share/opencv/haarcascades/" + CASCADE_FILE);
            
            // OpenCV installation directory paths
            if (opencvDir != null) {
                possiblePathsList.add(opencvDir + "/build/etc/haarcascades/" + CASCADE_FILE);
                possiblePathsList.add(opencvDir + "/data/haarcascades/" + CASCADE_FILE);
                possiblePathsList.add(opencvDir + "/share/opencv4/haarcascades/" + CASCADE_FILE);
            }
        } else {
            // Windows paths
            if (opencvDir != null) {
                possiblePathsList.add(opencvDir + File.separator + "build" + File.separator + "etc" + File.separator + "haarcascades" + File.separator + CASCADE_FILE);
                possiblePathsList.add(opencvDir + File.separator + "data" + File.separator + "haarcascades" + File.separator + CASCADE_FILE);
            }
            possiblePathsList.add("C:\\Users\\TUSHAR\\Downloads\\opencv\\build\\etc\\haarcascades\\" + CASCADE_FILE);
            possiblePathsList.add("C:\\opencv\\build\\etc\\haarcascades\\" + CASCADE_FILE);
            possiblePathsList.add("C:\\opencv\\data\\haarcascades\\" + CASCADE_FILE);
        }
        
        // Common paths for both OS
        possiblePathsList.add(System.getProperty("user.dir") + File.separator + CASCADE_FILE);
        possiblePathsList.add(System.getProperty("opencv.data.dir", "") + File.separator + CASCADE_FILE);
        possiblePathsList.add("resources" + File.separator + CASCADE_FILE);
        
        String[] possiblePaths = possiblePathsList.toArray(new String[0]);
        
        for (String path : possiblePaths) {
            if (path == null) continue;
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                // Normalize path - use absolute path and replace backslashes
                String normalizedPath = file.getAbsolutePath();
                logger.info("Found cascade file at: {}", normalizedPath);
                return normalizedPath;
            }
        }
        
        // Try to extract from resources
        try (InputStream is = getClass().getResourceAsStream("/" + CASCADE_FILE)) {
            if (is != null) {
                File tempFile = File.createTempFile("haarcascade_", ".xml");
                Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Extracted cascade file to: {}", tempFile.getAbsolutePath());
                return tempFile.getAbsolutePath();
            }
        } catch (Exception e) {
            logger.warn("Could not load cascade from resources", e);
        }
        
        throw new Exception("Could not find face cascade classifier file. Please ensure OpenCV is properly installed.\n" +
            "Expected location: " + opencvDir + File.separator + "build" + File.separator + "etc" + File.separator + "haarcascades" + File.separator + CASCADE_FILE);
    }
}

