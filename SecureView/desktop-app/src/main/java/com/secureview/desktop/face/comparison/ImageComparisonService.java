package com.secureview.desktop.face.comparison;

import com.secureview.desktop.opencv.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for comparing face images using OpenCV template matching and feature matching.
 * Used for image-based authentication instead of embedding-based.
 */
public class ImageComparisonService {
    private static final Logger logger = LoggerFactory.getLogger(ImageComparisonService.class);
    
    /**
     * Compares a face image with stored reference images.
     * @param currentFace Current face image to compare
     * @param referenceImages List of reference face images
     * @return Best similarity score (0.0 to 1.0)
     */
    public double compareWithImages(Mat currentFace, List<Mat> referenceImages) {
        if (currentFace == null || currentFace.empty()) {
            logger.error("Current face image is null or empty");
            return 0.0;
        }
        
        if (referenceImages == null || referenceImages.isEmpty()) {
            logger.error("Reference images list is null or empty");
            return 0.0;
        }
        
        logger.info("Comparing current face ({}x{}) with {} reference images", 
                   currentFace.cols(), currentFace.rows(), referenceImages.size());
        
        double bestSimilarity = 0.0;
        double bestTemplateScore = 0.0; // Track best template score separately
        int validComparisons = 0;
        
        for (int i = 0; i < referenceImages.size(); i++) {
            Mat referenceImage = referenceImages.get(i);
            if (referenceImage == null || referenceImage.empty()) {
                logger.warn("Reference image {} is null or empty, skipping", i);
                continue;
            }
            
            if (referenceImage.cols() == 0 || referenceImage.rows() == 0) {
                logger.warn("Reference image {} has invalid dimensions, skipping", i);
                continue;
            }
            
            try {
                logger.debug("Comparing with reference image {} ({}x{})", i, referenceImage.cols(), referenceImage.rows());
                
                // Get combined similarity score
                double similarity = compareTwoImages(currentFace, referenceImage);
                
                // Also get template score for strictness validation
                double templateScore = templateMatch(currentFace, referenceImage);
                
                bestSimilarity = Math.max(bestSimilarity, similarity);
                bestTemplateScore = Math.max(bestTemplateScore, templateScore);
                validComparisons++;
                
                logger.info("Reference image {} - Similarity: {} (template: {})", i, similarity, templateScore);
            } catch (Exception e) {
                logger.error("Error comparing with reference image {}", i, e);
            }
        }
        
        if (validComparisons == 0) {
            logger.error("No valid comparisons were performed!");
            return 0.0;
        }
        
        // Final strictness check: if template matching is very low, reduce overall score
        // This prevents false positives when other methods give high scores
        // But be more lenient to allow legitimate matches
        if (bestTemplateScore < 0.5) {
            // Only penalize if template is very low
            bestSimilarity = Math.min(bestSimilarity, bestTemplateScore * 0.95);
            logger.debug("Template score low ({}), slightly reducing overall similarity to {}", 
                        bestTemplateScore, bestSimilarity);
        }
        
        logger.info("Best similarity score from {} valid comparisons: {} (best template: {})", 
                   validComparisons, bestSimilarity, bestTemplateScore);
        return bestSimilarity;
    }
    
    /**
     * Compares two face images using multiple methods and returns a strict combined score.
     * Uses weighted average with higher weight on template matching for better accuracy.
     */
    private double compareTwoImages(Mat img1, Mat img2) {
        try {
            // Method 1: Template matching (normalized cross-correlation)
            double templateScore = templateMatch(img1, img2);
            
            // Method 2: Histogram comparison
            double histogramScore = histogramCompare(img1, img2);
            
            // Method 3: Structural similarity
            double structuralScore = structuralSimilarity(img1, img2);
            
            // Method 4: Direct pixel comparison (normalized)
            double pixelScore = pixelWiseCompare(img1, img2);
            
            // Use weighted average: 40% template, 30% histogram, 20% structural, 10% pixel
            double combinedScore = (templateScore * 0.4) + (histogramScore * 0.3) + 
                                   (structuralScore * 0.2) + (pixelScore * 0.1);
            
            // Additional strictness: require template matching to be at least 0.4
            // This prevents false matches when other methods give high scores
            // But not too strict to allow legitimate matches
            if (templateScore < 0.4) {
                combinedScore = Math.min(combinedScore, templateScore * 0.8); // Penalize if template is low
            }
            
            // If all scores are very low, use the best single score instead of weighted average
            // This helps when one method works better than others
            if (combinedScore < 0.3 && (templateScore > 0.3 || histogramScore > 0.3 || structuralScore > 0.3)) {
                combinedScore = Math.max(templateScore, Math.max(histogramScore, structuralScore));
                logger.debug("Using best single score instead of weighted average: {}", combinedScore);
            }
            
            logger.debug("Comparison scores - Template: {:.3f}, Histogram: {:.3f}, Structural: {:.3f}, Pixel: {:.3f}, Combined: {:.3f}", 
                        templateScore, histogramScore, structuralScore, pixelScore, combinedScore);
            
            return Math.max(0.0, Math.min(1.0, combinedScore)); // Clamp to [0, 1]
        } catch (Exception e) {
            logger.error("Error in compareTwoImages", e);
            return 0.0;
        }
    }
    
