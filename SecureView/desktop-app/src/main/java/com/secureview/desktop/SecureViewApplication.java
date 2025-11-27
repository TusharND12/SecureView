package com.secureview.desktop;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.face.FaceRecognitionService;
import com.secureview.desktop.firebase.FirebaseService;
import com.secureview.desktop.lock.LockManager;
import com.secureview.desktop.logging.AttemptLogger;
import com.secureview.desktop.opencv.OpenCVLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Main application entry point for SecureView Desktop Application.
 * Handles initialization and startup of all system components.
 */
public class SecureViewApplication {
    private static final Logger logger = LoggerFactory.getLogger(SecureViewApplication.class);
    
    private FaceRecognitionService faceRecognitionService;
    private FirebaseService firebaseService;
    private LockManager lockManager;
    private AttemptLogger attemptLogger;
    private ConfigManager configManager;
    private AuthenticationWindow authWindow;
    
    public static void main(String[] args) {
        // Check for command-line arguments
        boolean forceRegistration = false;
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.equals("--register") || arg.equals("-r") || arg.equals("--reset")) {
                    forceRegistration = true;
                    logger.info("Force registration mode enabled via command-line argument");
                    break;
                }
            }
        }
        
        // Load OpenCV native library (optional - will show warning if not available)
        if (!OpenCVLoader.loadLibrary()) {
            logger.warn("OpenCV not available. Face recognition features will be limited.");
            JOptionPane.showMessageDialog(null,
                "OpenCV not found!\n\n" +
                "For full functionality, please install OpenCV 4.x:\n" +
                "1. Download from https://opencv.org/releases/\n" +
                "2. Extract to a directory (e.g., C:\\opencv)\n" +
                "3. Add OpenCV native library to your PATH\n" +
                "4. Restart the application\n\n" +
                "The application will continue with limited functionality.",
                "OpenCV Warning",
                JOptionPane.WARNING_MESSAGE);
        }
        
        final boolean finalForceRegistration = forceRegistration;
        
        // Start application
        SwingUtilities.invokeLater(() -> {
            new SecureViewApplication().start(finalForceRegistration);
        });
    }
    
    public void start() {
        start(false);
    }
    
    public void start(boolean forceRegistration) {
        logger.info("Starting SecureView Application...");
        
        try {
            // Initialize configuration
            configManager = ConfigManager.getInstance();
            configManager.loadConfiguration();
            
            // Initialize services
            initializeServices();
            
            // If force registration is requested, clear existing registration
            if (forceRegistration) {
                logger.info("Force registration requested. Clearing existing registration...");
                faceRecognitionService.clearRegistration();
                JOptionPane.showMessageDialog(null,
                    "Existing registration has been cleared.\n" +
                    "You will now be taken to the registration window.",
                    "Registration Reset",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Check if user is registered - REGISTRATION MUST HAPPEN FIRST
            if (!faceRecognitionService.isUserRegistered()) {
                logger.info("No registered user found. User must register face first.");
                showRegistrationWindow();
            } else {
                logger.info("Registered user found. Starting authentication (will compare with registered face).");
                startAuthentication();
            }
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            JOptionPane.showMessageDialog(null, 
                "Failed to start SecureView: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void initializeServices() throws Exception {
        logger.info("Initializing services...");
        
        // Initialize Firebase
        firebaseService = FirebaseService.getInstance();
        firebaseService.initialize();
        
        // Initialize face recognition
        faceRecognitionService = FaceRecognitionService.getInstance();
        faceRecognitionService.initialize();
        
        // Initialize lock manager
        lockManager = LockManager.getInstance();
        
        // Initialize attempt logger
        attemptLogger = AttemptLogger.getInstance();
        
        logger.info("All services initialized successfully");
    }
    
    private void showRegistrationWindow() {
        logger.info("No registered user found. Showing registration window...");
        RegistrationWindow registrationWindow = new RegistrationWindow(
            faceRecognitionService,
            firebaseService,
            configManager
        );
        registrationWindow.setVisible(true);
        registrationWindow.toFront();
        registrationWindow.requestFocus();
    }
    
    private void startAuthentication() {
        logger.info("Starting authentication process...");
        authWindow = new AuthenticationWindow(
            faceRecognitionService,
            firebaseService,
            lockManager,
            attemptLogger,
            configManager
        );
        authWindow.setVisible(true);
        authWindow.toFront();
        authWindow.requestFocus();
    }
}

