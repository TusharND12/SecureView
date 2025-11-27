package com.secureview.desktop.face.angle;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects face angle/orientation for automatic multi-angle capture.
 */
public class AngleDetector {
    private static final Logger logger = LoggerFactory.getLogger(AngleDetector.class);
    
    /**
     * Detected angle information.
     */
    public static class AngleInfo {
        public int angle; // 0, 45, 90, 135, 180, 225, 270, 315
        public double confidence; // 0.0 to 1.0
        public String angleName; // "Front", "Right 45°", etc.
        
        public AngleInfo() {
            angle = 0;
            confidence = 0.0;
            angleName = "Unknown";
        }
    }
    
    /**
     * Detects the current face angle.
     * Returns the closest matching angle from the standard set.
     */
    public AngleInfo detectAngle(Mat faceImage) {
        AngleInfo info = new AngleInfo();
        
        if (faceImage == null || faceImage.empty()) {
            return info;
        }
        
        try {
            // Convert to grayscale
            Mat gray = new Mat();
            if (faceImage.channels() == 3) {
                Imgproc.cvtColor(faceImage, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = faceImage;
            }
            
            // Calculate face symmetry and orientation
            // For simplicity, we use symmetry as a proxy for frontal angle
            // In production, you'd use face landmarks or pose estimation
            
            double symmetry = calculateSymmetry(gray);
            
            // Estimate angle based on symmetry
            // High symmetry (0.8+) = frontal (0° or 180°)
            // Medium symmetry (0.6-0.8) = 45° or 135°
            // Low symmetry (<0.6) = 90° (side profile)
            
            if (symmetry >= 0.8) {
                // Frontal - determine if 0° or 180° based on other features
                info.angle = 0; // Default to front
                info.angleName = "Front";
                info.confidence = symmetry;
            } else if (symmetry >= 0.6) {
                // Slight angle - estimate 45° or 135°
                info.angle = 45; // Default
                info.angleName = "Right 45°";
                info.confidence = symmetry;
            } else {
                // Side profile
                info.angle = 90;
                info.angleName = "Right 90°";
                info.confidence = 1.0 - symmetry;
            }
            
            gray.release();
            
        } catch (Exception e) {
            logger.warn("Error detecting angle", e);
        }
        
        return info;
    }
    
    /**
     * Calculates face symmetry (0.0 to 1.0).
     */
    private double calculateSymmetry(Mat faceImage) {
        try {
            int width = faceImage.cols();
            int height = faceImage.rows();
            int midX = width / 2;
            
            Mat leftHalf = new Mat(faceImage, new Rect(0, 0, midX, height));
            Mat rightHalf = new Mat(faceImage, new Rect(midX, 0, width - midX, height));
            
            // Flip right half
            Mat rightFlipped = new Mat();
            Core.flip(rightHalf, rightFlipped, 1);
            
            // Resize to same size if needed
            if (leftHalf.cols() != rightFlipped.cols()) {
                Mat resized = new Mat();
                Imgproc.resize(rightFlipped, resized, leftHalf.size());
                rightFlipped.release();
                rightFlipped = resized;
            }
            
            // Calculate difference
            Mat diff = new Mat();
            Core.absdiff(leftHalf, rightFlipped, diff);
            Scalar meanDiff = Core.mean(diff);
            double symmetry = 1.0 - (meanDiff.val[0] / 255.0);
            symmetry = Math.max(0.0, Math.min(1.0, symmetry));
            
            leftHalf.release();
            rightHalf.release();
            rightFlipped.release();
            diff.release();
            
            return symmetry;
            
        } catch (Exception e) {
            logger.warn("Error calculating symmetry", e);
            return 0.7; // Default
        }
    }
    
    /**
     * Checks if the detected angle matches a target angle (with tolerance).
     */
    public boolean matchesAngle(AngleInfo detected, int targetAngle, int tolerance) {
        int diff = Math.abs(detected.angle - targetAngle);
        // Handle wrap-around (e.g., 315° and 0°)
        if (diff > 180) {
            diff = 360 - diff;
        }
        return diff <= tolerance;
    }
}



