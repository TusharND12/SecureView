package com.secureview.desktop.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.secureview.desktop.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles Firebase Cloud Messaging for sending intrusion alerts.
 */
public class FirebaseService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);
    private static FirebaseService instance;
    
    private FirebaseMessaging messaging;
    private ConfigManager configManager;
    private boolean initialized = false;
    
    private FirebaseService() {
        configManager = ConfigManager.getInstance();
    }
    
    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }
    
    /**
     * Initializes Firebase Admin SDK.
     */
    public void initialize() throws Exception {
        if (initialized) {
            logger.warn("Firebase already initialized");
            return;
        }
        
        String credentialsPath = configManager.getConfig().getFirebaseCredentialsPath();
        String projectId = configManager.getConfig().getFirebaseProjectId();
        
        if (credentialsPath == null || credentialsPath.isEmpty()) {
            logger.warn("Firebase credentials path not configured. FCM features will be disabled.");
            return;
        }
        
        if (projectId == null || projectId.isEmpty()) {
            logger.warn("Firebase project ID not configured. FCM features will be disabled.");
            return;
        }
        
        try {
            FileInputStream serviceAccount = new FileInputStream(credentialsPath);
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId(projectId)
                .build();
            
            // Initialize only if not already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            messaging = FirebaseMessaging.getInstance();
            initialized = true;
            
            logger.info("Firebase initialized successfully");
            
        } catch (FileNotFoundException e) {
            logger.error("Firebase credentials file not found: {}", credentialsPath, e);
            throw new Exception("Firebase credentials file not found", e);
        } catch (Exception e) {
            logger.error("Failed to initialize Firebase", e);
            throw new Exception("Failed to initialize Firebase", e);
        }
    }
    
    /**
     * Sends an intrusion alert with the intruder's image to the registered device.
     */
    public void sendIntrusionAlert(byte[] intruderImage, String timestamp, String attemptDetails) {
        if (!initialized) {
            logger.warn("Firebase not initialized. Cannot send intrusion alert.");
            return;
        }
        
        String deviceToken = configManager.getConfig().getDeviceToken();
        if (deviceToken == null || deviceToken.isEmpty()) {
            logger.warn("Device token not configured. Cannot send intrusion alert.");
            return;
        }
        
        try {
            // Encode image to Base64
            String imageBase64 = Base64.getEncoder().encodeToString(intruderImage);
            
            // Create notification
            Notification notification = Notification.builder()
                .setTitle("🚨 Intrusion Alert")
                .setBody("Unauthorized access attempt detected at " + timestamp)
                .build();
            
            // Create data payload
            Map<String, String> data = new HashMap<>();
            data.put("type", "intrusion");
            data.put("timestamp", timestamp);
            data.put("image", imageBase64);
            data.put("details", attemptDetails);
            data.put("action", "view_alert");
            
            // Build message
            Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
                .putAllData(data)
                .build();
            
            // Send message
            String response = messaging.send(message);
            logger.info("Intrusion alert sent successfully. Message ID: {}", response);
            
        } catch (Exception e) {
            logger.error("Failed to send intrusion alert", e);
        }
    }
    
    /**
     * Sends a test notification to verify FCM setup.
     */
    public void sendTestNotification() {
        if (!initialized) {
            logger.warn("Firebase not initialized. Cannot send test notification.");
            return;
        }
        
        String deviceToken = configManager.getConfig().getDeviceToken();
        if (deviceToken == null || deviceToken.isEmpty()) {
            logger.warn("Device token not configured. Cannot send test notification.");
            return;
        }
        
        try {
            Notification notification = Notification.builder()
                .setTitle("SecureView Test")
                .setBody("Firebase Cloud Messaging is working correctly!")
                .build();
            
            Map<String, String> data = new HashMap<>();
            data.put("type", "test");
            
            Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
                .putAllData(data)
                .build();
            
            String response = messaging.send(message);
            logger.info("Test notification sent successfully. Message ID: {}", response);
            
        } catch (Exception e) {
            logger.error("Failed to send test notification", e);
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}

