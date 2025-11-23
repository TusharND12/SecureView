package com.secureview.desktop.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

/**
 * Handles encryption and decryption of sensitive data (face embeddings).
 * Uses AES-256-GCM for authenticated encryption.
 */
public class EncryptionService {
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private static EncryptionService instance;
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    private SecretKey secretKey;
    private static final String KEY_FILE = "encryption.key";
    
    private EncryptionService() {
        loadOrGenerateKey();
    }
    
    public static synchronized EncryptionService getInstance() {
        if (instance == null) {
            instance = new EncryptionService();
        }
        return instance;
    }
    
    /**
     * Loads encryption key from file or generates a new one.
     */
    private void loadOrGenerateKey() {
        try {
            String userHome = System.getProperty("user.home");
            String keyPath = userHome + File.separator + ".secureview" + File.separator + KEY_FILE;
            File keyFile = new File(keyPath);
            
            if (keyFile.exists()) {
                // Load existing key
                byte[] keyBytes = Files.readAllBytes(Paths.get(keyPath));
                secretKey = new SecretKeySpec(keyBytes, "AES");
                logger.info("Encryption key loaded from file");
            } else {
                // Generate new key
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(KEY_SIZE, new SecureRandom());
                secretKey = keyGenerator.generateKey();
                
                // Save key
                keyFile.getParentFile().mkdirs();
                Files.write(Paths.get(keyPath), secretKey.getEncoded());
                logger.info("New encryption key generated and saved");
            }
        } catch (Exception e) {
            logger.error("Failed to load or generate encryption key", e);
            throw new RuntimeException("Encryption service initialization failed", e);
        }
    }
    
    /**
     * Encrypts data using AES-256-GCM.
     */
    public byte[] encrypt(byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        
        // Generate IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        // Encrypt
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        // Prepend IV to ciphertext
        byte[] encryptedData = new byte[GCM_IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
        System.arraycopy(ciphertext, 0, encryptedData, GCM_IV_LENGTH, ciphertext.length);
        
        return encryptedData;
    }
    
    /**
     * Decrypts data using AES-256-GCM.
     */
    public byte[] decrypt(byte[] encryptedData) throws Exception {
        if (encryptedData.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Encrypted data too short");
        }
        
        // Extract IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
        
        // Extract ciphertext
        byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
        
        // Decrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        
        return cipher.doFinal(ciphertext);
    }
}

