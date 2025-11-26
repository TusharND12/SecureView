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
        if (currentFace == null || currentFace.empty() || referenceImages == null || referenceImages.isEmpty()) {
            logger.warn("Invalid input for image comparison");
            return 0.0;
        }
        
        double bestSimilarity = 0.0;
        
        for (Mat referenceImage : referenceImages) {
            if (referenceImage == null || referenceImage.empty()) {
                continue;
            }
            
            try {
                double similarity = compareTwoImages(currentFace, referenceImage);
                bestSimilarity = Math.max(bestSimilarity, similarity);
                logger.debug("Image similarity: {}", similarity);
            } catch (Exception e) {
                logger.warn("Error comparing images", e);
            }
        }
        
        logger.info("Best similarity score from {} reference images: {}", referenceImages.size(), bestSimilarity);
        return bestSimilarity;
    }
    
    /**
     * Compares two face images using multiple methods and returns the best score.
     */
    private double compareTwoImages(Mat img1, Mat img2) {
        // Method 1: Template matching
        double templateScore = templateMatch(img1, img2);
        
        // Method 2: Histogram comparison
        double histogramScore = histogramCompare(img1, img2);
        
        // Method 3: Structural similarity (if available)
        double structuralScore = structuralSimilarity(img1, img2);
        
        // Use the best score
        double bestScore = Math.max(templateScore, Math.max(histogramScore, structuralScore));
        
        logger.debug("Comparison scores - Template: {}, Histogram: {}, Structural: {}, Best: {}", 
                    templateScore, histogramScore, structuralScore, bestScore);
        
        return bestScore;
    }
    
    /**
     * Template matching using OpenCV.
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
            
            // Perform template matching
            Mat result = new Mat();
            Imgproc.matchTemplate(grayImage, grayTemplate, result, Imgproc.TM_CCOEFF_NORMED);
            
            // Get the best match value
            double[] minVal = new double[1];
            double[] maxVal = new double[1];
            Core.minMaxLoc(result, minVal, maxVal);
            
            double similarity = maxVal[0]; // Normalized correlation coefficient (0-1)
            
            // Cleanup
            resizedTemplate.release();
            resizedImage.release();
            grayTemplate.release();
            grayImage.release();
            result.release();
            
            return similarity;
        } catch (Exception e) {
            logger.warn("Template matching failed", e);
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
            logger.warn("Reference images directory does not exist: {}", directoryPath);
            return images;
        }
        
        File[] imageFiles = dir.listFiles((d, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".jpeg") || 
            name.toLowerCase().endsWith(".png"));
        
        if (imageFiles == null) {
            return images;
        }
        
        for (File imageFile : imageFiles) {
            try {
                Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
                if (image != null && !image.empty()) {
                    images.add(image);
                    logger.debug("Loaded reference image: {}", imageFile.getName());
                } else {
                    logger.warn("Failed to load image: {}", imageFile.getName());
                }
            } catch (Exception e) {
                logger.warn("Error loading image: {}", imageFile.getName(), e);
            }
        }
        
        logger.info("Loaded {} reference images from {}", images.size(), directoryPath);
        return images;
    }
}

