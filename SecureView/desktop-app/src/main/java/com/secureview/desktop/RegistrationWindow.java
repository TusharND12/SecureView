package com.secureview.desktop;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.face.FaceRecognitionService;
import com.secureview.desktop.firebase.FirebaseService;
import com.secureview.desktop.lock.LockManager;
import com.secureview.desktop.logging.AttemptLogger;
import com.secureview.desktop.opencv.stub.Mat;
import com.secureview.desktop.opencv.stub.VideoCapture;
import com.secureview.desktop.opencv.stub.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    private JButton captureButton;
    private JButton finishButton;
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
        setTitle("SecureView - User Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Camera panel
        cameraLabel = new JLabel("Initializing camera...", JLabel.CENTER);
        cameraLabel.setPreferredSize(new Dimension(640, 480));
        cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("<html><center><b>360° Face Registration</b><br>" +
            "We'll capture your face from multiple angles<br>" +
            "Position your face: <b>Front</b> (0°)<br>" +
            "Captured: 0/8 angles</center></html>", 
            JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        captureButton = new JButton("Capture Current Angle");
        captureButton.addActionListener(e -> captureFace());
        
        finishButton = new JButton("Finish Registration");
        finishButton.setEnabled(false);
        finishButton.addActionListener(e -> finishRegistration());
        
        buttonPanel.add(captureButton);
        buttonPanel.add(finishButton);
        
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(cameraLabel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
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
            
            // Set camera resolution
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
            
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
        
        // Check for face
        try {
                Mat face = faceRecognitionService.detectFace(frame);
            if (face != null) {
                // Check if face is empty - use real OpenCV method if available
                boolean isEmpty = face.empty();
                if (!isEmpty) {
                    String angleName = currentAngle < ANGLE_NAMES.length ? ANGLE_NAMES[currentAngle] : "Angle " + currentAngle;
                    statusLabel.setText("<html><center><b>360° Face Registration</b><br>" +
                        "Face detected! Position: <b>" + angleName + "</b><br>" +
                        "Captured: " + capturedFaces.size() + "/8 angles<br>" +
                        "Click 'Capture Current Angle' when ready</center></html>");
                    face.release();
                } else {
                    statusLabel.setText("<html><center><b>360° Face Registration</b><br>" +
                        "No face detected. Please position your face.<br>" +
                        "Captured: " + capturedFaces.size() + "/8 angles</center></html>");
                    if (face != null) {
                        face.release();
                    }
                }
            } else {
                statusLabel.setText("<html><center><b>360° Face Registration</b><br>" +
                    "No face detected. Please position your face.<br>" +
                    "Captured: " + capturedFaces.size() + "/8 angles</center></html>");
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
            ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_SMOOTH));
            cameraLabel.setIcon(icon);
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
            statusLabel.setText("<html><center>Capturing <b>" + angleName + "</b>... Please wait...</center></html>");
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
                
                // Store captured face - create a proper copy
                Mat faceCopy = new Mat();
                try {
                    // Use real OpenCV copyTo if available
                    Object realFace = face.getRealInstance();
                    Object realCaptured = faceCopy.getRealInstance();
                    if (realFace != null && realCaptured != null) {
                        // Use reflection to call copyTo on real Mat
                        java.lang.reflect.Method copyToMethod = realFace.getClass().getMethod("copyTo", 
                            Class.forName("org.opencv.core.Mat"));
                        copyToMethod.invoke(realFace, realCaptured);
                    } else {
                        // Fallback to stub copyTo
                        face.copyTo(faceCopy);
                    }
                } catch (Exception e) {
                    logger.warn("Error copying face Mat, using stub method", e);
                    face.copyTo(faceCopy);
                }
                face.release();
                
                // Add to captured faces list
                capturedFaces.add(faceCopy);
                currentAngle++;
                
                SwingUtilities.invokeLater(() -> {
                    if (capturedFaces.size() >= ANGLES.length) {
                        statusLabel.setText("<html><center><b>All angles captured!</b><br>" +
                            "Captured: " + capturedFaces.size() + "/8 angles<br>" +
                            "Click 'Finish Registration' to complete</center></html>");
                        finishButton.setEnabled(true);
                        captureButton.setEnabled(false);
                    } else {
                        String nextAngleName = currentAngle < ANGLE_NAMES.length ? ANGLE_NAMES[currentAngle] : "Angle " + currentAngle;
                        statusLabel.setText("<html><center><b>Angle " + (capturedFaces.size()) + " captured!</b><br>" +
                            "Now position your face: <b>" + nextAngleName + "</b><br>" +
                            "Captured: " + capturedFaces.size() + "/8 angles<br>" +
                            "Click 'Capture Current Angle' for next angle</center></html>");
                        captureButton.setEnabled(true);
                    }
                    isProcessing.set(false);
                });
                
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
    
    private void finishRegistration() {
        if (capturedFaces.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No faces captured. Please capture at least one angle first.",
                "Registration Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
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