    /**
     * Pixel-wise comparison with normalization.
     */
    private double pixelWiseCompare(Mat img1, Mat img2) {
        try {
            // Resize to same size
            Mat resized1 = new Mat();
            Mat resized2 = new Mat();
            Size targetSize = new Size(160, 160);
            
            Imgproc.resize(img1, resized1, targetSize);
            Imgproc.resize(img2, resized2, targetSize);
            
            // Convert to grayscale
            Mat gray1 = new Mat();
            Mat gray2 = new Mat();
            
            if (resized1.channels() == 3) {
                Imgproc.cvtColor(resized1, gray1, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray1 = resized1;
            }
            
            if (resized2.channels() == 3) {
                Imgproc.cvtColor(resized2, gray2, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray2 = resized2;
            }
            
            // Convert to float and normalize
            Mat float1 = new Mat();
            Mat float2 = new Mat();
            gray1.convertTo(float1, CvType.CV_32F, 1.0 / 255.0);
            gray2.convertTo(float2, CvType.CV_32F, 1.0 / 255.0);
            
            Core.normalize(float1, float1, 0.0, 1.0, Core.NORM_MINMAX);
            Core.normalize(float2, float2, 0.0, 1.0, Core.NORM_MINMAX);
            
            // Calculate absolute difference
            Mat diff = new Mat();
            Core.absdiff(float1, float2, diff);
            
            // Calculate mean absolute difference
            Scalar meanDiff = Core.mean(diff);
            double similarity = 1.0 - Math.min(meanDiff.val[0], 1.0);
            
            // Cleanup
            resized1.release();
            resized2.release();
            gray1.release();
            gray2.release();
            float1.release();
            float2.release();
            diff.release();
            
            return Math.max(0.0, Math.min(1.0, similarity));
        } catch (Exception e) {
            logger.warn("Pixel-wise comparison failed", e);
            return 0.0;
        }
    }
    
    /**
     * Template matching using OpenCV.
     * For same-size images, uses normalized cross-correlation directly.
     */
    private double templateMatch(Mat template, Mat image) {
        try {
            // Resize both images to same size for comparison
            Mat resizedTemplate = new Mat();
            Mat resizedImage = new Mat();
            Size targetSize = new Size(160, 160);
            
            Imgproc.resize(template, resizedTemplate, targetSize);
            Imgproc.resize(image, resizedImage, targetSize);
            
            // Convert to grayscale if needed
            Mat grayTemplate = new Mat();
            Mat grayImage = new Mat();
            
            if (resizedTemplate.channels() == 3) {
                Imgproc.cvtColor(resizedTemplate, grayTemplate, Imgproc.COLOR_BGR2GRAY);
            } else {
                grayTemplate = resizedTemplate;
            }
            
            if (resizedImage.channels() == 3) {
                Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_BGR2GRAY);
            } else {
                grayImage = resizedImage;
            }
            
            // For same-size images, use a simpler approach: normalized difference
            // Convert to float
            Mat floatTemplate = new Mat();
            Mat floatImage = new Mat();
            grayTemplate.convertTo(floatTemplate, CvType.CV_32F, 1.0 / 255.0);
            grayImage.convertTo(floatImage, CvType.CV_32F, 1.0 / 255.0);
            
            // Normalize both images to [0, 1]
            Core.normalize(floatTemplate, floatTemplate, 0.0, 1.0, Core.NORM_MINMAX);
            Core.normalize(floatImage, floatImage, 0.0, 1.0, Core.NORM_MINMAX);
            
            // Calculate absolute difference
            Mat diff = new Mat();
            Core.absdiff(floatTemplate, floatImage, diff);
            
            // Calculate mean absolute difference
            Scalar meanDiff = Core.mean(diff);
            
            // Similarity is inverse of normalized difference
            // When images are identical, difference is 0, similarity is 1
            double similarity = 1.0 - Math.min(meanDiff.val[0], 1.0);
            
            // Cleanup
            diff.release();
            
            // Cleanup
            resizedTemplate.release();
            resizedImage.release();
            grayTemplate.release();
            grayImage.release();
            floatTemplate.release();
            floatImage.release();
            
            return Math.max(0.0, Math.min(1.0, similarity));
        } catch (Exception e) {
            logger.error("Template matching failed", e);
            return 0.0;
        }
    }
    
    /**
     * Histogram comparison using correlation.
     */
    private double histogramCompare(Mat img1, Mat img2) {
        try {
            // Convert to grayscale
            Mat gray1 = new Mat();
            Mat gray2 = new Mat();
            
            if (img1.channels() == 3) {
                Imgproc.cvtColor(img1, gray1, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray1 = img1;
            }
            
            if (img2.channels() == 3) {
                Imgproc.cvtColor(img2, gray2, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray2 = img2;
            }
            
            // Calculate histograms
            Mat hist1 = new Mat();
            Mat hist2 = new Mat();
            MatOfInt histSize = new MatOfInt(256);
            MatOfFloat ranges = new MatOfFloat(0f, 256f);
            MatOfInt channels = new MatOfInt(0);
            
            Imgproc.calcHist(java.util.Arrays.asList(gray1), channels, new Mat(), hist1, histSize, ranges);
            Imgproc.calcHist(java.util.Arrays.asList(gray2), channels, new Mat(), hist2, histSize, ranges);
            
            // Normalize histograms
            Core.normalize(hist1, hist1, 0.0, 1.0, Core.NORM_MINMAX);
            Core.normalize(hist2, hist2, 0.0, 1.0, Core.NORM_MINMAX);
            
            // Compare histograms using correlation
            double correlation = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CORREL);
            
            // Cleanup
            gray1.release();
            gray2.release();
            hist1.release();
            hist2.release();
            
            // Correlation ranges from -1 to 1, normalize to 0-1
            return (correlation + 1.0) / 2.0;
        } catch (Exception e) {
            logger.warn("Histogram comparison failed", e);
            return 0.0;
        }
    }
    
    /**
     * Structural similarity index (simplified version).
     */
    private double structuralSimilarity(Mat img1, Mat img2) {
        try {
            // Resize to same size
            Mat resized1 = new Mat();
            Mat resized2 = new Mat();
            Size targetSize = new Size(160, 160);
            
            Imgproc.resize(img1, resized1, targetSize);
            Imgproc.resize(img2, resized2, targetSize);
            
            // Convert to grayscale
            Mat gray1 = new Mat();
            Mat gray2 = new Mat();
            
            if (resized1.channels() == 3) {
                Imgproc.cvtColor(resized1, gray1, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray1 = resized1;
            }
            
            if (resized2.channels() == 3) {
                Imgproc.cvtColor(resized2, gray2, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray2 = resized2;
            }
            
            // Convert to float for calculations
            Mat float1 = new Mat();
            Mat float2 = new Mat();
            gray1.convertTo(float1, CvType.CV_32F, 1.0);
            gray2.convertTo(float2, CvType.CV_32F, 1.0);
            
            // Calculate mean
            Scalar mean1 = Core.mean(float1);
            Scalar mean2 = Core.mean(float2);
            
            // Calculate variance and covariance (simplified)
            Mat diff = new Mat();
            Core.subtract(float1, mean1, diff);
            Mat diff2 = new Mat();
            Core.subtract(float2, mean2, diff2);
            
            // Simplified SSIM calculation
            double meanDiff = Math.abs(mean1.val[0] - mean2.val[0]) / 255.0;
            double similarity = 1.0 - Math.min(meanDiff, 1.0);
            
            // Cleanup
            resized1.release();
            resized2.release();
            gray1.release();
            gray2.release();
            float1.release();
            float2.release();
            diff.release();
            diff2.release();
            
            return similarity;
        } catch (Exception e) {
            logger.warn("Structural similarity calculation failed", e);
            return 0.0;
        }
    }
    
    /**
     * Loads all reference images from a directory.
     */
    public List<Mat> loadReferenceImages(String directoryPath) {
        List<Mat> images = new ArrayList<>();
        File dir = new File(directoryPath);
        
        if (!dir.exists() || !dir.isDirectory()) {
            logger.error("Reference images directory does not exist: {}", directoryPath);
            return images;
        }
        
        File[] imageFiles = dir.listFiles((d, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".jpeg") || 
            name.toLowerCase().endsWith(".png"));
        
        if (imageFiles == null) {
            logger.error("Failed to list files in directory: {}", directoryPath);
            return images;
        }
        
        logger.info("Found {} image files to load", imageFiles.length);
        
        for (File imageFile : imageFiles) {
            try {
                logger.debug("Attempting to load image: {}", imageFile.getAbsolutePath());
                Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
                if (image != null && !image.empty() && image.cols() > 0 && image.rows() > 0) {
                    images.add(image);
                    logger.info("Successfully loaded reference image: {} ({}x{})", 
                               imageFile.getName(), image.cols(), image.rows());
                } else {
                    logger.error("Failed to load image (null or empty): {}", imageFile.getName());
                }
            } catch (Exception e) {
                logger.error("Error loading image: {}", imageFile.getName(), e);
            }
        }
        
        logger.info("Loaded {} reference images from {}", images.size(), directoryPath);
        if (images.isEmpty()) {
            logger.error("WARNING: No images were successfully loaded from {}", directoryPath);
        }
        return images;
    }
}



