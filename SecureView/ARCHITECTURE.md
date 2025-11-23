# SecureView Architecture Documentation

## System Overview

SecureView is a modular, secure face recognition-based authentication system with real-time intrusion detection and mobile alerting capabilities. The system consists of two main components:

1. **Desktop Application** (Java): Runs on Windows laptops
2. **Android Application** (Java/Kotlin): Receives alerts on mobile devices

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Desktop Application                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Face       │  │   Firebase   │  │    Lock     │     │
│  │ Recognition  │→ │   Service    │→ │   Manager    │     │
│  │   Service    │  │              │  │              │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                  │                  │            │
│         └──────────────────┼──────────────────┘            │
│                            │                                │
│                   ┌────────▼────────┐                       │
│                   │  Authentication │                       │
│                   │     Window      │                       │
│                   └─────────────────┘                       │
└────────────────────────────┬────────────────────────────────┘
                             │
                             │ FCM
                             │
┌────────────────────────────▼────────────────────────────────┐
│                  Android Application                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   FCM        │  │  Intrusion   │  │   Remote     │     │
│  │  Service     │→ │   Alert      │→ │   Actions    │     │
│  │              │  │   Activity   │  │              │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Desktop Application Architecture

### Core Components

#### 1. SecureViewApplication
- **Purpose**: Main entry point and application coordinator
- **Responsibilities**:
  - Initialize OpenCV native library
  - Initialize all services
  - Determine if user registration is needed
  - Launch appropriate window (Registration or Authentication)

#### 2. Face Recognition Service
- **Purpose**: Central service for face recognition operations
- **Components**:
  - `FaceDetector`: Detects faces in images using Haar Cascade
  - `FaceEmbeddingExtractor`: Extracts face embeddings using DNN models
  - `LivenessDetector`: Verifies liveness to prevent spoofing
- **Responsibilities**:
  - Face detection
  - Face embedding extraction
  - Face comparison using cosine similarity
  - User registration
  - User authentication

#### 3. Encryption Service
- **Purpose**: Secure storage of biometric data
- **Algorithm**: AES-256-GCM
- **Responsibilities**:
  - Encrypt face embeddings before storage
  - Decrypt face embeddings for comparison
  - Key management

#### 4. Firebase Service
- **Purpose**: Cloud messaging for intrusion alerts
- **Responsibilities**:
  - Initialize Firebase Admin SDK
  - Send intrusion alerts with images
  - Handle FCM token management

#### 5. Lock Manager
- **Purpose**: System locking and alarm functionality
- **Responsibilities**:
  - Lock Windows system
  - Trigger alarms
  - Execute remote actions from mobile app

#### 6. Attempt Logger
- **Purpose**: Logging and audit trail
- **Responsibilities**:
  - Log successful authentications
  - Log failed attempts
  - Log intrusion events
  - Track failed attempt counts

#### 7. Configuration Manager
- **Purpose**: Application configuration management
- **Storage**: JSON file in user directory
- **Responsibilities**:
  - Load/save configuration
  - Provide default values
  - Manage configuration directories

### UI Components

#### AuthenticationWindow
- **Purpose**: Main authentication interface
- **Features**:
  - Real-time camera feed
  - Face detection visualization
  - Authentication status display
  - Automatic authentication on face match
  - Intrusion detection and alerting

#### RegistrationWindow
- **Purpose**: First-time user registration
- **Features**:
  - Camera feed preview
  - Face capture button
  - Registration completion

### Data Flow

#### Authentication Flow
```
1. Camera captures frame
2. FaceDetector detects face
3. FaceEmbeddingExtractor extracts embedding
4. Load stored encrypted embedding
5. Decrypt stored embedding
6. Calculate cosine similarity
7. Compare with threshold
8. If match: Unlock system
9. If mismatch: Increment failed attempts
10. If threshold exceeded: Trigger intrusion alert
```

#### Intrusion Alert Flow
```
1. Failed attempts exceed threshold
2. Capture intruder image
3. Save image to disk
4. Log intrusion event
5. Encrypt image (Base64)
6. Send FCM message with image
7. Lock system
8. Trigger alarm
9. Start lockout period
```

## Android Application Architecture

### Core Components

#### 1. MainActivity
- **Purpose**: Main interface and token management
- **Features**:
  - Display FCM token
  - Copy token to clipboard
  - Token refresh handling

#### 2. SecureViewMessagingService
- **Purpose**: Handle incoming FCM messages
- **Responsibilities**:
  - Receive push notifications
  - Parse message data
  - Display notifications
  - Launch IntrusionAlertActivity

