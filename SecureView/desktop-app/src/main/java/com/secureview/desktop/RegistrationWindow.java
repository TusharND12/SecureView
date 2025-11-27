package com.secureview.desktop;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.face.FaceRecognitionService;
import com.secureview.desktop.face.angle.AngleDetector;
import com.secureview.desktop.face.quality.FaceQualityAnalyzer;
import com.secureview.desktop.face.quality.FaceQualityAnalyzer.QualityScore;
import com.secureview.desktop.firebase.FirebaseService;
import com.secureview.desktop.lock.LockManager;
import com.secureview.desktop.logging.AttemptLogger;
import com.secureview.desktop.opencv.stub.Mat;
import com.secureview.desktop.opencv.stub.VideoCapture;
import com.secureview.desktop.opencv.stub.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.secureview.desktop.ui.ModernTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Window for registering a new user's face.
 */
public class RegistrationWindow extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationWindow.class);
    
    private FaceRecognitionService faceRecognitionService;
    private FirebaseService firebaseService;
    private ConfigManager configManager;
    
    private JLabel cameraLabel;
    private JLabel statusLabel;
    private JLabel angleProgressLabel;
    private ModernTheme.AnimatedProgressBar qualityBar;
    private JTextField emailField;
    private JTextField nameField;
    private ModernTheme.ModernButton captureButton;
    private ModernTheme.ModernButton finishButton;
    private ModernTheme.StatusBadge statusBadge;
    private JPanel cameraContainer;
    private VideoCapture camera;
    private Timer captureTimer;
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private java.util.List<Mat> capturedFaces = new java.util.ArrayList<>();
    private int currentAngle = 0;
    private static final int[] ANGLES = {0, 45, 90, 135, 180, 225, 270, 315}; // 8 angles for 360-degree capture
    private static final String[] ANGLE_NAMES = {
        "Front", "Right 45°", "Right 90°", "Right 135°", 
        "Back 180°", "Left 135°", "Left 90°", "Left 45°"
    };
    
    private final FaceQualityAnalyzer qualityAnalyzer = new FaceQualityAnalyzer();
    private final AngleDetector angleDetector = new AngleDetector();
    private static final String EMAIL_CSV_PATH = "T:\\COLLEGE LIFE\\projects\\SecureView\\SecureView\\desktop-app\\Email Alert Data.csv";
    
    public RegistrationWindow(
            FaceRecognitionService faceRecognitionService,
            FirebaseService firebaseService,
            ConfigManager configManager) {
        
        this.faceRecognitionService = faceRecognitionService;
        this.firebaseService = firebaseService;
        this.configManager = configManager;
        
        initializeUI();
        startCamera();
    }
    
    private void initializeUI() {
        setTitle("SecureView - Face Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        getContentPane().setBackground(ModernTheme.PRIMARY_DARK);
        
        // === MODERN HEADER ===
        JPanel headerPanel = new ModernTheme.RoundedPanel(0, ModernTheme.SECONDARY_DARK);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("🔒 SecureView");
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setFont(ModernTheme.getTitleFont());
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        statusBadge = new ModernTheme.StatusBadge("● Registration Mode", ModernTheme.ACCENT_PURPLE);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        headerPanel.add(statusBadge, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // === MAIN CONTENT ===
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Left: Modern camera panel
        cameraContainer = new ModernTheme.RoundedPanel(16, ModernTheme.SECONDARY_DARK);
        cameraContainer.setLayout(new BorderLayout());
        cameraContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel cameraCaption = new JLabel("📹 360° Face Capture");
        cameraCaption.setFont(ModernTheme.getHeadingFont());
        cameraCaption.setForeground(ModernTheme.TEXT_PRIMARY);
        cameraCaption.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        cameraContainer.add(cameraCaption, BorderLayout.NORTH);
        
        cameraLabel = new JLabel("Initializing camera...", JLabel.CENTER);
        cameraLabel.setPreferredSize(new Dimension(720, 540));
        cameraLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        cameraLabel.setFont(ModernTheme.getBodyFont());
        cameraLabel.setVerticalAlignment(SwingConstants.CENTER);
        cameraContainer.add(cameraLabel, BorderLayout.CENTER);
        
        angleProgressLabel = new JLabel("📊 Angles captured: 0 / " + ANGLES.length);
        angleProgressLabel.setFont(ModernTheme.getBodyFont());
        angleProgressLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        angleProgressLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        cameraContainer.add(angleProgressLabel, BorderLayout.SOUTH);
        
        mainPanel.add(cameraContainer, BorderLayout.CENTER);
        
        // Right: Modern registration panel
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setOpaque(false);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        sidePanel.setPreferredSize(new Dimension(320, 0));

        // Email input card
        ModernTheme.RoundedPanel emailCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        emailCard.setLayout(new BorderLayout());
        emailCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel emailTitle = new JLabel("📧 Alert Email");
        emailTitle.setFont(ModernTheme.getHeadingFont());
        emailTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        emailTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        emailCard.add(emailTitle, BorderLayout.NORTH);
        
        emailField = new JTextField();
        emailField.setFont(ModernTheme.getBodyFont());
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        emailField.setBackground(ModernTheme.SECONDARY_DARK);
        emailField.setForeground(ModernTheme.TEXT_PRIMARY);
        String existingEmail = configManager.getConfig().getAlertEmailTo();
        if (existingEmail != null && !existingEmail.isEmpty()) {
            emailField.setText(existingEmail);
        }
        emailCard.add(emailField, BorderLayout.CENTER);
        
        JLabel emailHint = new JLabel("<html><small style='color: #94A3B8;'>We'll send intrusion alerts to this email</small></html>");
        emailHint.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        emailCard.add(emailHint, BorderLayout.SOUTH);
        sidePanel.add(emailCard);
        sidePanel.add(Box.createVerticalStrut(16));
        
        // Name input card
        ModernTheme.RoundedPanel nameCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        nameCard.setLayout(new BorderLayout());
        nameCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel nameTitle = new JLabel("👤 Your Name");
        nameTitle.setFont(ModernTheme.getHeadingFont());
        nameTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        nameTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        nameCard.add(nameTitle, BorderLayout.NORTH);
        
        nameField = new JTextField();
        nameField.setFont(ModernTheme.getBodyFont());
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        nameField.setBackground(ModernTheme.SECONDARY_DARK);
        nameField.setForeground(ModernTheme.TEXT_PRIMARY);
        nameField.setToolTipText("Enter your name");
        nameCard.add(nameField, BorderLayout.CENTER);
        
        JLabel nameHint = new JLabel("<html><small style='color: #94A3B8;'>We'll greet you by name when you authenticate</small></html>");
        nameHint.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        nameCard.add(nameHint, BorderLayout.SOUTH);
        sidePanel.add(nameCard);
        sidePanel.add(Box.createVerticalStrut(16));
        
        // Quality meter card
        ModernTheme.RoundedPanel qualityCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        qualityCard.setLayout(new BorderLayout());
        qualityCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel qualityTitle = new JLabel("✨ Face Quality");
        qualityTitle.setFont(ModernTheme.getHeadingFont());
        qualityTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        qualityTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        qualityCard.add(qualityTitle, BorderLayout.NORTH);
        
        qualityBar = new ModernTheme.AnimatedProgressBar();
        qualityBar.setPreferredSize(new Dimension(0, 40));
        qualityBar.setString("Waiting for face...");
        qualityBar.setValue(0);
        qualityCard.add(qualityBar, BorderLayout.CENTER);
        sidePanel.add(qualityCard);
        sidePanel.add(Box.createVerticalStrut(16));
        
        // Status card
        ModernTheme.RoundedPanel statusCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        statusCard.setLayout(new BorderLayout());
        statusCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel statusTitle = new JLabel("📋 Instructions");
        statusTitle.setFont(ModernTheme.getHeadingFont());
        statusTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        statusTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        statusCard.add(statusTitle, BorderLayout.NORTH);
        
        statusLabel = new JLabel("<html><div style='text-align: center; color: #94A3B8;'>We'll capture your face from multiple angles.<br><br>Start by facing the camera: <b style='color: #3B82F6;'>Front (0°)</b><br>and enter your email above for alerts.</div></html>");
        statusLabel.setFont(ModernTheme.getBodyFont());
        statusCard.add(statusLabel, BorderLayout.CENTER);
        sidePanel.add(statusCard);
        sidePanel.add(Box.createVerticalStrut(16));
        
        // Buttons card
        ModernTheme.RoundedPanel buttonCard = new ModernTheme.RoundedPanel(16, ModernTheme.CARD_BG);
        buttonCard.setLayout(new BoxLayout(buttonCard, BoxLayout.Y_AXIS));
        buttonCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        captureButton = new ModernTheme.ModernButton("📸 Capture Current Angle", true);
        captureButton.setPreferredSize(new Dimension(0, 50));
        captureButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        captureButton.addActionListener(e -> captureFace());
        buttonCard.add(captureButton);
        buttonCard.add(Box.createVerticalStrut(12));
        
        finishButton = new ModernTheme.ModernButton("✅ Finish Registration", false);
        finishButton.setPreferredSize(new Dimension(0, 50));
        finishButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        finishButton.setEnabled(false);
        finishButton.addActionListener(e -> finishRegistration());
        buttonCard.add(finishButton);
        
        sidePanel.add(buttonCard);
        mainPanel.add(sidePanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true);
        toFront();
        requestFocus();
    }
    
    private void startCamera() {
        try {
            logger.info("Initializing camera...");
            statusLabel.setText("Initializing camera...");
            
            camera = new VideoCapture(0);
            
            // Wait a bit for camera to initialize
            Thread.sleep(500);
            
            if (!camera.isOpened()) {
                throw new Exception("Failed to open camera. Please check:\n" +
                    "1. Camera is connected\n" +
                    "2. No other application is using the camera\n" +
                    "3. Camera permissions are granted");
            }
            
            logger.info("Camera opened successfully, setting properties...");
            
            // Set camera resolution for modern display
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);
            
            // Give camera time to adjust
            Thread.sleep(200);
            
            // Test if we can read a frame
            Mat testFrame = new Mat();
            int retries = 5;
            boolean frameRead = false;
            for (int i = 0; i < retries; i++) {
                if (camera.read(testFrame) && !testFrame.empty()) {
                    frameRead = true;
                    logger.info("Camera test frame read successfully");
                    break;
                }
                Thread.sleep(100);
            }
            testFrame.release();
            
            if (!frameRead) {
                throw new Exception("Camera opened but cannot read frames. Please check camera settings.");
            }
            
            // Start capture timer
            captureTimer = new Timer(33, e -> updateCameraFeed());
            captureTimer.start();
            
            logger.info("Camera started successfully for registration");
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("<html><center><b>Step 1:</b> Position your face in front of the camera<br>" +
                    "<b>Step 2:</b> Click 'Capture Face' when ready<br>" +
                    "<b>Step 3:</b> Click 'Finish Registration' to complete</center></html>");
            });
            
        } catch (Exception e) {
            logger.error("Failed to start camera", e);
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Error: Failed to start camera");
                JOptionPane.showMessageDialog(this,
                    "Failed to start camera: " + e.getMessage() + "\n\n" +
                    "Troubleshooting:\n" +
                    "1. Ensure camera is connected and working\n" +
                    "2. Close other applications using the camera\n" +
                    "3. Check camera permissions in Windows settings\n" +
                    "4. Try restarting the application",
                    "Camera Error",
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    private void updateCameraFeed() {
        if (isProcessing.get()) {
            return;
        }
        
        Mat frame = new Mat();
        try {
            if (!camera.read(frame)) {
                frame.release();
                return;
            }
            
            if (frame.empty() || frame.cols() == 0 || frame.rows() == 0) {
                frame.release();
                return;
            }
        } catch (Exception e) {
            logger.debug("Error reading camera frame: {}", e.getMessage());
            frame.release();
            return;
        }
        
        // Check for face and update quality/angle UI
        try {
            Mat face = faceRecognitionService.detectFace(frame);
            if (face != null && !face.empty()) {
                // Quality analysis
                QualityScore quality = qualityAnalyzer.analyzeQuality(face);
                int qualityPercent = (int) Math.round(quality.overallScore * 100.0);
                qualityBar.setValue(qualityPercent);
                qualityBar.setString("Quality: " + qualityPercent + "%");
                if (qualityPercent > 70) {
                    qualityBar.startAnimation();
                } else {
                    qualityBar.stopAnimation();
                }
                
                // Angle detection
                AngleDetector.AngleInfo angleInfo = angleDetector.detectAngle(face);
                String angleName = currentAngle < ANGLE_NAMES.length ? ANGLE_NAMES[currentAngle] : angleInfo.angleName;
                
                statusLabel.setText("<html><div style='text-align: center; color: #94A3B8;'><b style='color: #3B82F6; font-size: 16px;'>360° Face Registration</b><br><br>" +
                    "✅ Face detected! Position: <b style='color: #10B981;'>" + angleName + "</b><br>" +
                    quality.feedback + "<br><br>" +
                    "📊 Captured: <b>" + capturedFaces.size() + "/" + ANGLES.length + "</b> angles</div></html>");
                
                angleProgressLabel.setText("📊 Angles captured: " + capturedFaces.size() + " / " + ANGLES.length);
                
                // Optional auto-capture when quality is optimal
                if (quality.isOptimal && capturedFaces.size() < ANGLES.length && !isProcessing.get()) {
                    logger.info("Quality optimal ({}). Auto-capturing angle {}.", qualityPercent, capturedFaces.size() + 1);
                    autoCaptureFromFace(face);
                    // autoCaptureFromFace will release face
                    face = null;
                } else {
                    face.release();
                }
            } else {
                qualityBar.setValue(0);
                qualityBar.setString("Waiting for face...");
                qualityBar.stopAnimation();
                statusLabel.setText("<html><div style='text-align: center; color: #94A3B8;'><b style='color: #3B82F6; font-size: 16px;'>360° Face Registration</b><br><br>" +
                    "👤 No face detected. Please position your face.<br><br>" +
                    "📊 Captured: <b>" + capturedFaces.size() + "/" + ANGLES.length + "</b> angles</div></html>");
                if (face != null) {
                    face.release();
                }
            }
        } catch (Exception e) {
            logger.debug("Face detection error: {}", e.getMessage());
            statusLabel.setText("Position your face in front of the camera.");
        }
        
        // Display frame
        displayFrame(frame);
        frame.release();
    }
    
    private void displayFrame(Mat frame) {
        try {
            BufferedImage image = matToBufferedImage(frame);
            Image scaled = image.getScaledInstance(720, 540, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);
            cameraLabel.setIcon(icon);
            cameraLabel.setText(""); // Clear text when showing image
        } catch (Exception e) {
            logger.error("Error displaying frame", e);
        }
    }
    
    private void captureFace() {
        if (isProcessing.get()) {
            return;
        }
        
        if (capturedFaces.size() >= ANGLES.length) {
            JOptionPane.showMessageDialog(this,
                "All angles have been captured!\nClick 'Finish Registration' to complete.",
                "All Angles Captured",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        isProcessing.set(true);
        captureButton.setEnabled(false);
        String angleName = currentAngle < ANGLE_NAMES.length ? ANGLE_NAMES[currentAngle] : "Angle " + currentAngle;
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("<html><div style='text-align: center; color: #3B82F6;'><b>📸 Capturing " + angleName + "...</b><br>Please wait...</div></html>");
                qualityBar.setString("Capturing...");
                qualityBar.setValue(50);
                qualityBar.startAnimation();
            });
        
        new Thread(() -> {
            try {
                // Try multiple times to capture a frame
                Mat frame = new Mat();
                boolean frameCaptured = false;
                int maxRetries = 10;
                
                for (int i = 0; i < maxRetries; i++) {
                    if (camera.read(frame)) {
                        if (!frame.empty() && frame.cols() > 0 && frame.rows() > 0) {
                            frameCaptured = true;
                            logger.info("Frame captured successfully (attempt {})", i + 1);
                            break;
                        }
                    }
                    Thread.sleep(50); // Wait 50ms between retries
                }
                
                if (!frameCaptured) {
                    logger.error("Failed to capture frame after {} attempts", maxRetries);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Failed to capture frame. Please try again.");
                        JOptionPane.showMessageDialog(RegistrationWindow.this,
                            "Failed to capture frame from camera.\n\n" +
                            "Possible causes:\n" +
                            "1. Camera is being used by another application\n" +
                            "2. Camera permissions not granted\n" +
                            "3. Camera hardware issue\n\n" +
                            "Please check your camera and try again.",
                            "Capture Error",
                            JOptionPane.WARNING_MESSAGE);
                        isProcessing.set(false);
                        captureButton.setEnabled(true);
                    });
                    frame.release();
                    return;
                }
                
                Mat face = faceRecognitionService.detectFace(frame);
                frame.release();
                
                if (face == null || face.empty()) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("No face detected. Please try again.");
                        isProcessing.set(false);
                        captureButton.setEnabled(true);
                    });
                    return;
                }
                
                autoCaptureFromFace(face);
                
            } catch (Exception e) {
                logger.error("Error capturing face", e);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    isProcessing.set(false);
                    captureButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    /**
     * Adds a detected face to the captured list and updates UI.
     * Used by both manual capture and auto-capture.
     */
    private void autoCaptureFromFace(Mat face) {
        try {
            // Store captured face - create a proper copy
            Mat faceCopy = new Mat();
            try {
                Object realFace = face.getRealInstance();
                Object realCaptured = faceCopy.getRealInstance();
                if (realFace != null && realCaptured != null) {
                    java.lang.reflect.Method copyToMethod = realFace.getClass().getMethod("copyTo",
                        Class.forName("org.opencv.core.Mat"));
                    copyToMethod.invoke(realFace, realCaptured);
                } else {
                    face.copyTo(faceCopy);
                }
            } catch (Exception e) {
                logger.warn("Error copying face Mat, using stub method", e);
                face.copyTo(faceCopy);
            }
            face.release();
            
            capturedFaces.add(faceCopy);
            currentAngle++;
            
            SwingUtilities.invokeLater(() -> {
                angleProgressLabel.setText("Angles captured: " + capturedFaces.size() + " / " + ANGLES.length);
                if (capturedFaces.size() >= ANGLES.length) {
                    statusLabel.setText("<html><div style='text-align: center; color: #10B981;'><b style='font-size: 18px;'>✅ All angles captured!</b><br><br>" +
                        "📊 Captured: <b>" + capturedFaces.size() + "/" + ANGLES.length + "</b> angles<br><br>" +
                        "Click <b>'Finish Registration'</b> to complete</div></html>");
                    finishButton.setEnabled(true);
                    captureButton.setEnabled(false);
                    statusBadge.setBadgeColor(ModernTheme.SUCCESS_GREEN);
                    statusBadge.setText("● Complete");
                } else {
                    String nextAngleName = currentAngle < ANGLE_NAMES.length ? ANGLE_NAMES[currentAngle] : "Angle " + currentAngle;
                    statusLabel.setText("<html><div style='text-align: center; color: #94A3B8;'><b style='color: #10B981; font-size: 16px;'>✅ Angle " + (capturedFaces.size()) + " captured!</b><br><br>" +
                        "Now position your face: <b style='color: #3B82F6;'>" + nextAngleName + "</b><br><br>" +
                        "📊 Captured: <b>" + capturedFaces.size() + "/" + ANGLES.length + "</b> angles<br><br>" +
                        "You can wait for auto-capture or click 'Capture current angle'</div></html>");
                    captureButton.setEnabled(true);
                }
                isProcessing.set(false);
            });
        } catch (Exception e) {
            logger.error("Error during auto-capture", e);
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Auto-capture error: " + e.getMessage());
                isProcessing.set(false);
                captureButton.setEnabled(true);
            });
        }
    }

    /**
     * Saves the user's alert email into a CSV file.
     */
    private void saveEmailToCsv(String email) {
        try {
            String dataDir = configManager.getConfig().getDataDirectory();
            Files.createDirectories(Paths.get(dataDir));
            File csvFile = new File(dataDir, "Email Alert Data.csv");
            boolean exists = csvFile.exists();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
                if (!exists) {
                    writer.write("timestamp,email");
                    writer.newLine();
                }
                String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String safeEmail = email.replace(",", " ");
                writer.write(ts + "," + safeEmail);
                writer.newLine();
            }
            logger.info("Saved alert email to CSV: {}", csvFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("Failed to save alert email to CSV", e);
            JOptionPane.showMessageDialog(this,
                "Warning: Could not save email to CSV file.\n" +
                "Email alerts may not work after restart.",
                "Email Save Warning",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Saves the user's name to a file.
     */
    private void saveUserName(String name) {
        try {
            String dataDir = configManager.getConfig().getDataDirectory();
            Files.createDirectories(Paths.get(dataDir));
            File nameFile = new File(dataDir, "user_name.txt");
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nameFile))) {
                writer.write(name);
            }
            logger.info("Saved user name: {}", name);
        } catch (IOException e) {
            logger.warn("Failed to save user name", e);
        }
    }
    
    private void finishRegistration() {
        if (capturedFaces.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No faces captured. Please capture at least one angle first.",
                "Registration Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate email
        String email = emailField != null ? emailField.getText().trim() : "";
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address for intrusion alerts.",
                "Invalid Email",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get and validate name
        String userName = nameField != null ? nameField.getText().trim() : "";
        if (userName.isEmpty()) {
            userName = "User"; // Default name
        }

        // Save alert email into CSV file
        saveEmailToCsv(email);
        
        // Save user name
        saveUserName(userName);
        
        if (capturedFaces.size() < 3) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Only " + capturedFaces.size() + " angle(s) captured. For better security, " +
                "it's recommended to capture at least 3-4 angles.\n\n" +
                "Do you want to continue with " + capturedFaces.size() + " angle(s)?",
                "Few Angles Captured",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        finishButton.setEnabled(false);
        captureButton.setEnabled(false);
        statusLabel.setText("<html><center>Registering user with " + capturedFaces.size() + " angles...<br>Please wait...</center></html>");
        
        new Thread(() -> {
            try {
                logger.info("Starting registration process with {} face angles...", capturedFaces.size());
                boolean success = faceRecognitionService.registerUserMultiAngle(capturedFaces);
                
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "User registered successfully!\n\n" +
                            "You will now be taken to the authentication screen.",
                            "Registration Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        // Transition to authentication window
                        startAuthenticationWindow();
                    } else {
                        statusLabel.setText("Registration failed. Please try capturing again.");
                        finishButton.setEnabled(true);
                        captureButton.setEnabled(true);
                        JOptionPane.showMessageDialog(this,
                            "Registration failed. Possible reasons:\n" +
                            "- Face embedding extraction failed\n" +
                            "- Liveness detection failed\n" +
                            "- Please try capturing your face again with better lighting.",
                            "Registration Error",
                            JOptionPane.WARNING_MESSAGE);
                    }
                });
                
            } catch (Exception e) {
                logger.error("Error during registration", e);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Registration error: " + e.getMessage());
                    finishButton.setEnabled(true);
                    captureButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                        "Registration failed: " + e.getMessage() + "\n\n" +
                        "Please try again or check the logs for details.",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
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
    
    private void startAuthenticationWindow() {
        logger.info("Starting authentication window after successful registration");
        try {
            LockManager lockManager = LockManager.getInstance();
            AttemptLogger attemptLogger = AttemptLogger.getInstance();
            
            AuthenticationWindow authWindow = new AuthenticationWindow(
                faceRecognitionService,
                firebaseService,
                lockManager,
                attemptLogger,
                configManager
            );
            authWindow.setVisible(true);
        } catch (Exception e) {
            logger.error("Failed to start authentication window", e);
            JOptionPane.showMessageDialog(null,
                "Registration successful, but failed to start authentication window.\n" +
                "Please restart the application.",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }
    
    @Override
    public void dispose() {
        if (captureTimer != null) {
            captureTimer.stop();
        }
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        for (Mat face : capturedFaces) {
            if (face != null) {
                face.release();
            }
        }
        capturedFaces.clear();
        super.dispose();
    }
}

