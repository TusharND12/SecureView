package com.secureview.desktop;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.face.FaceRecognitionService;
import com.secureview.desktop.firebase.FirebaseService;
import com.secureview.desktop.email.EmailAlertService;
import com.secureview.desktop.lock.LockManager;
import com.secureview.desktop.logging.AttemptLogger;
import com.secureview.desktop.opencv.stub.Mat;
import com.secureview.desktop.opencv.stub.Imgcodecs;
import com.secureview.desktop.opencv.stub.VideoCapture;
import com.secureview.desktop.opencv.stub.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main authentication window that handles face recognition and intrusion detection.
 */
public class AuthenticationWindow extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationWindow.class);
    
    private FaceRecognitionService faceRecognitionService;
    private FirebaseService firebaseService;
    private LockManager lockManager;
    private AttemptLogger attemptLogger;
    private ConfigManager configManager;
    
    private JLabel cameraLabel;
    private JLabel statusLabel;
    private JLabel userLabel;
    private JLabel confidenceLabel;
    private JProgressBar progressBar;
    private VideoCapture camera;
    private Timer captureTimer;
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private AtomicInteger failedAttempts = new AtomicInteger(0);
    private long lastLockoutTime = 0;
    private long lastAuthenticationAttempt = 0;
    private static final long AUTHENTICATION_COOLDOWN = 2000; // 2 seconds between attempts
    private static final long SUCCESS_DELAY = 500; // 500ms before closing on success
    private int frameSkipCounter = 0;
    private static final int FRAME_SKIP = 2; // Process every 3rd frame for display
    
    public AuthenticationWindow(
            FaceRecognitionService faceRecognitionService,
            FirebaseService firebaseService,
            LockManager lockManager,
            AttemptLogger attemptLogger,
            ConfigManager configManager) {
        
        this.faceRecognitionService = faceRecognitionService;
        this.firebaseService = firebaseService;
        this.lockManager = lockManager;
        this.attemptLogger = attemptLogger;
        this.configManager = configManager;
        
        initializeUI();
        startCamera();
    }
    
    private void initializeUI() {
        setTitle("SecureView - Authentication");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setAlwaysOnTop(true); // Keep window on top initially
        setVisible(true); // Make sure it's visible
        toFront(); // Bring to front
        requestFocus(); // Request focus
        
        // === HEADER BAR ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0x1f2933));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        JLabel titleLabel = new JLabel("SecureView");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel modeLabel = new JLabel("Authentication Mode");
        modeLabel.setForeground(Color.LIGHT_GRAY);
        modeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        headerPanel.add(modeLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // === MAIN CONTENT ===
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left: camera panel
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xd1d5db)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        cameraLabel = new JLabel("Initializing camera...", JLabel.CENTER);
        cameraLabel.setPreferredSize(new Dimension(640, 480));
        cameraPanel.add(cameraLabel, BorderLayout.CENTER);
        
        JLabel cameraCaption = new JLabel("Live camera", JLabel.LEFT);
        cameraCaption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cameraCaption.setForeground(new Color(0x6b7280));
        cameraPanel.add(cameraCaption, BorderLayout.NORTH);
        
        mainPanel.add(cameraPanel, BorderLayout.CENTER);
        
        // Right: status & details panel
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        sidePanel.setPreferredSize(new Dimension(260, 0));
        
        // User info
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("User"));
        userLabel = new JLabel("Current user: user", JLabel.LEFT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userPanel.add(userLabel, BorderLayout.CENTER);
        sidePanel.add(userPanel);
        
        // Confidence info
        JPanel confidencePanel = new JPanel(new BorderLayout());
        confidencePanel.setBorder(BorderFactory.createTitledBorder("Confidence"));
        confidenceLabel = new JLabel("Confidence: --%", JLabel.LEFT);
        confidenceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confidencePanel.add(confidenceLabel, BorderLayout.CENTER);
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(confidencePanel);
        
        // Status text
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        statusLabel = new JLabel("Position your face in front of the camera", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        statusPanel.add(progressBar, BorderLayout.SOUTH);
        
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(statusPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton reRegisterButton = new JButton("Re-register Face");
        reRegisterButton.setToolTipText("Clear existing registration and register a new face");
        reRegisterButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "This will clear your current registration.\n" +
                "You will need to register your face again.\n\n" +
                "Do you want to continue?",
                "Re-register Face",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    faceRecognitionService.clearRegistration();
                    JOptionPane.showMessageDialog(
                        this,
                        "Registration cleared.\n" +
                        "Closing authentication window.\n" +
                        "Please restart the application to register again.",
                        "Registration Cleared",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    dispose();
                    System.exit(0);
                } catch (Exception ex) {
                    logger.error("Error clearing registration", ex);
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to clear registration: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        buttonPanel.add(reRegisterButton);
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(buttonPanel);
        
        mainPanel.add(sidePanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // === BOTTOM BAR ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        
        JLabel attemptsLabel = new JLabel("Attempts: 0 failed", JLabel.LEFT);
        attemptsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bottomPanel.add(attemptsLabel, BorderLayout.WEST);
        
        JLabel versionLabel = new JLabel("SecureView Desktop", JLabel.RIGHT);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(0x6b7280));
        bottomPanel.add(versionLabel, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Handle window close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Prevent closing - user must authenticate
                JOptionPane.showMessageDialog(
                    AuthenticationWindow.this,
                    "Please authenticate to close this window.",
                    "Authentication Required",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        });
    }
    
    private void startCamera() {
        try {
            camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                throw new Exception("Failed to open camera");
            }
            
            // Set camera resolution
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
            
            // Start capture timer - reduced frequency for better performance
            captureTimer = new Timer(200, e -> captureAndProcess()); // 5 FPS for processing
            captureTimer.start();
            
            logger.info("Camera started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start camera", e);
            statusLabel.setText("Error: Failed to start camera");
            JOptionPane.showMessageDialog(this,
                "Failed to start camera: " + e.getMessage(),
                "Camera Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void captureAndProcess() {
        if (isProcessing.get()) {
            return;
        }
        
        // Check for lockout
        long currentTime = System.currentTimeMillis();
        if (lastLockoutTime > 0 && (currentTime - lastLockoutTime) < 
            configManager.getConfig().getLockoutDuration()) {
            long remainingSeconds = (configManager.getConfig().getLockoutDuration() - 
                (currentTime - lastLockoutTime)) / 1000;
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("System locked. Please wait " + remainingSeconds + " seconds.");
                progressBar.setString("Locked");
            });
            return;
        }
        
        if (lastLockoutTime > 0 && (currentTime - lastLockoutTime) >= 
            configManager.getConfig().getLockoutDuration()) {
            // Lockout expired
            lastLockoutTime = 0;
            failedAttempts.set(0);
        }
        
        // Check authentication cooldown
        if ((currentTime - lastAuthenticationAttempt) < AUTHENTICATION_COOLDOWN) {
            // Still in cooldown, just display frame without processing
            Mat frame = new Mat();
            if (camera.read(frame) && !frame.empty()) {
                displayFrame(frame);
                frame.release();
            }
            return;
        }
        
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) {
            frame.release();
            return;
        }
        
        // Display frame (every frame for smooth video)
        displayFrame(frame);
        
        // Process authentication (with frame skipping for performance)
        frameSkipCounter++;
        if (frameSkipCounter >= FRAME_SKIP) {
            frameSkipCounter = 0;
            processAuthentication(frame);
        } else {
            frame.release(); // Release frame if not processing
        }
    }
    
    private void displayFrame(Mat frame) {
        try {
            BufferedImage image = matToBufferedImage(frame);
            ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_SMOOTH));
            cameraLabel.setIcon(icon);
        } catch (Exception e) {
            logger.error("Error displaying frame", e);
        }
    }
    
    private void processAuthentication(Mat frame) {
        if (isProcessing.get()) {
            frame.release();
            return;
        }
        
        isProcessing.set(true);
        lastAuthenticationAttempt = System.currentTimeMillis();
        
        SwingUtilities.invokeLater(() -> {
            progressBar.setString("Processing...");
        });
        
        new Thread(() -> {
            try {
                // Detect face
                Mat face = faceRecognitionService.detectFace(frame);
                
                if (face == null || face.empty()) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("No face detected. Please position your face in front of the camera.");
                        progressBar.setString("No face detected");
                    });
                    isProcessing.set(false);
                    frame.release();
                    return;
                }
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Face detected. Authenticating...");
                });
                
                // Authenticate
                double similarity = faceRecognitionService.authenticateUser(face);
                double threshold = configManager.getConfig().getFaceRecognitionThreshold();
                
                final double finalSimilarity = similarity;
                SwingUtilities.invokeLater(() -> {
                    int confidencePercent = (int) Math.round(finalSimilarity * 100.0);
                    confidenceLabel.setText("Confidence: " + confidencePercent + "% (Threshold: " +
                        (int) Math.round(threshold * 100.0) + "%)");
                });
                
                if (similarity >= threshold) {
                    // Authentication successful
                    handleAuthenticationSuccess();
                } else {
                    // Authentication failed
                    handleAuthenticationFailure(face, similarity);
                }
                
                face.release();
                frame.release();
                
            } catch (Exception e) {
                logger.error("Error during authentication", e);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    progressBar.setString("Error");
                });
            } finally {
                isProcessing.set(false);
            }
        }).start();
    }
    
    private void handleAuthenticationSuccess() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Authentication successful!");
            progressBar.setString("Success");
            progressBar.setValue(100);
            
            attemptLogger.logSuccess("user");
            failedAttempts.set(0);
            
            // Close window after short delay (reduced from 1000ms to 500ms)
            Timer closeTimer = new Timer((int)SUCCESS_DELAY, e -> {
                dispose();
                System.exit(0);
            });
            closeTimer.setRepeats(false);
            closeTimer.start();
        });
    }
    
    private void handleAuthenticationFailure(Mat face, double similarity) {
        SwingUtilities.invokeLater(() -> {
            int attempts = failedAttempts.incrementAndGet();
            statusLabel.setText("Authentication failed. Attempts: " + attempts);
            progressBar.setString("Failed - Similarity: " + String.format("%.2f", similarity));
            int confidencePercent = (int) Math.round(similarity * 100.0);
            confidenceLabel.setText("Confidence: " + confidencePercent + "% (below threshold)");
            
            attemptLogger.logFailure("Low similarity score", similarity);
            
            // Check if threshold exceeded
            if (attempts >= configManager.getConfig().getMaxFailedAttempts()) {
                handleIntrusion(face, attempts);
            }
        });
    }
    
    private void handleIntrusion(Mat face, int attempts) {
        try {
            logger.error("INTRUSION DETECTED - {} failed attempts", attempts);
            
            // Save intruder image
            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dataDir = configManager.getConfig().getDataDirectory();
            String imagePath = dataDir + File.separator + "intrusion_" + timestamp + ".jpg";
            
            Files.createDirectories(Paths.get(dataDir));
            Imgcodecs.imwrite(imagePath, face);
            
            // Log intrusion
            attemptLogger.logIntrusion("Multiple failed authentication attempts", imagePath);
            attemptLogger.logLockout(attempts);
            
            // Send alert via email (direct, no Firebase)
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            String details = String.format("Failed attempts: %d, Similarity: %.3f", 
                attempts, 0.0);
            String emailTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            EmailAlertService.getInstance().sendIntrusionAlert(imageBytes, emailTimestamp, details);
            
            // Lock system
            lockManager.lockSystem();
            lockManager.triggerAlarm();
            
            // Lockout
            lastLockoutTime = System.currentTimeMillis();
            failedAttempts.set(0);
            
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("INTRUSION DETECTED! System locked.");
                progressBar.setString("LOCKED");
                JOptionPane.showMessageDialog(this,
                    "INTRUSION DETECTED!\n\n" +
                    "Multiple failed authentication attempts detected.\n" +
                    "System has been locked and an alert has been sent to your email.",
                    "Security Alert",
                    JOptionPane.ERROR_MESSAGE);
            });
            
        } catch (Exception e) {
            logger.error("Error handling intrusion", e);
        }
    }
    
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        
        return image;
    }
    
    @Override
    public void dispose() {
        if (captureTimer != null) {
            captureTimer.stop();
        }
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        super.dispose();
    }
}