#### 3. IntrusionAlertActivity
- **Purpose**: Display intrusion details
- **Features**:
  - Display intruder image
  - Show timestamp and details
  - Remote action buttons (future)

#### 4. BootReceiver
- **Purpose**: Handle boot events
- **Responsibilities**:
  - Refresh FCM token on boot
  - Ensure service availability

### Data Flow

#### Notification Reception Flow
```
1. FCM message received
2. SecureViewMessagingService processes message
3. Extract image, timestamp, details
4. Create notification
5. Launch IntrusionAlertActivity
6. Display intruder image and details
```

## Security Architecture

### Encryption

#### Biometric Data Encryption
- **Algorithm**: AES-256-GCM
- **Key Storage**: User directory (`~/.secureview/encryption.key`)
- **Key Generation**: SecureRandom
- **IV**: 12 bytes, randomly generated per encryption

#### Data in Transit
- **Protocol**: HTTPS (FCM)
- **Image Encoding**: Base64
- **Authentication**: Firebase service account credentials

### Access Control

#### Authentication Threshold
- Configurable similarity threshold (0.0-1.0)
- Default: 0.6
- Cosine similarity comparison

#### Lockout Mechanism
- Configurable max failed attempts
- Configurable lockout duration
- Automatic system lock on threshold

### Privacy

#### Data Storage
- Face embeddings: Encrypted, local storage only
- Intruder images: Local storage, sent only to registered device
- Logs: Local storage only

#### Data Transmission
- Only intrusion alerts sent to mobile
- No continuous monitoring data
- Images sent only on intrusion detection

## Module Dependencies

### Desktop Application
```
SecureViewApplication
├── ConfigManager
├── FaceRecognitionService
│   ├── FaceDetector
│   ├── FaceEmbeddingExtractor
│   └── LivenessDetector
├── EncryptionService
├── FirebaseService
├── LockManager
├── AttemptLogger
└── UI Components
    ├── AuthenticationWindow
    └── RegistrationWindow
```

### Android Application
```
MainActivity
├── SecureViewMessagingService
│   └── IntrusionAlertActivity
└── BootReceiver
```

## Configuration

### Desktop Configuration File
Location: `~/.secureview/config.json`

```json
{
  "faceRecognitionThreshold": 0.6,
  "livenessDetectionEnabled": true,
  "maxFailedAttempts": 3,
  "lockoutDuration": 300000,
  "firebaseProjectId": "project-id",
  "firebaseCredentialsPath": "path/to/credentials.json",
  "deviceToken": "fcm-token",
  "dataDirectory": "~/.secureview/data",
  "logsDirectory": "~/.secureview/logs"
}
```

## Performance Considerations

### Face Detection
- **Frame Rate**: ~10 FPS (100ms intervals)
- **Processing**: Asynchronous to prevent UI blocking
- **Memory**: Mat objects released after processing

### Network
- **FCM Latency**: Typically < 1 second
- **Image Size**: Compressed before transmission
- **Retry Logic**: Built into FCM SDK

### Storage
- **Embedding Size**: ~1KB (128 dimensions × 8 bytes)
- **Encrypted Size**: ~1.2KB (with IV and tag)
- **Intruder Images**: JPEG compressed

## Extensibility

### Adding New Features

#### New Liveness Detection Method
1. Extend `LivenessDetector` class
2. Add new detection method
3. Integrate into `verifyLiveness()`

#### New Remote Action
1. Add action to `LockManager`
2. Update FCM message format
3. Add handler in Android app

#### Multiple User Support
1. Modify `FaceRecognitionService` to store multiple embeddings
2. Add user management UI
3. Update authentication flow

## Testing Strategy

### Unit Tests
- Face detection accuracy
- Embedding extraction
- Encryption/decryption
- Similarity calculation

### Integration Tests
- End-to-end authentication flow
- Intrusion detection flow
- FCM message delivery

### Performance Tests
- Face detection latency
- Authentication speed
- Memory usage

## Deployment

### Desktop Application
- JAR file with all dependencies
- Auto-start configuration
- Configuration file generation

### Android Application
- APK or AAB file
- Google Play Store distribution
- FCM configuration

## Future Enhancements

1. **Real-time Remote Actions**
   - Firebase Realtime Database integration
   - Bidirectional communication

2. **Advanced Liveness Detection**
   - 3D face mapping
   - Eye blink detection
   - Head movement tracking

3. **Multi-User Support**
   - Multiple registered users
   - User management interface

4. **Cloud Backup**
   - Encrypted backup of biometric data
   - Cross-device synchronization

5. **Web Dashboard**
   - Monitoring interface
   - Historical data visualization
   - Configuration management

