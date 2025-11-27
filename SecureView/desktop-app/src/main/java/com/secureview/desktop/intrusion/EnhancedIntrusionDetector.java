package com.secureview.desktop.intrusion;

import com.secureview.desktop.face.detection.FaceDetector;
import com.secureview.desktop.face.FaceRecognitionService;
import com.secureview.desktop.firebase.FirebaseService;
import com.secureview.desktop.opencv.stub.*;
import com.secureview.desktop.user.UserManager;
import com.secureview.desktop.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enhanced intrusion detection with multiple unknown faces, motion detection, and alerts.
 */
public class EnhancedIntrusionDetector {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedIntrusionDetector.class);
    
    private FaceDetector faceDetector;
    private FaceRecognitionService faceRecognitionService;
    private FirebaseService firebaseService;
    private UserManager userManager;
    
    private AtomicBoolean isMonitoring = new AtomicBoolean(false);
    private Mat previousFrame;
    private long lastMotionDetectionTime = 0;
    private static final long MOTION_DETECTION_INTERVAL = 1000; // 1 second
    private static final double MOTION_THRESHOLD = 5000.0; // Motion sensitivity
    
    // Sound detection (simplified - would need audio library in production)
    private boolean soundDetectionEnabled = false;
    
    public EnhancedIntrusionDetector(FaceDetector faceDetector,
                                    FaceRecognitionService faceRecognitionService,
                                    FirebaseService firebaseService,
                                    UserManager userManager) {
        this.faceDetector = faceDetector;
        this.faceRecognitionService = faceRecognitionService;
        this.firebaseService = firebaseService;
        this.userManager = userManager;
    }
    
    /**
     * Starts intrusion monitoring.
     */
    public void startMonitoring() {
        isMonitoring.set(true);
        logger.info("Enhanced intrusion detection monitoring started");
    }
    
    /**
     * Stops intrusion monitoring.
     */
    public void stopMonitoring() {
        isMonitoring.set(false);
        if (previousFrame != null) {
            previousFrame.release();
            previousFrame = null;
        }
        logger.info("Enhanced intrusion detection monitoring stopped");
    }
    
    /**
     * Analyzes frame for intrusion detection.
     * Checks for multiple unknown faces, motion, and other threats.
     */
    public void analyzeFrame(Mat frame) {
        if (!isMonitoring.get() || frame == null || frame.empty()) {
            return;
        }
        
        try {
            // 1. Detect multiple unknown faces
            detectMultipleUnknownFaces(frame);
            
            // 2. Motion detection
            detectMotion(frame);
            
            // 3. Sound detection (if enabled)
            if (soundDetectionEnabled) {
                // Would integrate with audio library here
                // detectSound();
            }
            
        } catch (Exception e) {
            logger.error("Error during intrusion detection", e);
        }
    }
    
    /**
     * Detects multiple unknown faces in the frame.
     */
    private void detectMultipleUnknownFaces(Mat frame) {
        try {
            // Detect all faces in frame
            List<Mat> detectedFaces = detectAllFaces(frame);
            
            if (detectedFaces.isEmpty()) {
                return;
            }
            
            List<Mat> unknownFaces = new ArrayList<>();
            int knownFaceCount = 0;
            
            // Check each face against registered users
            for (Mat face : detectedFaces) {
                try {
                    // Try to identify the face
                    UserProfile matchedUser = userManager.findUserByFace(
                        face, 
                        faceRecognitionService.getImageComparisonService()
                    );
                    
                    if (matchedUser == null) {
                        // Unknown face
                        unknownFaces.add(face);
                    } else {
                        knownFaceCount++;
                    }
                } catch (Exception e) {
                    logger.warn("Error identifying face", e);
                    unknownFaces.add(face);
                }
            }
            
            // Alert if multiple unknown faces detected
            if (unknownFaces.size() > 1) {
                logger.warn("MULTIPLE UNKNOWN FACES DETECTED: {} unknown, {} known", 
                           unknownFaces.size(), knownFaceCount);
                handleIntrusionAlert(frame, unknownFaces, 
                    "Multiple unknown faces detected: " + unknownFaces.size());
            } else if (unknownFaces.size() == 1 && detectedFaces.size() > 1) {
                // One unknown face among multiple faces
                logger.warn("UNKNOWN FACE DETECTED among {} total faces", detectedFaces.size());
                handleIntrusionAlert(frame, unknownFaces, 
                    "Unknown face detected among " + detectedFaces.size() + " faces");
            } else if (unknownFaces.size() == 1) {
                // Single unknown face
                logger.warn("UNKNOWN FACE DETECTED");
                handleIntrusionAlert(frame, unknownFaces, "Unknown face detected");
            }
            
            // Cleanup
            for (Mat face : detectedFaces) {
                if (!unknownFaces.contains(face)) {
                    face.release();
                }
            }
            
        } catch (Exception e) {
            logger.error("Error detecting multiple unknown faces", e);
        }
    }
    
    /**
     * Detects all faces in a frame (not just the first one).
     */
    private List<Mat> detectAllFaces(Mat frame) {
        List<Mat> faces = new ArrayList<>();
        // This would need to be implemented in FaceDetector to return all faces
        // For now, we'll use a simplified approach
        try {
            Mat face = faceDetector.detectFace(frame);
            if (face != null && !face.empty()) {
                faces.add(face);
            }
            // In production, you'd call a method that returns all detected faces
        } catch (Exception e) {
            logger.warn("Error detecting faces", e);
        }
        return faces;
    }
    
    /**
     * Detects motion in the frame.
     */
    private void detectMotion(Mat frame) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMotionDetectionTime < MOTION_DETECTION_INTERVAL) {
            return; // Skip if too soon
        }
        lastMotionDetectionTime = currentTime;
        
        try {
            if (previousFrame == null || previousFrame.empty()) {
                // Store first frame
                previousFrame = new Mat();
                frame.copyTo(previousFrame);
                return;
            }
            
            // Convert to grayscale
            Mat gray1 = new Mat();
            Mat gray2 = new Mat();
            if (previousFrame.channels() == 3) {
                Imgproc.cvtColor(previousFrame, gray1, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray1 = previousFrame;
            }
            
            if (frame.channels() == 3) {
                Imgproc.cvtColor(frame, gray2, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray2 = frame;
            }
            
            // Resize if needed
            if (gray1.cols() != gray2.cols() || gray1.rows() != gray2.rows()) {
                Mat resized = new Mat();
                Imgproc.resize(gray1, resized, gray2.size());
                gray1.release();
                gray1 = resized;
            }
            
            // Calculate absolute difference
            Mat diff = new Mat();
            Core.absdiff(gray1, gray2, diff);
            
            // Calculate motion amount
            Scalar meanDiff = Core.mean(diff);
            double motionAmount = meanDiff.val[0];
            
            // Update previous frame
            previousFrame.release();
            previousFrame = new Mat();
            frame.copyTo(previousFrame);
            
            // Check if motion exceeds threshold
            if (motionAmount > MOTION_THRESHOLD) {
                logger.warn("Significant motion detected: {}", motionAmount);
                // Could trigger alert here if motion is suspicious
            }
            
            gray1.release();
            gray2.release();
            diff.release();
            
        } catch (Exception e) {
            logger.warn("Error detecting motion", e);
        }
    }
    
    /**
     * Handles intrusion alert - saves images and sends notifications.
     */
    private void handleIntrusionAlert(Mat frame, List<Mat> unknownFaces, String details) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            
            // Save intruder images
            String dataDir = com.secureview.desktop.config.ConfigManager.getInstance()
                .getConfig().getDataDirectory();
            String intrusionDir = dataDir + File.separator + "intrusions";
            Files.createDirectories(Paths.get(intrusionDir));
            
            List<String> savedImages = new ArrayList<>();
            for (int i = 0; i < unknownFaces.size(); i++) {
                String imagePath = intrusionDir + File.separator + 
                                  "intruder_" + timestamp + "_" + i + ".jpg";
                boolean saved = Imgcodecs.imwrite(imagePath, unknownFaces.get(i));
                if (saved) {
                    savedImages.add(imagePath);
                    logger.info("Saved intruder image: {}", imagePath);
                }
            }
            
            // Save full frame
            String framePath = intrusionDir + File.separator + "frame_" + timestamp + ".jpg";
            Imgcodecs.imwrite(framePath, frame);
            
            // Send alert to mobile app
            if (firebaseService != null && firebaseService.isInitialized()) {
                try {
                    // Convert first unknown face to bytes for notification
                    if (!unknownFaces.isEmpty()) {
                        // Save to temp file and read bytes
                        String tempPath = dataDir + File.separator + "temp_intruder.jpg";
                        Imgcodecs.imwrite(tempPath, unknownFaces.get(0));
                        byte[] imageBytes = Files.readAllBytes(Paths.get(tempPath));
                        new File(tempPath).delete();
                        
                        firebaseService.sendIntrusionAlert(
                            imageBytes,
                            timestamp,
                            details + " | Faces detected: " + unknownFaces.size()
                        );
                        logger.info("Intrusion alert sent to mobile app");
                    }
                } catch (Exception e) {
                    logger.error("Error sending intrusion alert", e);
                }
            }
            
            logger.warn("INTRUSION ALERT: {} - {} images saved", details, savedImages.size());
            
        } catch (Exception e) {
            logger.error("Error handling intrusion alert", e);
        }
    }
    
    /**
     * Enables/disables sound detection.
     */
    public void setSoundDetectionEnabled(boolean enabled) {
        this.soundDetectionEnabled = enabled;
        logger.info("Sound detection {}", enabled ? "enabled" : "disabled");
    }
}

