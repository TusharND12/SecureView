package com.secureview.desktop.face.adaptive;

import com.secureview.desktop.opencv.stub.Imgcodecs;
import com.secureview.desktop.opencv.stub.Mat;
import com.secureview.desktop.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Adaptive learning service that improves face recognition over time.
 * Handles aging, appearance changes, and retraining on successful authentications.
 */
public class AdaptiveLearningService {
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveLearningService.class);
    
    private static final double RETRAIN_THRESHOLD = 0.75; // Retrain if confidence is above this
    private static final double MIN_CONFIDENCE_FOR_LEARNING = 0.65; // Minimum confidence to learn from
    private static final int MAX_REFERENCE_IMAGES = 20; // Maximum number of reference images per user
    
    /**
     * Records a successful authentication for adaptive learning.
     * If confidence is high enough, adds the image to reference set.
     */
    public void recordSuccessfulAuth(UserProfile user, Mat faceImage, double confidence, 
                                    String userDataDir) {
        if (user == null || faceImage == null || faceImage.empty()) {
            return;
        }
        
        // Only learn from high-confidence matches
        if (confidence < MIN_CONFIDENCE_FOR_LEARNING) {
            logger.debug("Confidence {} too low for learning (min: {})", 
                        confidence, MIN_CONFIDENCE_FOR_LEARNING);
            return;
        }
        
        // Update user's average confidence
        user.updateConfidence(confidence);
        user.recordSuccess(confidence);
        
        // Retrain if confidence is very high (user's appearance may have changed slightly)
        if (confidence >= RETRAIN_THRESHOLD) {
            logger.info("High confidence match ({}). Considering retraining for user {}", 
                       confidence, user.getUsername());
            considerRetraining(user, faceImage, userDataDir);
        }
        
        logger.debug("Recorded successful auth for user {} with confidence {}", 
                    user.getUsername(), confidence);
    }
    
    /**
     * Considers retraining by adding new reference image if appropriate.
     */
    private void considerRetraining(UserProfile user, Mat faceImage, String userDataDir) {
        try {
            List<String> currentImages = user.getFaceImagePaths();
            
            // Don't add too many images
            if (currentImages.size() >= MAX_REFERENCE_IMAGES) {
                // Replace oldest image (simple strategy - could be improved)
                String oldestImage = currentImages.get(0);
                File oldFile = new File(oldestImage);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                currentImages.remove(0);
            }
            
            // Save new reference image
            String timestamp = String.valueOf(System.currentTimeMillis());
            String newImagePath = userDataDir + File.separator + 
                                "learned_" + timestamp + ".jpg";
            
            boolean saved = Imgcodecs.imwrite(newImagePath, faceImage);
            if (saved) {
                currentImages.add(newImagePath);
                user.setFaceImagePaths(currentImages);
                logger.info("Added new reference image for adaptive learning: {}", newImagePath);
            } else {
                logger.warn("Failed to save learned image for user {}", user.getUsername());
            }
            
        } catch (Exception e) {
            logger.error("Error during adaptive retraining", e);
        }
    }
    
    /**
     * Calculates confidence score with adaptive threshold based on user's history.
     */
    public double calculateAdaptiveConfidence(double rawSimilarity, UserProfile user) {
        if (user == null) {
            return rawSimilarity;
        }
        
        // Adjust threshold based on user's average confidence
        // Users with consistently high confidence get slightly lower threshold
        // Users with lower confidence need higher threshold
        
        double userAvgConfidence = user.getAverageConfidence();
        double adaptiveThreshold = 0.6; // Base threshold
        
        if (userAvgConfidence > 0.85) {
            // Very reliable user - slightly lower threshold
            adaptiveThreshold = 0.55;
        } else if (userAvgConfidence > 0.75) {
            // Reliable user - standard threshold
            adaptiveThreshold = 0.60;
        } else if (userAvgConfidence > 0.65) {
            // Moderate reliability - slightly higher threshold
            adaptiveThreshold = 0.65;
        } else {
            // Lower reliability - higher threshold
            adaptiveThreshold = 0.70;
        }
        
        // Return confidence score (0.0 to 1.0)
        // If similarity is above threshold, scale it to confidence
        if (rawSimilarity >= adaptiveThreshold) {
            // Map [threshold, 1.0] to [0.5, 1.0]
            double normalized = 0.5 + 0.5 * ((rawSimilarity - adaptiveThreshold) / (1.0 - adaptiveThreshold));
            return Math.min(1.0, normalized);
        } else {
            // Below threshold - low confidence
            return rawSimilarity * 0.5; // Scale down
        }
    }
    
    /**
     * Handles appearance changes (glasses, beard, aging) by updating reference images.
     */
    public void handleAppearanceChange(UserProfile user, Mat newFaceImage, String userDataDir) {
        logger.info("Handling appearance change for user {}", user.getUsername());
        
        // Add new image to reference set
        considerRetraining(user, newFaceImage, userDataDir);
        
        // Could implement more sophisticated change detection here
        // For now, we rely on the retraining mechanism
    }
}

