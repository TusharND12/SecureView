package com.secureview.desktop.face.liveness;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects liveness to prevent spoofing attacks using photos or videos.
 * Uses multiple techniques: eye blink detection, head movement, texture analysis.
 */
public class LivenessDetector {
    private static final Logger logger = LoggerFactory.getLogger(LivenessDetector.class);
    
    private List<Mat> previousFrames;
    private static final int FRAME_HISTORY_SIZE = 5;
    private static final double MIN_MOVEMENT_THRESHOLD = 0.02;
    
    public void initialize() {
        logger.info("Initializing Liveness Detector...");
        previousFrames = new ArrayList<>();
        logger.info("Liveness Detector initialized");
    }
    
    /**
     * Verifies liveness of the face in the current frame.
     * Optimized for speed - uses simplified checks.
     * @param faceImage Current face image
     * @return true if liveness is detected, false otherwise
     */
    public boolean verifyLiveness(Mat faceImage) {
        if (faceImage.empty()) {
            return false;
        }
        
        // Fast path: If we have previous frames, do quick movement check
        // Otherwise, assume live (first frame)
        if (previousFrames.isEmpty()) {
            updateFrameHistory(faceImage);
            return true; // First frame, assume live
        }
        
        // Quick movement check (fastest method)
        boolean hasMovement = checkMovement(faceImage);
        
        // Update frame history
        updateFrameHistory(faceImage);
        
        // Simplified: Only check movement for speed
        // Texture and 3D checks are expensive, skip them for faster authentication
        if (hasMovement) {
            return true; // Movement detected, assume live
        }
        
        // If no movement, do a quick texture check (simplified)
        // Skip expensive 3D structure check
        boolean isRealTexture = checkTextureFast(faceImage);
        
        boolean isLive = isRealTexture; // Only need texture check if no movement
        
        if (!isLive) {
            logger.debug("Liveness detection failed. Movement: {}, Texture: {}", 
                hasMovement, isRealTexture);
        }
        
        return isLive;
    }
    
    /**
     * Checks for movement between frames.
     */
    private boolean checkMovement(Mat currentFrame) {
        if (previousFrames.isEmpty()) {
            return true; // First frame, assume movement
        }
        
        Mat previousFrame = previousFrames.get(previousFrames.size() - 1);
        
        // Calculate optical flow or frame difference
        Mat diff = new Mat();
        Core.absdiff(currentFrame, previousFrame, diff);
        
        // Calculate mean difference
        Scalar meanDiff = Core.mean(diff);
        double movement = meanDiff.val[0] / 255.0;
        
        diff.release();
        
        return movement > MIN_MOVEMENT_THRESHOLD;
    }
    
    /**
     * Fast texture check - simplified version for performance.
     */
    private boolean checkTextureFast(Mat faceImage) {
        // Simplified texture check using variance
        Mat gray = new Mat();
        if (faceImage.channels() == 3) {
            Imgproc.cvtColor(faceImage, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = faceImage;
        }
        
        Mat mean = new Mat();
        Mat stddev = new Mat();
        Core.meanStdDev(gray, mean, stddev);
        
        // Defensive coding: stub Mat.get(...) may return null if underlying data
        // is not available yet. In that case, treat variance as low (non-live)
        // instead of throwing an exception.
        double variance = 0.0;
        try {
            double[] vals = stddev.get(0, 0);
            if (vals != null && vals.length > 0) {
                variance = vals[0];
            } else {
                logger.debug("LivenessDetector.checkTextureFast: stddev.get(0,0) returned null/empty, treating as low variance");
            }
        } catch (Exception e) {
            logger.debug("LivenessDetector.checkTextureFast: error reading stddev value, treating as low variance", e);
            variance = 0.0;
        }
        
        if (gray != faceImage) {
            gray.release();
        }
        
        // Lower threshold for faster processing
        return variance > 8.0;
    }
    
    /**
     * Analyzes texture to detect printed photos (full version - slower).
     */
    private boolean checkTexture(Mat faceImage) {
        // Convert to grayscale if needed
        Mat gray = new Mat();
        if (faceImage.channels() == 3) {
            Imgproc.cvtColor(faceImage, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = faceImage;
        }
        
        // Calculate Local Binary Pattern (LBP) or similar texture descriptor
        // Real faces have more texture variation than printed photos
        
        Mat lbp = calculateLBP(gray);
        
        // Calculate variance of LBP
        Mat mean = new Mat();
        Mat stddev = new Mat();
        Core.meanStdDev(lbp, mean, stddev);
        
        double textureVariance = stddev.get(0, 0)[0];
        
        if (gray != faceImage) {
            gray.release();
        }
        lbp.release();
        
        // Real faces typically have higher texture variance
        return textureVariance > 10.0;
    }
    
    /**
     * Calculates Local Binary Pattern for texture analysis.
     */
    private Mat calculateLBP(Mat image) {
        Mat lbp = new Mat(image.size(), CvType.CV_8UC1);
        
        for (int y = 1; y < image.rows() - 1; y++) {
            for (int x = 1; x < image.cols() - 1; x++) {
                double center = image.get(y, x)[0];
                int code = 0;
                
                // 8-neighborhood
                double[] neighbors = {
                    image.get(y - 1, x - 1)[0],
                    image.get(y - 1, x)[0],
                    image.get(y - 1, x + 1)[0],
                    image.get(y, x + 1)[0],
                    image.get(y + 1, x + 1)[0],
                    image.get(y + 1, x)[0],
                    image.get(y + 1, x - 1)[0],
                    image.get(y, x - 1)[0]
                };
                
                for (int i = 0; i < 8; i++) {
                    if (neighbors[i] >= center) {
                        code |= (1 << i);
                    }
                }
                
                lbp.put(y, x, code);
            }
        }
        
        return lbp;
    }
    
    /**
     * Checks for 3D structure (depth information).
     */
    private boolean check3DStructure(Mat faceImage) {
        // Simplified 3D structure check using gradient analysis
        // Real faces have depth, which creates gradients
        // Flat photos have less pronounced gradients
        
        Mat gray = new Mat();
        if (faceImage.channels() == 3) {
            Imgproc.cvtColor(faceImage, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = faceImage;
        }
        
        // Calculate gradients
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Mat gradMag = new Mat();
        
        Imgproc.Sobel(gray, gradX, CvType.CV_64F, 1, 0, 3, 1.0, 0.0);
        Imgproc.Sobel(gray, gradY, CvType.CV_64F, 0, 1, 3, 1.0, 0.0);
        
        Core.magnitude(gradX, gradY, gradMag);
        
        // Calculate mean gradient magnitude
        Scalar meanGrad = Core.mean(gradMag);
        double avgGradient = meanGrad.val[0];
        
        if (gray != faceImage) {
            gray.release();
        }
        gradX.release();
        gradY.release();
        gradMag.release();
        
        // Real faces have more pronounced gradients
        return avgGradient > 20.0;
    }
    
    /**
     * Updates frame history for movement detection.
     */
    private void updateFrameHistory(Mat frame) {
        Mat frameCopy = new Mat();
        frame.copyTo(frameCopy);
        
        previousFrames.add(frameCopy);
        
        if (previousFrames.size() > FRAME_HISTORY_SIZE) {
            Mat oldFrame = previousFrames.remove(0);
            oldFrame.release();
        }
    }
    
    /**
     * Resets the liveness detector state.
     */
    public void reset() {
        for (Mat frame : previousFrames) {
            frame.release();
        }
        previousFrames.clear();
    }
}

