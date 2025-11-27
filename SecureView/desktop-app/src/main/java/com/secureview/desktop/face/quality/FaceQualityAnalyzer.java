package com.secureview.desktop.face.quality;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzes face image quality for optimal registration.
 * Provides scoring for lighting, angle, clarity, and overall quality.
 */
public class FaceQualityAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(FaceQualityAnalyzer.class);
    
    /**
     * Quality score result containing all metrics.
     */
    public static class QualityScore {
        public double overallScore; // 0.0 to 1.0
        public double lightingScore; // 0.0 to 1.0
        public double angleScore; // 0.0 to 1.0
        public double clarityScore; // 0.0 to 1.0
        public double sizeScore; // 0.0 to 1.0
        public String feedback; // Human-readable feedback
        public boolean isOptimal; // True if quality is good enough for capture
        
        public QualityScore() {
            overallScore = 0.0;
            lightingScore = 0.0;
            angleScore = 0.0;
            clarityScore = 0.0;
            sizeScore = 0.0;
            feedback = "";
            isOptimal = false;
        }
    }
    
    /**
     * Analyzes face image quality and returns detailed scores.
     */
    public QualityScore analyzeQuality(Mat faceImage) {
        QualityScore score = new QualityScore();
        
        if (faceImage == null || faceImage.empty()) {
            score.feedback = "No face detected";
            return score;
        }
        
        try {
            // Analyze lighting
            score.lightingScore = analyzeLighting(faceImage);
            
            // Analyze angle (face orientation)
            score.angleScore = analyzeAngle(faceImage);
            
            // Analyze clarity (blur detection)
            score.clarityScore = analyzeClarity(faceImage);
            
            // Analyze size (face should be large enough)
            score.sizeScore = analyzeSize(faceImage);
            
            // Calculate overall score (weighted average)
            score.overallScore = (
                score.lightingScore * 0.25 +
                score.angleScore * 0.25 +
                score.clarityScore * 0.30 +
                score.sizeScore * 0.20
            );
            
            // Determine if optimal (threshold: 0.7)
            score.isOptimal = score.overallScore >= 0.7;
            
            // Generate feedback
            score.feedback = generateFeedback(score);
            
        } catch (Exception e) {
            logger.error("Error analyzing face quality", e);
            score.feedback = "Error analyzing quality";
        }
        
        return score;
    }
    
    /**
     * Analyzes lighting conditions (brightness and contrast).
     */
    private double analyzeLighting(Mat faceImage) {
        try {
            // Convert to grayscale if needed
            Mat gray = new Mat();
            if (faceImage.channels() == 3) {
                Imgproc.cvtColor(faceImage, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = faceImage;
            }
            
            // Calculate mean brightness
            Scalar mean = Core.mean(gray);
            double brightness = mean.val[0] / 255.0;
            
            // Ideal brightness is around 0.4-0.6 (not too dark, not too bright)
            double brightnessScore = 1.0 - Math.abs(brightness - 0.5) * 2.0;
            brightnessScore = Math.max(0.0, Math.min(1.0, brightnessScore));
            
            // Calculate contrast (standard deviation)
            Mat stddev = new Mat();
            Core.meanStdDev(gray, new Mat(), stddev);
            double[] stddevVal = new double[1];
            stddev.get(0, 0, stddevVal);
            double contrast = stddevVal[0] / 255.0;
            
            // Good contrast is > 0.2
            double contrastScore = Math.min(1.0, contrast / 0.2);
            
            gray.release();
            stddev.release();
            
            // Combined lighting score
            return (brightnessScore * 0.6 + contrastScore * 0.4);
            
        } catch (Exception e) {
            logger.warn("Error analyzing lighting", e);
            return 0.5; // Default score
        }
    }
    
    /**
     * Analyzes face angle (how frontal the face is).
     */
    private double analyzeAngle(Mat faceImage) {
        try {
            // For simplicity, we assume frontal faces are more symmetric
            // In a real implementation, you'd use face landmarks
            
            // Convert to grayscale
            Mat gray = new Mat();
            if (faceImage.channels() == 3) {
                Imgproc.cvtColor(faceImage, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = faceImage;
            }
            
            // Calculate symmetry (compare left and right halves)
            int width = gray.cols();
            int height = gray.rows();
            int midX = width / 2;
            
            Mat leftHalf = new Mat(gray, new Rect(0, 0, midX, height));
            Mat rightHalf = new Mat(gray, new Rect(midX, 0, width - midX, height));
            
            // Flip right half and compare
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
            gray.release();
            
            return symmetry;
            
        } catch (Exception e) {
            logger.warn("Error analyzing angle", e);
            return 0.7; // Default score (assume frontal)
        }
    }
    
    /**
     * Analyzes image clarity (blur detection using Laplacian variance).
     */
    private double analyzeClarity(Mat faceImage) {
        try {
            // Convert to grayscale
            Mat gray = new Mat();
            if (faceImage.channels() == 3) {
                Imgproc.cvtColor(faceImage, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = faceImage;
            }
            
            // Apply Laplacian filter for blur detection
            Mat laplacian = new Mat();
            Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);
            
            // Calculate variance (higher variance = sharper image)
            Mat mean = new Mat();
            Mat stddev = new Mat();
            Core.meanStdDev(laplacian, mean, stddev);
            
            double[] variance = new double[1];
            stddev.get(0, 0, variance);
            double varianceValue = variance[0] * variance[0];
            
            // Normalize (good clarity is variance > 100)
            double clarityScore = Math.min(1.0, varianceValue / 100.0);
            
            gray.release();
            laplacian.release();
            mean.release();
            stddev.release();
            
            return clarityScore;
            
        } catch (Exception e) {
            logger.warn("Error analyzing clarity", e);
            return 0.7; // Default score
        }
    }
    
    /**
     * Analyzes face size (should be large enough for good recognition).
     */
    private double analyzeSize(Mat faceImage) {
        try {
            int width = faceImage.cols();
            int height = faceImage.rows();
            int area = width * height;
            
            // Ideal face size is around 100x100 to 200x200 pixels
            // Minimum acceptable: 80x80 (6400 pixels)
            // Optimal: 120x120 (14400 pixels)
            
            double minArea = 6400.0;
            double optimalArea = 14400.0;
            
            if (area < minArea) {
                return area / minArea; // 0.0 to 1.0
            } else if (area >= optimalArea) {
                return 1.0;
            } else {
                // Between min and optimal
                return 0.5 + 0.5 * ((area - minArea) / (optimalArea - minArea));
            }
            
        } catch (Exception e) {
            logger.warn("Error analyzing size", e);
            return 0.8; // Default score
        }
    }
    
    /**
     * Generates human-readable feedback based on quality scores.
     */
    private String generateFeedback(QualityScore score) {
        StringBuilder feedback = new StringBuilder();
        
        if (score.isOptimal) {
            feedback.append("✓ Perfect! Ready to capture.");
        } else {
            if (score.lightingScore < 0.6) {
                if (score.lightingScore < 0.3) {
                    feedback.append("⚠ Too dark. Move to better lighting. ");
                } else {
                    feedback.append("⚠ Lighting could be better. ");
                }
            }
            
            if (score.angleScore < 0.6) {
                feedback.append("⚠ Face not centered. Look straight at camera. ");
            }
            
            if (score.clarityScore < 0.6) {
                feedback.append("⚠ Image is blurry. Hold still. ");
            }
            
            if (score.sizeScore < 0.6) {
                feedback.append("⚠ Move closer to camera. ");
            }
        }
        
        if (feedback.length() == 0) {
            feedback.append("Position your face in front of the camera.");
        }
        
        return feedback.toString().trim();
    }
}



