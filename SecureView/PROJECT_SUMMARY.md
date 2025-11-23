# SecureView Project Summary

## Overview

SecureView is a comprehensive laptop authentication system that uses face recognition to secure your device. It automatically starts on boot, authenticates users through facial recognition, and sends real-time alerts to your smartphone when unauthorized access is detected.

## Key Features Implemented

### ✅ Core Functionality
- [x] Automatic boot startup
- [x] Face detection using OpenCV
- [x] Face recognition with embedding comparison
- [x] Encrypted storage of biometric data (AES-256-GCM)
- [x] Liveness detection to prevent spoofing
- [x] Real-time intrusion detection
- [x] Firebase Cloud Messaging integration
- [x] Mobile push notifications with images
- [x] Comprehensive attempt logging
- [x] Configurable mismatch thresholds
- [x] Automatic system lockout
- [x] Intruder image capture and storage

### ✅ Desktop Application
- [x] Java-based application with Swing UI
- [x] Modular architecture
- [x] Face detection module
- [x] Face recognition module
- [x] Face embedding extraction
- [x] Liveness detection module
- [x] Encryption service
- [x] Firebase integration
- [x] Lock manager
- [x] Attempt logger
- [x] Auto-start manager
- [x] Configuration management
- [x] Registration window
- [x] Authentication window

### ✅ Android Application
- [x] FCM token generation
- [x] Push notification reception
- [x] Intrusion alert activity
- [x] Intruder image display
- [x] Remote action UI (framework)
- [x] Boot receiver
- [x] Notification channel management

### ✅ Documentation
- [x] Comprehensive README
- [x] Detailed setup guide
- [x] Architecture documentation
- [x] Quick start guide
- [x] Project structure documentation

## Technology Stack

### Desktop
- **Language**: Java 11
- **Build Tool**: Maven
- **Computer Vision**: OpenCV 4.8.0
- **Cloud Messaging**: Firebase Admin SDK 9.2.0
- **UI Framework**: Java Swing
- **Encryption**: Java Cryptography Extension (AES-256-GCM)
- **Logging**: SLF4J

### Android
- **Language**: Java
- **Build Tool**: Gradle
- **Cloud Messaging**: Firebase Cloud Messaging
- **UI Framework**: Android Views
- **Min SDK**: 24 (Android 7.0)

## Project Structure

```
SecureView/
├── desktop-app/              # Java desktop application
│   ├── src/main/java/       # Source code
│   └── pom.xml              # Maven configuration
├── android-app/              # Android application
│   ├── app/src/main/        # Android source
│   └── build.gradle         # Gradle configuration
├── README.md                 # Main documentation
├── SETUP_GUIDE.md           # Setup instructions
├── ARCHITECTURE.md          # Architecture docs
├── QUICK_START.md           # Quick start guide
└── LICENSE                  # Apache 2.0 license
```

## Security Features

1. **Biometric Data Protection**
   - AES-256-GCM encryption
   - Secure key storage
   - Local-only storage

2. **Authentication Security**
   - Configurable similarity threshold
   - Liveness detection
   - Lockout mechanism

3. **Network Security**
   - Encrypted FCM communication
   - Base64 image encoding
   - Service account authentication

4. **Privacy**
   - Local data storage
   - No continuous monitoring
   - Images sent only on intrusion

## Workflow

### Registration Flow
1. User launches application
2. Registration window appears
3. User positions face in camera
4. System captures and processes face
5. Face embedding extracted
6. Embedding encrypted and stored
7. Registration complete

### Authentication Flow
1. Application starts on boot
2. Authentication window appears
3. Camera captures frames continuously
4. Face detected in frame
5. Face embedding extracted
6. Compared with stored embedding
7. If match: System unlocks
8. If mismatch: Increment failed attempts
9. If threshold exceeded: Trigger intrusion alert

### Intrusion Alert Flow
1. Failed attempts exceed threshold
2. Intruder image captured
3. Image saved to disk
4. Event logged
5. FCM message sent with image
6. System locked
7. Alarm triggered
8. Lockout period started

## Configuration Options

- Face recognition threshold (0.0-1.0)
- Liveness detection enable/disable
- Max failed attempts
- Lockout duration
- Firebase project configuration
- Data and log directories

## Future Enhancements

- Real-time remote actions via Firebase Realtime Database
- Multiple user support
- Advanced 3D liveness detection
- Cloud backup of biometric data
- Web dashboard for monitoring
- Integration with Windows Hello
- Face recognition model training interface

## Testing Recommendations

1. **Unit Tests**
   - Face detection accuracy
   - Embedding extraction
   - Encryption/decryption
   - Similarity calculation

2. **Integration Tests**
   - End-to-end authentication
   - Intrusion detection flow
   - FCM message delivery

3. **Performance Tests**
   - Face detection latency
   - Authentication speed
   - Memory usage

4. **Security Tests**
   - Spoofing resistance
   - Encryption strength
   - Network security

## Deployment

### Desktop
- JAR file with dependencies
- Auto-start configuration
- Configuration file generation

### Android
- APK or AAB file
- Google Play Store distribution
- FCM configuration

## Support and Maintenance

- Error logging to `~/.secureview/logs/`
- Configuration in `~/.secureview/config.json`
- Intruder images in `~/.secureview/data/`

## License

Apache License 2.0

## Contributing

See main README.md for contributing guidelines.

