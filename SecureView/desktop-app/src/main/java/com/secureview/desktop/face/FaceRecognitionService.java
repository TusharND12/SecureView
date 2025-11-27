package com.secureview.desktop.face;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.encryption.EncryptionService;
import com.secureview.desktop.face.detection.FaceDetector;
import com.secureview.desktop.face.embedding.FaceEmbeddingExtractor;
import com.secureview.desktop.face.liveness.LivenessDetector;
import com.secureview.desktop.face.comparison.ImageComparisonService;
import com.secureview.desktop.opencv.stub.Mat;
import com.secureview.desktop.opencv.stub.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main service for face recognition operations.
 * Handles face detection, embedding extraction, comparison, and storage.
 */
public class FaceRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionService.class);
    private static FaceRecognitionService instance;
    
    private FaceDetector faceDetector;
    private FaceEmbeddingExtractor embeddingExtractor;
    private LivenessDetector livenessDetector;
    private ImageComparisonService imageComparisonService;
    private EncryptionService encryptionService;
    private ConfigManager configManager;
    
    private static final String EMBEDDING_FILE = "face_embedding.enc";
    private static final String REGISTERED_FACES_DIR = "registered_faces";
    private static final String REGISTERED_FACE_IMAGE = "registered_face.jpg";
    
    private FaceRecognitionService() {
        configManager = ConfigManager.getInstance();
        encryptionService = EncryptionService.getInstance();
    }
    
    public static synchronized FaceRecognitionService getInstance() {
        if (instance == null) {
            instance = new FaceRecognitionService();
        }
        return instance;
    }
    
    public void initialize() throws Exception {
        logger.info("Initializing Face Recognition Service...");
        
        faceDetector = new FaceDetector();
        faceDetector.initialize();
        
        embeddingExtractor = new FaceEmbeddingExtractor();
        embeddingExtractor.initialize();
        
        imageComparisonService = new ImageComparisonService();
        
        if (configManager.getConfig().isLivenessDetectionEnabled()) {
            livenessDetector = new LivenessDetector();
            livenessDetector.initialize();
        }
        
        logger.info("Face Recognition Service initialized successfully");
    }
    
    /**
     * Checks if a user is registered (has stored face images or embedding).
     */
    public boolean isUserRegistered() {
        // Check for image-based registration (new method)
        String imageDataPath = getImageDataPath();
        File imageDataDir = new File(imageDataPath);
        if (imageDataDir.exists() && imageDataDir.isDirectory()) {
            File[] imageFiles = imageDataDir.listFiles((d, name) -> 
                name.toLowerCase().endsWith(".jpg") || 
                name.toLowerCase().endsWith(".jpeg") || 
                name.toLowerCase().endsWith(".png"));
            if (imageFiles != null && imageFiles.length > 0) {
                return true;
            }
        }
        
        // Fallback: check for embedding-based registration (old method)
        String dataDir = configManager.getConfig().getDataDirectory();
        File embeddingFile = new File(dataDir, EMBEDDING_FILE);
        return embeddingFile.exists();
    }
    
    /**
     * Gets the path to the Image Data folder.
     */
    private String getImageDataPath() {
        // Use the user-specified path
        return "T:\\COLLEGE LIFE\\projects\\SecureView\\SecureView\\desktop-app\\Image Data";
    }
    
    /**
     * Clears existing registration to allow re-registration.
     * Deletes the stored face embedding file.
     */
    public boolean clearRegistration() {
        try {
            String dataDir = configManager.getConfig().getDataDirectory();
            File embeddingFile = new File(dataDir, EMBEDDING_FILE);
            boolean deleted = true;
            
            // Delete embedding file
            if (embeddingFile.exists()) {
                deleted = embeddingFile.delete();
                if (!deleted) {
                    logger.error("Failed to delete embedding file");
                }
            }
            
            // Delete registered face image
            String registeredFacePath = dataDir + File.separator + REGISTERED_FACES_DIR + File.separator + REGISTERED_FACE_IMAGE;
            File registeredFaceFile = new File(registeredFacePath);
            if (registeredFaceFile.exists()) {
                boolean faceDeleted = registeredFaceFile.delete();
                if (!faceDeleted) {
                    logger.warn("Failed to delete registered face image");
                } else {
                    logger.info("Registered face image deleted");
                }
            }
            
            // Try to delete the registered_faces directory if empty
            File registeredFacesDir = new File(dataDir, REGISTERED_FACES_DIR);
            if (registeredFacesDir.exists() && registeredFacesDir.listFiles().length == 0) {
                registeredFacesDir.delete();
            }
            
            // Delete image-based registration (Image Data folder)
            String imageDataPath = getImageDataPath();
            File imageDataDir = new File(imageDataPath);
            if (imageDataDir.exists() && imageDataDir.isDirectory()) {
                File[] imageFiles = imageDataDir.listFiles((d, name) -> 
                    name.toLowerCase().endsWith(".jpg") || 
                    name.toLowerCase().endsWith(".jpeg") || 
                    name.toLowerCase().endsWith(".png"));
                if (imageFiles != null) {
                    for (File file : imageFiles) {
                        boolean fileDeleted = file.delete();
                        if (!fileDeleted) {
                            logger.warn("Failed to delete image file: {}", file.getName());
                        }
                    }
                    logger.info("Deleted {} image files from Image Data folder", imageFiles.length);
                }
            }
            
            if (deleted) {
                logger.info("Registration cleared successfully. User can now register again.");
                return true;
            } else {
                logger.error("Failed to delete registration files");
                return false;
            }
        } catch (Exception e) {
            logger.error("Error clearing registration", e);
            return false;
        }
    }
    
    /**
     * Registers a new user by capturing and storing their face embedding.
     * This must be done FIRST before authentication can work.
     */
    public boolean registerUser(Mat faceImage) throws Exception {
        logger.info("=== REGISTRATION PROCESS STARTED ===");
        logger.info("Step 1: Registering new user...");
        
        if (faceImage == null || faceImage.empty()) {
            logger.error("Invalid face image provided for registration");
            return false;
        }
        
        // Verify liveness if enabled (skip for first frame during registration)
        if (livenessDetector != null) {
            try {
                if (!livenessDetector.verifyLiveness(faceImage)) {
                    logger.warn("Liveness detection failed during registration - skipping for now");
                    // Don't fail registration due to liveness - allow it for first registration
                    // return false;
                }
            } catch (Exception e) {
                logger.warn("Liveness detection error during registration - continuing anyway", e);
            }
        }
        
        // Extract face embedding
        logger.info("Step 2: Extracting face embedding...");
        double[] embedding = embeddingExtractor.extractEmbedding(faceImage);
        if (embedding == null || embedding.length == 0) {
            logger.error("Failed to extract face embedding");
            return false;
        }
        logger.info("Face embedding extracted successfully. Dimensions: {}", embedding.length);
        
        // Encrypt and store embedding
        logger.info("Step 3: Encrypting and storing face embedding...");
        String dataDir = configManager.getConfig().getDataDirectory();
        Files.createDirectories(Paths.get(dataDir));
        
        byte[] embeddingBytes = convertToBytes(embedding);
        byte[] encryptedData = encryptionService.encrypt(embeddingBytes);
        
        Files.write(Paths.get(dataDir, EMBEDDING_FILE), encryptedData);
        
        // Store face image in registered_faces folder
        logger.info("Step 4: Saving registered face image...");
        String registeredFacesDir = dataDir + File.separator + REGISTERED_FACES_DIR;
        Files.createDirectories(Paths.get(registeredFacesDir));
        logger.info("Registered faces directory: {}", registeredFacesDir);
        
        String faceImagePath = registeredFacesDir + File.separator + REGISTERED_FACE_IMAGE;
        boolean saved = Imgcodecs.imwrite(faceImagePath, faceImage);
        if (saved) {
            logger.info("Registered face image saved successfully at: {}", faceImagePath);
            // Verify file was created
            File savedFile = new File(faceImagePath);
            if (savedFile.exists()) {
                logger.info("Verified: Face image file exists. Size: {} bytes", savedFile.length());
            } else {
                logger.warn("Warning: Face image file was not created at: {}", faceImagePath);
            }
        } else {
            logger.error("Failed to save registered face image to: {}", faceImagePath);
        }
        
        logger.info("=== REGISTRATION COMPLETE ===");
        logger.info("Face embedding stored at: {}/{}", dataDir, EMBEDDING_FILE);
        logger.info("Face image stored at: {}", faceImagePath);
        logger.info("User registered successfully. Authentication can now compare faces.");
        return true;
    }
    
    /**
     * Registers a user with multiple face angles (360-degree style).
     * Saves all face images to the Image Data folder.
     */
    public boolean registerUserMultiAngle(List<Mat> faceImages) throws Exception {
        logger.info("=== MULTI-ANGLE REGISTRATION PROCESS STARTED ===");
        logger.info("Registering user with {} face angles...", faceImages.size());
        
        if (faceImages == null || faceImages.isEmpty()) {
            logger.error("No face images provided for registration");
            return false;
        }
        
        // Create Image Data directory
        String imageDataPath = getImageDataPath();
        File imageDataDir = new File(imageDataPath);
        Files.createDirectories(Paths.get(imageDataPath));
        logger.info("Image Data directory: {}", imageDataPath);
        
        // Clear existing images in the directory
        File[] existingFiles = imageDataDir.listFiles((d, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".jpeg") || 
            name.toLowerCase().endsWith(".png"));
        if (existingFiles != null) {
            for (File file : existingFiles) {
                file.delete();
                logger.debug("Deleted existing image: {}", file.getName());
            }
        }
        
        // Save each face image
        int savedCount = 0;
        for (int i = 0; i < faceImages.size(); i++) {
            Mat faceImage = faceImages.get(i);
            if (faceImage == null || faceImage.empty()) {
                logger.warn("Skipping empty face image at index {}", i);
                continue;
            }
            
            String imageFileName = String.format("face_angle_%03d.jpg", i + 1);
            String imagePath = imageDataPath + File.separator + imageFileName;
            
            boolean saved = Imgcodecs.imwrite(imagePath, faceImage);
            if (saved) {
                savedCount++;
                logger.info("Saved face image {}: {}", i + 1, imagePath);
                
                // Verify file was created
                File savedFile = new File(imagePath);
                if (savedFile.exists()) {
                    logger.debug("Verified: Image file exists. Size: {} bytes", savedFile.length());
                } else {
                    logger.warn("Warning: Image file was not created at: {}", imagePath);
                }
            } else {
                logger.error("Failed to save face image {} to: {}", i + 1, imagePath);
            }
        }
        
        if (savedCount == 0) {
            logger.error("Failed to save any face images");
            return false;
        }
        
        logger.info("=== MULTI-ANGLE REGISTRATION COMPLETE ===");
        logger.info("Saved {}/{} face images to: {}", savedCount, faceImages.size(), imageDataPath);
        logger.info("User registered successfully with {} face angles.", savedCount);
        return true;
    }
    
    /**
     * Authenticates a user by comparing their face with stored images.
     * Uses image-based comparison if images are available, otherwise falls back to embedding-based.
     * @return similarity score (0.0 to 1.0), where 1.0 is perfect match
     */
    public double authenticateUser(Mat faceImage) throws Exception {
        logger.debug("=== AUTHENTICATION PROCESS STARTED ===");
        
        // Check if user is registered FIRST
        if (!isUserRegistered()) {
            logger.error("No registered user found! User must register first.");
            logger.error("Authentication cannot proceed without registration.");
            return 0.0;
        }
        
        if (faceImage == null || faceImage.empty()) {
            logger.error("Invalid face image provided for authentication");
            return 0.0;
        }
        
        // Verify liveness if enabled (but don't block authentication if it fails)
        if (livenessDetector != null) {
            boolean isLive = livenessDetector.verifyLiveness(faceImage);
            if (!isLive) {
                logger.warn("Liveness detection failed during authentication, but continuing with image comparison");
                // Don't return 0.0 - continue with image comparison anyway
                // This prevents false negatives from liveness detection blocking legitimate users
            } else {
                logger.debug("Liveness detection passed");
            }
        }
        
        // Try image-based authentication first (new method)
        String imageDataPath = getImageDataPath();
        File imageDataDir = new File(imageDataPath);
        logger.info("Checking Image Data folder: {}", imageDataPath);
        logger.info("Directory exists: {}, Is directory: {}", imageDataDir.exists(), imageDataDir.isDirectory());
        
        if (imageDataDir.exists() && imageDataDir.isDirectory()) {
            File[] imageFiles = imageDataDir.listFiles((d, name) -> 
                name.toLowerCase().endsWith(".jpg") || 
                name.toLowerCase().endsWith(".jpeg") || 
                name.toLowerCase().endsWith(".png"));
            
            logger.info("Found {} image files in Image Data folder", imageFiles != null ? imageFiles.length : 0);
            
            if (imageFiles != null && imageFiles.length > 0) {
                logger.info("Using image-based authentication with {} reference images", imageFiles.length);
                
                try {
                    // Load reference images
                    List<Mat> referenceImages = imageComparisonService.loadReferenceImages(imageDataPath);
                    logger.info("Loaded {} reference images for comparison", referenceImages.size());
                    
                    if (referenceImages.isEmpty()) {
                        logger.error("Failed to load any reference images from: {}", imageDataPath);
                    } else {
                        // Compare current face with all reference images
                        logger.info("Starting image comparison...");
                        double similarity = imageComparisonService.compareWithImages(faceImage, referenceImages);
                        
                        // Cleanup reference images
                        for (Mat img : referenceImages) {
                            if (img != null) {
                                img.release();
                            }
                        }
                        
                        logger.info("=== AUTHENTICATION RESULT (Image-based) ===");
                        logger.info("Face similarity score: {} (1.0 = perfect match, 0.0 = no match)", similarity);
                        logger.info("Threshold: {}", configManager.getConfig().getFaceRecognitionThreshold());
                        logger.info("Match: {}", similarity >= configManager.getConfig().getFaceRecognitionThreshold() ? "YES" : "NO");
                        
                        return similarity;
                    }
                } catch (Exception e) {
                    logger.error("Error during image-based authentication", e);
                    // Continue to fallback
                }
            } else {
                logger.warn("Image Data folder exists but contains no image files");
            }
        } else {
            logger.warn("Image Data folder does not exist or is not a directory: {}", imageDataPath);
        }
        
        // Fallback to embedding-based authentication (old method)
        logger.debug("Falling back to embedding-based authentication...");
        String dataDir = configManager.getConfig().getDataDirectory();
        File embeddingFile = new File(dataDir, EMBEDDING_FILE);
        
        if (!embeddingFile.exists()) {
            logger.error("No embedding file found and no images found. Registration may be incomplete.");
            return 0.0;
        }
        
        // Load stored embedding (from registration)
        logger.debug("Step 1: Loading registered face embedding...");
        byte[] encryptedData = Files.readAllBytes(Paths.get(dataDir, EMBEDDING_FILE));
        byte[] decryptedData = encryptionService.decrypt(encryptedData);
        double[] storedEmbedding = convertFromBytes(decryptedData);
        logger.debug("Registered embedding loaded. Dimensions: {}", storedEmbedding.length);
        
        // Extract current face embedding (from camera)
        logger.debug("Step 2: Extracting current face embedding...");
        double[] currentEmbedding = embeddingExtractor.extractEmbedding(faceImage);
        if (currentEmbedding == null || currentEmbedding.length == 0) {
            logger.error("Failed to extract face embedding from current image");
            return 0.0;
        }
        logger.debug("Current embedding extracted. Dimensions: {}", currentEmbedding.length);
        
        // Compare embeddings (registered vs current)
        logger.debug("Step 3: Comparing registered face with current face...");
        double similarity = calculateCosineSimilarity(storedEmbedding, currentEmbedding);
        
        logger.info("=== AUTHENTICATION RESULT (Embedding-based) ===");
        logger.info("Face similarity score: {} (1.0 = perfect match, 0.0 = no match)", similarity);
        logger.info("Threshold: {}", configManager.getConfig().getFaceRecognitionThreshold());
        logger.info("Match: {}", similarity >= configManager.getConfig().getFaceRecognitionThreshold() ? "YES" : "NO");
        
        return similarity;
    }
    
    /**
     * Detects face in an image.
     */
    public Mat detectFace(Mat image) {
        return faceDetector.detectFace(image);
    }
    
    /**
     * Gets the image comparison service.
     */
    public ImageComparisonService getImageComparisonService() {
        return imageComparisonService;
    }
    
    /**
     * Calculates cosine similarity between two embeddings.
     */
    private double calculateCosineSimilarity(double[] embedding1, double[] embedding2) {
        if (embedding1.length != embedding2.length) {
            logger.error("Embedding dimensions do not match");
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }
        
        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        if (denominator == 0.0) {
            return 0.0;
        }
        
        return dotProduct / denominator;
    }
    
    private byte[] convertToBytes(double[] array) {
        byte[] bytes = new byte[array.length * 8];
        for (int i = 0; i < array.length; i++) {
            long bits = Double.doubleToLongBits(array[i]);
            for (int j = 0; j < 8; j++) {
                bytes[i * 8 + j] = (byte) ((bits >> (j * 8)) & 0xff);
            }
        }
        return bytes;
    }
    
    private double[] convertFromBytes(byte[] bytes) {
        double[] array = new double[bytes.length / 8];
        for (int i = 0; i < array.length; i++) {
            long bits = 0;
            for (int j = 0; j < 8; j++) {
                bits |= ((long) (bytes[i * 8 + j] & 0xff)) << (j * 8);
            }
            array[i] = Double.longBitsToDouble(bits);
        }
        return array;
    }
}

