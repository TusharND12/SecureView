# SecureView - Advanced Laptop Authentication System

SecureView is a comprehensive face recognition-based authentication system for laptops that provides real-time intrusion detection and mobile alerts. The system automatically starts on boot, authenticates users through facial recognition, and sends instant alerts to your smartphone when unauthorized access is detected.

## Features

### Core Functionality
- **Automatic Boot Startup**: Starts automatically when the laptop boots
- **Face Recognition**: Advanced face detection and recognition using OpenCV
- **Face Embedding Comparison**: Secure biometric comparison using cosine similarity
- **Encrypted Storage**: AES-256-GCM encryption for biometric data
- **Liveness Detection**: Prevents spoofing attacks using photos or videos
- **Intrusion Detection**: Real-time detection of unauthorized access attempts
- **Mobile Alerts**: Instant push notifications via Firebase Cloud Messaging
- **Attempt Logging**: Comprehensive logging of all authentication attempts

### Security Features
- **Mismatch Threshold Detection**: Configurable similarity threshold
- **Lockout Mechanism**: Automatic system lockout after failed attempts
- **Intruder Image Capture**: Automatic capture and storage of intruder images
- **Secure Communication**: Encrypted communication between laptop and mobile device
- **Remote Actions**: Lock laptop or trigger alarm from mobile app

## Architecture

### Desktop Application (Java)
- **Face Detection Module**: OpenCV-based face detection
- **Face Recognition Module**: Deep learning-based face embedding extraction
- **Liveness Detection Module**: Multi-factor liveness verification
- **Encryption Service**: AES-256-GCM for data protection
- **Firebase Integration**: FCM for push notifications
- **Lock Manager**: System locking and alarm functionality
- **Attempt Logger**: Comprehensive event logging

### Android Application
- **FCM Service**: Receives push notifications
- **Intrusion Alert Activity**: Displays intruder images and details
- **Remote Action Support**: Send commands to desktop app
- **Token Management**: FCM token generation and management

## Prerequisites

### Desktop Application
- Java 11 or higher
- Maven 3.6+
- OpenCV 4.8.0+
- Firebase Admin SDK credentials
- Webcam

### Android Application
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0+)
- Firebase project with FCM enabled
- Google Services JSON file

## Installation

