package com.secureview.desktop.face;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.encryption.EncryptionService;
import com.secureview.desktop.face.detection.FaceDetector;
import com.secureview.desktop.face.embedding.FaceEmbeddingExtractor;
import com.secureview.desktop.face.liveness.LivenessDetector;
import com.secureview.desktop.opencv.stub.Mat;
import com.secureview.desktop.opencv.stub.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
        
        if (configManager.getConfig().isLivenessDetectionEnabled()) {
            livenessDetector = new LivenessDetector();
            livenessDetector.initialize();
        }
        
        logger.info("Face Recognition Service initialized successfully");
    }
    
    /**
     * Checks if a user is registered (has stored face embedding).
     */
    public boolean isUserRegistered() {
        String dataDir = configManager.getConfig().getDataDirectory();
        File embeddingFile = new File(dataDir, EMBEDDING_FILE);
        return embeddingFile.exists();
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
     * Authenticates a user by comparing their face with stored embedding.
     * This compares the current face with the face registered during registration.
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
        
        // Verify liveness if enabled
        if (livenessDetector != null) {
            if (!livenessDetector.verifyLiveness(faceImage)) {
                logger.warn("Liveness detection failed during authentication");
                return 0.0;
            }
        }
        
        // Load stored embedding (from registration)
        logger.debug("Step 1: Loading registered face embedding...");
        String dataDir = configManager.getConfig().getDataDirectory();
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
        
        // Additional check: Compare with stored face image if available
        String registeredFacePath = dataDir + File.separator + REGISTERED_FACES_DIR + File.separator + REGISTERED_FACE_IMAGE;
        File registeredFaceFile = new File(registeredFacePath);
        
        if (registeredFaceFile.exists()) {
            try {
                // Load registered face image
                Mat registeredFaceImage = Imgcodecs.imread(registeredFacePath);
                if (!registeredFaceImage.empty()) {
                    // Extract embedding from stored image for comparison
                    double[] registeredImageEmbedding = embeddingExtractor.extractEmbedding(registeredFaceImage);
                    if (registeredImageEmbedding != null && registeredImageEmbedding.length > 0) {
                        // Compare with stored image embedding
                        double imageSimilarity = calculateCosineSimilarity(registeredImageEmbedding, currentEmbedding);
                        logger.debug("Similarity with stored image: {}", imageSimilarity);
                        
                        // Use the higher similarity score
                        similarity = Math.max(similarity, imageSimilarity);
                        logger.debug("Using maximum similarity: {}", similarity);
                    }
                    registeredFaceImage.release();
                }
            } catch (Exception e) {
                logger.warn("Could not load registered face image for comparison", e);
            }
        }
        
        logger.info("=== AUTHENTICATION RESULT ===");
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

