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
import com.secureview.desktop.ui.ModernTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
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
    private ModernTheme.AnimatedProgressBar progressBar;
    private ModernTheme.StatusBadge statusBadge;
    private JPanel cameraContainer;
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
        setTitle("SecureView - Face Authentication");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setResizable(false);
        setAlwaysOnTop(true);
        getContentPane().setBackground(ModernTheme.PRIMARY_DARK);
        
        // === MODERN HEADER WITH GRADIENT ===
        JPanel headerPanel = new ModernTheme.RoundedPanel(0, ModernTheme.SECONDARY_DARK);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("🔒 SecureView");
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setFont(ModernTheme.getTitleFont());
        titlePanel.add(titleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Status badge
        statusBadge = new ModernTheme.StatusBadge("● Ready", ModernTheme.TEXT_SECONDARY);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        headerPanel.add(statusBadge, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // === MAIN CONTENT ===
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Left: Modern camera panel with rounded corners
        cameraContainer = new ModernTheme.RoundedPanel(16, ModernTheme.SECONDARY_DARK);
        cameraContainer.setLayout(new BorderLayout());
        cameraContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Camera label with modern styling
        cameraLabel = new JLabel("Initializing camera...", JLabel.CENTER);
        cameraLabel.setPreferredSize(new Dimension(720, 540));
        cameraLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        cameraLabel.setFont(ModernTheme.getBodyFont());
        cameraLabel.setVerticalAlignment(SwingConstants.CENTER);
        cameraContainer.add(cameraLabel, BorderLayout.CENTER);
        
        // Camera caption
        JLabel cameraCaption = new JLabel("📹 Live Camera Feed");
        cameraCaption.setFont(ModernTheme.getHeadingFont());
        cameraCaption.setForeground(ModernTheme.TEXT_PRIMARY);
        cameraCaption.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        cameraContainer.add(cameraCaption, BorderLayout.NORTH);
        
        mainPanel.add(cameraContainer, BorderLayout.CENTER);
        
        // Right: Modern status panel
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setOpaque(false);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        sidePanel.setPreferredSize(new Dimension(320, 0));
        
        // Status card
        ModernTheme.RoundedPanel statusCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        statusCard.setLayout(new BorderLayout());
        statusCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        
        JLabel statusTitle = new JLabel("🔐 Authentication Status");
        statusTitle.setFont(ModernTheme.getHeadingFont());
        statusTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        statusTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        statusCard.add(statusTitle, BorderLayout.NORTH);
        
        statusLabel = new JLabel("<html><div style='text-align: center;'>Position your face<br>in front of the camera</div></html>");
        statusLabel.setFont(ModernTheme.getBodyFont());
        statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        statusCard.add(statusLabel, BorderLayout.CENTER);
        
        progressBar = new ModernTheme.AnimatedProgressBar();
        progressBar.setPreferredSize(new Dimension(0, 40));
        progressBar.setString("Ready");
        progressBar.setValue(0);
        statusCard.add(progressBar, BorderLayout.SOUTH);
        
        sidePanel.add(statusCard);
        sidePanel.add(Box.createVerticalStrut(16));
        
        // Info cards
        ModernTheme.RoundedPanel infoCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel infoTitle = new JLabel("📊 Recognition Details");
        infoTitle.setFont(ModernTheme.getHeadingFont());
        infoTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        infoTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        infoCard.add(infoTitle);
        
        userLabel = new JLabel("👤 User: Registered");
        userLabel.setFont(ModernTheme.getBodyFont());
        userLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        infoCard.add(userLabel);
        
        confidenceLabel = new JLabel("🎯 Confidence: --%");
        confidenceLabel.setFont(ModernTheme.getBodyFont());
        confidenceLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        infoCard.add(confidenceLabel);
        
        sidePanel.add(infoCard);
        sidePanel.add(Box.createVerticalStrut(16));
        
        // Buttons
        ModernTheme.RoundedPanel buttonCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        buttonCard.setLayout(new BorderLayout());
        buttonCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        ModernTheme.ModernButton reRegisterButton = new ModernTheme.ModernButton("🔄 Re-register Face", false);
        reRegisterButton.setPreferredSize(new Dimension(0, 45));
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
        buttonCard.add(reRegisterButton, BorderLayout.CENTER);
        sidePanel.add(buttonCard);
        
        mainPanel.add(sidePanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // === MODERN FOOTER ===
        ModernTheme.RoundedPanel bottomPanel = new ModernTheme.RoundedPanel(0, ModernTheme.SECONDARY_DARK);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        
        JLabel attemptsLabel = new JLabel("⚠️ Failed Attempts: 0");
        attemptsLabel.setFont(ModernTheme.getSmallFont());
        attemptsLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        bottomPanel.add(attemptsLabel, BorderLayout.WEST);
        
        JLabel versionLabel = new JLabel("SecureView v1.0 • Powered by RetinaFace + ArcFace");
        versionLabel.setFont(ModernTheme.getSmallFont());
        versionLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        bottomPanel.add(versionLabel, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        setVisible(true);
        toFront();
        requestFocus();
        
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
            
            // Set camera resolution for modern display
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);
            
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
                statusLabel.setText("<html><div style='text-align: center; color: #F59E0B;'><b>🔒 System Locked</b><br>Please wait " + remainingSeconds + " seconds</div></html>");
                progressBar.setString("Locked");
                progressBar.setValue(0);
                statusBadge.setBadgeColor(ModernTheme.WARNING_ORANGE);
                statusBadge.setText("● Locked");
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
            // Scale to fit modern camera display
            Image scaled = image.getScaledInstance(720, 540, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);
            cameraLabel.setIcon(icon);
            cameraLabel.setText(""); // Clear text when showing image
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
            // Smooth animation to 50%
            animateProgressBar(progressBar, 0, 50, 300);
            progressBar.startAnimation();
            statusBadge.setBadgeColor(ModernTheme.WARNING_ORANGE);
            statusBadge.setText("● Processing");
        });
        
        new Thread(() -> {
            try {
                // Detect face
                Mat face = faceRecognitionService.detectFace(frame);
                
                if (face == null || face.empty()) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("<html><div style='text-align: center; color: #94A3B8;'>👤 No face detected<br>Please position your face in front of the camera</div></html>");
                        progressBar.setString("No face detected");
                        progressBar.setValue(0);
                        progressBar.stopAnimation();
                        statusBadge.setBadgeColor(ModernTheme.TEXT_SECONDARY);
                        statusBadge.setText("● Waiting");
                    });
                    isProcessing.set(false);
                    frame.release();
                    return;
                }
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("<html><div style='text-align: center; color: #3B82F6;'>✅ Face detected<br>Authenticating...</div></html>");
                    animateProgressBar(progressBar, 50, 75, 200);
                });
                
                // Authenticate
                double similarity = faceRecognitionService.authenticateUser(face);
                double threshold = configManager.getConfig().getFaceRecognitionThreshold();
                
                final double finalSimilarity = similarity;
                SwingUtilities.invokeLater(() -> {
                    int confidencePercent = (int) Math.round(finalSimilarity * 100.0);
                    int thresholdPercent = (int) Math.round(threshold * 100.0);
                    confidenceLabel.setText("🎯 Confidence: " + confidencePercent + "% (Threshold: " + thresholdPercent + "%)");
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
                    statusLabel.setText("<html><div style='text-align: center; color: #EF4444;'>⚠️ Error: " + e.getMessage() + "</div></html>");
                    progressBar.setString("Error");
                    progressBar.setValue(0);
                    progressBar.stopAnimation();
                    statusBadge.setBadgeColor(ModernTheme.ERROR_RED);
                    statusBadge.setText("● Error");
                });
            } finally {
                isProcessing.set(false);
            }
        }).start();
    }
    
    private void handleAuthenticationSuccess() {
        // Load user name
        String userName = loadUserName();
        String greeting = userName != null && !userName.isEmpty() 
            ? "Hi " + userName + "!" 
            : "Welcome back!";
        
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center; color: #10B981; font-size: 18px;'><b>✅ Authentication Successful!</b><br><br><span style='font-size: 20px;'>" + greeting + "</span></div></html>");
            progressBar.setString("Success!");
            // Smooth animation to 100%
            animateProgressBar(progressBar, progressBar.getValue(), 100, 500);
            progressBar.stopAnimation();
            statusBadge.setBadgeColor(ModernTheme.SUCCESS_GREEN);
            statusBadge.setText("● Authenticated");
            
            attemptLogger.logSuccess("user");
            failedAttempts.set(0);
            
            // Close window after short delay with success animation
            Timer closeTimer = new Timer((int)SUCCESS_DELAY, e -> {
                dispose();
                System.exit(0);
            });
            closeTimer.setRepeats(false);
            closeTimer.start();
        });
    }
    
    /**
     * Smoothly animates progress bar from current to target value.
     */
    private void animateProgressBar(ModernTheme.AnimatedProgressBar bar, int from, int to, int durationMs) {
        if (from == to) return;
        
        int steps = 30;
        int delay = durationMs / steps;
        int diff = to - from;
        
        Timer animTimer = new Timer(delay, null);
        final int[] step = {0};
        
        animTimer.addActionListener(e -> {
            step[0]++;
            if (step[0] > steps) {
                animTimer.stop();
                bar.setValue(to);
            } else {
                int current = from + (diff * step[0] / steps);
                bar.setValue(current);
            }
        });
        animTimer.start();
    }
    
    /**
     * Loads the user's name from storage.
     */
    private String loadUserName() {
        try {
            String dataDir = configManager.getConfig().getDataDirectory();
            File nameFile = new File(dataDir, "user_name.txt");
            if (nameFile.exists()) {
                return new String(Files.readAllBytes(Paths.get(nameFile.getAbsolutePath()))).trim();
            }
        } catch (Exception e) {
            logger.debug("Could not load user name", e);
        }
        return null;
    }
    
    private void handleAuthenticationFailure(Mat face, double similarity) {
        SwingUtilities.invokeLater(() -> {
            int attempts = failedAttempts.incrementAndGet();
            statusLabel.setText("<html><div style='text-align: center; color: #EF4444;'><b>❌ Authentication Failed</b><br>Attempts: " + attempts + "</div></html>");
            progressBar.setString("Failed - " + String.format("%.1f", similarity * 100) + "% match");
            progressBar.setValue((int)(similarity * 100));
            progressBar.stopAnimation();
            statusBadge.setBadgeColor(ModernTheme.ERROR_RED);
            statusBadge.setText("● Failed");
            
            int confidencePercent = (int) Math.round(similarity * 100.0);
            confidenceLabel.setText("🎯 Confidence: " + confidencePercent + "% (below threshold)");
            
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
            
            // Send alert via email
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
                String details = String.format("INTRUSION DETECTED!\n\nFailed authentication attempts: %d\n" +
                    "System has been locked for security.\n" +
                    "Intruder image has been saved and attached to this email.", attempts);
                String emailTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                EmailAlertService.getInstance().sendIntrusionAlert(imageBytes, emailTimestamp, details);
                logger.info("Intrusion alert email sent successfully");
            } catch (Exception emailEx) {
                logger.error("Failed to send intrusion alert email", emailEx);
            }
            
            // Lock system
            lockManager.lockSystem();
            lockManager.triggerAlarm();
            
            // Lockout
            lastLockoutTime = System.currentTimeMillis();
            failedAttempts.set(0);
            
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("<html><div style='text-align: center; color: #EF4444; font-size: 16px;'><b>🚨 INTRUSION DETECTED!</b><br>System locked and alert sent</div></html>");
                progressBar.setString("LOCKED");
                progressBar.setValue(0);
                statusBadge.setBadgeColor(ModernTheme.ERROR_RED);
                statusBadge.setText("● INTRUSION");
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

