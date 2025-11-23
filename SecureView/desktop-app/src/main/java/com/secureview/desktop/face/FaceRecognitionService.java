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
     * Registers a new user by capturing and storing their face embedding.
     */
    public boolean registerUser(Mat faceImage) throws Exception {
        logger.info("Registering new user...");
        
        // Verify liveness if enabled
        if (livenessDetector != null) {
            if (!livenessDetector.verifyLiveness(faceImage)) {
                logger.warn("Liveness detection failed during registration");
                return false;
            }
        }
        
        // Extract face embedding
        double[] embedding = embeddingExtractor.extractEmbedding(faceImage);
        if (embedding == null || embedding.length == 0) {
            logger.error("Failed to extract face embedding");
            return false;
        }
        
        // Encrypt and store embedding
        String dataDir = configManager.getConfig().getDataDirectory();
        Files.createDirectories(Paths.get(dataDir));
        
        byte[] embeddingBytes = convertToBytes(embedding);
        byte[] encryptedData = encryptionService.encrypt(embeddingBytes);
        
        Files.write(Paths.get(dataDir, EMBEDDING_FILE), encryptedData);
        
        logger.info("User registered successfully");
        return true;
    }
    
    /**
     * Authenticates a user by comparing their face with stored embedding.
     * @return similarity score (0.0 to 1.0), where 1.0 is perfect match
     */
    public double authenticateUser(Mat faceImage) throws Exception {
        if (!isUserRegistered()) {
            logger.warn("No registered user found");
            return 0.0;
        }
        
        // Verify liveness if enabled
        if (livenessDetector != null) {
            if (!livenessDetector.verifyLiveness(faceImage)) {
                logger.warn("Liveness detection failed during authentication");
                return 0.0;
            }
        }
        
        // Load stored embedding
        String dataDir = configManager.getConfig().getDataDirectory();
        byte[] encryptedData = Files.readAllBytes(Paths.get(dataDir, EMBEDDING_FILE));
        byte[] decryptedData = encryptionService.decrypt(encryptedData);
        double[] storedEmbedding = convertFromBytes(decryptedData);
        
        // Extract current face embedding
        double[] currentEmbedding = embeddingExtractor.extractEmbedding(faceImage);
        if (currentEmbedding == null || currentEmbedding.length == 0) {
            logger.error("Failed to extract face embedding");
            return 0.0;
        }
        
        // Compare embeddings
        double similarity = calculateCosineSimilarity(storedEmbedding, currentEmbedding);
        logger.info("Face similarity score: {}", similarity);
        
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

