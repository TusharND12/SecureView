package com.secureview.desktop;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.face.FaceRecognitionService;
import com.secureview.desktop.firebase.FirebaseService;
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
    private Mat capturedFace = null;
    
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
        statusLabel = new JLabel("Position your face in front of the camera and click 'Capture'", 
            JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        captureButton = new JButton("Capture Face");
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
            camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                throw new Exception("Failed to open camera");
            }
            
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
            
            captureTimer = new Timer(33, e -> updateCameraFeed());
            captureTimer.start();
            
            logger.info("Camera started for registration");
            
        } catch (Exception e) {
            logger.error("Failed to start camera", e);
            statusLabel.setText("Error: Failed to start camera");
            JOptionPane.showMessageDialog(this,
                "Failed to start camera: " + e.getMessage(),
                "Camera Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCameraFeed() {
        if (isProcessing.get()) {
            return;
        }
        
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) {
            frame.release();
            return;
        }
        
        // Check for face
        Mat face = faceRecognitionService.detectFace(frame);
        if (face != null && !face.empty()) {
            statusLabel.setText("Face detected! Click 'Capture Face' to register.");
            face.release();
        } else {
            statusLabel.setText("No face detected. Please position your face in front of the camera.");
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
        
        isProcessing.set(true);
        captureButton.setEnabled(false);
        statusLabel.setText("Capturing face...");
        
        new Thread(() -> {
            try {
                Mat frame = new Mat();
                if (!camera.read(frame) || frame.empty()) {
                    statusLabel.setText("Failed to capture frame");
                    isProcessing.set(false);
                    captureButton.setEnabled(true);
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
                
                // Store captured face
                if (capturedFace != null) {
                    capturedFace.release();
                }
                capturedFace = new Mat();
                face.copyTo(capturedFace);
                face.release();
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Face captured! Click 'Finish Registration' to complete.");
                    finishButton.setEnabled(true);
                    isProcessing.set(false);
                    captureButton.setEnabled(true);
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
        if (capturedFace == null || capturedFace.empty()) {
            JOptionPane.showMessageDialog(this,
                "No face captured. Please capture a face first.",
                "Registration Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        finishButton.setEnabled(false);
        statusLabel.setText("Registering user...");
        
        new Thread(() -> {
            try {
                boolean success = faceRecognitionService.registerUser(capturedFace);
                
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                            "User registered successfully!\n\n" +
                            "You can now use SecureView for authentication.",
                            "Registration Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        System.exit(0);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Registration failed. Please try again.");
                        finishButton.setEnabled(true);
                    });
                }
                
            } catch (Exception e) {
                logger.error("Error during registration", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Registration failed: " + e.getMessage(),
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                    finishButton.setEnabled(true);
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
    
    @Override
    public void dispose() {
        if (captureTimer != null) {
            captureTimer.stop();
        }
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        if (capturedFace != null) {
            capturedFace.release();
        }
        super.dispose();
    }
}

