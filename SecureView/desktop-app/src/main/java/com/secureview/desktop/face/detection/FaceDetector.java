package com.secureview.desktop.face.detection;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Handles face detection using OpenCV's Haar Cascade classifier.
 */
public class FaceDetector {
    private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
    
    private CascadeClassifier faceCascade;
    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    
    public void initialize() throws Exception {
        logger.info("Initializing Face Detector...");
        
        // Load cascade classifier (stub will use real OpenCV if available)
        faceCascade = new CascadeClassifier();
        
        // Try to load from resources or use default OpenCV path
        String cascadePath = loadCascadeFile();
        
        // Normalize path for OpenCV (use forward slashes or absolute path)
        String normalizedPath = new File(cascadePath).getAbsolutePath().replace("\\", "/");
        
        logger.info("Attempting to load cascade from: {}", normalizedPath);
        boolean loaded = faceCascade.load(normalizedPath);
        
        if (!loaded) {
            // Try with backslashes (Windows)
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
        
        logger.info("Face Detector initialized successfully with cascade: {}", cascadePath);
    }
    
    /**
     * Detects face in the given image.
     * @param image Input image
     * @return Cropped face region, or null if no face detected
     */
    public Mat detectFace(Mat image) {
        if (image == null || image.empty()) {
            logger.warn("Empty or null image provided for face detection");
            return null;
        }
        
        logger.debug("Starting face detection on image: {}x{}", image.cols(), image.rows());
        
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
        Mat face = detectFace(image);
        if (face != null) {
            face.release();
            return true;
        }
        return false;
    }
    
    private String loadCascadeFile() throws Exception {
        // Try to find cascade file in common locations
        String opencvDir = System.getenv("OPENCV_DIR");
        if (opencvDir == null || opencvDir.isEmpty()) {
            // Try common OpenCV installation paths
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
        
        String[] possiblePaths = {
            // OpenCV installation directory (most common)
            opencvDir != null ? opencvDir + File.separator + "build" + File.separator + "etc" + File.separator + "haarcascades" + File.separator + CASCADE_FILE : null,
            opencvDir != null ? opencvDir + File.separator + "data" + File.separator + "haarcascades" + File.separator + CASCADE_FILE : null,
            // Alternative OpenCV paths
            "C:\\Users\\TUSHAR\\Downloads\\opencv\\build\\etc\\haarcascades\\" + CASCADE_FILE,
            "C:\\opencv\\build\\etc\\haarcascades\\" + CASCADE_FILE,
            "C:\\opencv\\data\\haarcascades\\" + CASCADE_FILE,
            // Current directory and resources
            System.getProperty("user.dir") + File.separator + CASCADE_FILE,
            System.getProperty("opencv.data.dir", "") + File.separator + CASCADE_FILE,
            "resources" + File.separator + CASCADE_FILE
        };
        
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