### Desktop Application Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd SecureView/desktop-app
   ```

2. **Install OpenCV**
   - Download OpenCV from https://opencv.org/releases/
   - Extract and set environment variable `OPENCV_DIR`
   - Or use Maven dependency (included in pom.xml)

3. **Configure Firebase**
   - Create a Firebase project at https://console.firebase.google.com
   - Generate a service account key (JSON file)
   - Place the JSON file in a secure location
   - Update `config.json` with:
     - `firebaseProjectId`: Your Firebase project ID
     - `firebaseCredentialsPath`: Path to service account JSON file

4. **Build the application**
   ```bash
   mvn clean package
   ```

5. **Run the application**
   ```bash
   java -jar target/secureview-desktop-1.0.0.jar
   ```

6. **Enable Auto-Start** (Optional)
   ```java
   // Use AutoStartManager to enable auto-start
   AutoStartManager.enableAutoStart("path/to/secureview-desktop-1.0.0.jar");
   ```

### Android Application Setup

1. **Open in Android Studio**
   ```bash
   cd SecureView/android-app
   # Open in Android Studio
   ```

2. **Configure Firebase**
   - Add `google-services.json` to `app/` directory
   - Ensure Firebase project matches desktop app configuration

3. **Build and Install**
   - Build the APK in Android Studio
   - Install on your Android device

4. **Get FCM Token**
   - Launch the app
   - Copy the device token displayed
   - Add to desktop app configuration:
     ```json
     {
       "deviceToken": "your-fcm-token-here"
     }
     ```

## Configuration

### Desktop Application Configuration

Edit `~/.secureview/config.json`:

```json
{
  "faceRecognitionThreshold": 0.6,
  "livenessDetectionEnabled": true,
  "maxFailedAttempts": 3,
  "lockoutDuration": 300000,
  "firebaseProjectId": "your-project-id",
  "firebaseCredentialsPath": "path/to/service-account.json",
  "deviceToken": "your-fcm-token",
  "dataDirectory": "~/.secureview/data",
  "logsDirectory": "~/.secureview/logs"
}
```

### Configuration Parameters

- **faceRecognitionThreshold** (0.0-1.0): Similarity threshold for authentication
  - Lower = more strict (0.7-0.9 recommended)
  - Higher = more lenient (0.5-0.7 recommended)

- **livenessDetectionEnabled** (boolean): Enable/disable liveness detection

- **maxFailedAttempts** (integer): Number of failed attempts before lockout

- **lockoutDuration** (milliseconds): Duration of lockout period

## Usage

### First-Time Setup

1. **Launch the application**
   - On first launch, registration window will appear

2. **Register Your Face**
   - Position your face in front of the camera
   - Click "Capture Face"
   - Click "Finish Registration"

3. **Configure Firebase** (if not done during installation)
   - Add Firebase credentials path
   - Add device token from Android app

### Daily Usage

1. **Boot your laptop**
   - SecureView starts automatically (if auto-start enabled)
   - Authentication window appears

2. **Authenticate**
   - Position your face in front of the camera
   - System unlocks automatically upon successful recognition

3. **Intrusion Detection**
   - If unauthorized person attempts access:
     - System locks automatically
     - Intruder image is captured
     - Alert is sent to your mobile device
     - Alarm is triggered

### Mobile App Usage

1. **Receive Alerts**
   - Push notification appears when intrusion detected
   - Tap notification to view details

2. **View Intruder Image**
   - IntrusionAlertActivity displays captured image
   - View timestamp and details

3. **Remote Actions** (Future Enhancement)
   - Lock laptop remotely
   - Trigger alarm remotely

## Project Structure

```
SecureView/
├── desktop-app/
│   ├── src/main/java/com/secureview/desktop/
│   │   ├── SecureViewApplication.java
│   │   ├── AuthenticationWindow.java
│   │   ├── RegistrationWindow.java
│   │   ├── config/
│   │   ├── face/
│   │   │   ├── detection/
│   │   │   ├── embedding/
│   │   │   └── liveness/
│   │   ├── encryption/
│   │   ├── firebase/
│   │   ├── lock/
│   │   ├── logging/
│   │   └── autostart/
│   └── pom.xml
├── android-app/
│   ├── app/src/main/
│   │   ├── java/com/secureview/android/
│   │   └── res/
│   └── build.gradle
└── README.md
```

## Security Considerations

1. **Biometric Data Protection**
   - Face embeddings are encrypted using AES-256-GCM
   - Encryption key is stored securely in user directory

2. **Network Security**
   - FCM uses encrypted connections
   - Images are Base64 encoded for transmission

3. **Access Control**
   - System locks after failed attempts
   - Lockout period prevents brute force attacks

4. **Privacy**
   - Intruder images stored locally
   - Only sent to registered mobile device

## Troubleshooting

### Desktop Application

**Camera not detected**
- Ensure webcam is connected and working
- Check camera permissions
- Try different camera index (0, 1, 2...)

**OpenCV not found**
- Verify OpenCV installation
- Check environment variables
- Ensure native libraries are accessible

**Firebase connection failed**
- Verify credentials file path
- Check project ID
- Ensure service account has FCM permissions

### Android Application

**FCM token not generated**
- Check internet connection
- Verify Firebase configuration
- Ensure Google Play Services is updated

**Notifications not received**
- Verify device token in desktop config
- Check notification permissions
- Ensure app is not in battery optimization

## Future Enhancements

- [ ] Real-time remote actions via Firebase Realtime Database
- [ ] Multiple user support
- [ ] Face recognition model training interface
- [ ] Advanced liveness detection (3D face mapping)
- [ ] Biometric data backup/restore
- [ ] Web dashboard for monitoring
- [ ] Integration with Windows Hello

## License

Apache License 2.0 - See LICENSE file for details

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting pull requests.

## Support

For issues and questions, please open an issue on the GitHub repository.

## Acknowledgments

- OpenCV for face detection and recognition
- Firebase for cloud messaging
- Apache Commons for utilities

