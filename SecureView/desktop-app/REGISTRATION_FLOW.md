# SecureView - Registration and Authentication Flow

## What Happens After Registration

### 1. **Immediate Post-Registration** ✅
After you successfully register your face:
- Your face embedding is extracted and **encrypted** using AES-256-GCM
- The encrypted biometric data is stored securely in the application data directory
- A success message is displayed
- The registration window closes
- **NEW**: The application automatically transitions to the **Authentication Window** (no need to restart!)

### 2. **Authentication Window** 🔐
The Authentication Window is the main security interface that:

#### **Continuous Monitoring**
- Opens your webcam automatically
- Displays live camera feed (640x480 resolution)
- Continuously scans for faces at ~10 frames per second
- Shows real-time status updates

#### **Authentication Process**
When a face is detected:
1. **Face Detection**: Detects if a face is present in the frame
2. **Face Recognition**: Compares the detected face with your registered face embedding
3. **Similarity Score**: Calculates how similar the face is (0.0 to 1.0)
4. **Threshold Check**: Compares against configured threshold (default: 0.85)

### 3. **Authentication Success** ✅
If your face matches (similarity ≥ threshold):
- ✅ Status shows "Authentication successful!"
- ✅ Success is logged
- ✅ Failed attempt counter resets
- ✅ Window closes after 1 second
- ✅ Application exits (system is unlocked)

### 4. **Authentication Failure** ❌
If face doesn't match (similarity < threshold):
- ❌ Status shows "Authentication failed. Attempts: X"
- ❌ Displays similarity score
- ❌ Failure is logged with timestamp
- ❌ Failed attempt counter increments
- ⚠️ **After max failed attempts** (default: 3), triggers **Intrusion Detection**

### 5. **Intrusion Detection** 🚨
When maximum failed attempts are reached:

#### **Immediate Actions:**
1. **Saves Intruder Image**: Captures and saves the intruder's face image with timestamp
2. **Logs Intrusion**: Records detailed intrusion event in logs
3. **Sends Alert**: Sends push notification to your mobile device via Firebase Cloud Messaging (FCM)
   - Includes intruder's face image
   - Includes timestamp and details
4. **Locks System**: 
   - Locks the Windows system
   - Triggers alarm (if configured)
   - Prevents further authentication attempts
5. **Lockout Period**: System remains locked for configured duration (default: 5 minutes)

#### **Mobile Alert Includes:**
- Intruder's face snapshot
- Timestamp of intrusion
- Number of failed attempts
- Similarity scores
- Option to view full details in Android app

### 6. **Lockout Period** 🔒
During lockout:
- Authentication window shows: "System locked. Please wait X seconds"
- No authentication attempts are processed
- System remains locked
- After lockout expires, authentication resumes

### 7. **Security Features** 🛡️

#### **Liveness Detection** (if enabled):
- Detects if the face is from a real person (not a photo/video)
- Analyzes movement, texture, and 3D structure
- Prevents spoofing attacks

#### **Attempt Logging**:
- All authentication attempts are logged
- Includes success/failure, timestamps, similarity scores
- Logs stored in encrypted format

#### **Encrypted Storage**:
- Face embeddings stored with AES-256-GCM encryption
- Intruder images stored securely
- Configuration data encrypted

### 8. **Window Behavior** 🪟
- **Cannot be closed** without authentication
- Closing attempt shows: "Please authenticate to close this window"
- Only closes after successful authentication
- Runs in full-screen mode (configurable)

## Configuration Options

You can configure:
- **Face Recognition Threshold**: How similar face must be (0.0-1.0)
- **Max Failed Attempts**: Before intrusion is triggered
- **Lockout Duration**: How long system stays locked (milliseconds)
- **Liveness Detection**: Enable/disable anti-spoofing
- **Camera Resolution**: Frame size for processing

## Next Steps After Registration

1. **Test Authentication**: Position your face in front of the camera
2. **Monitor Logs**: Check logs for authentication events
3. **Setup Mobile App**: Install Android app to receive intrusion alerts
4. **Configure Settings**: Adjust thresholds and security settings as needed
5. **Auto-Start Setup**: Configure application to start on Windows boot (optional)

## Troubleshooting

### "No face detected"
- Ensure good lighting
- Face camera directly
- Remove glasses/hats if needed
- Check camera permissions

### "Authentication failed" (even with your face)
- Lower the recognition threshold in config
- Re-register with better lighting
- Ensure face is clearly visible
- Check if liveness detection is too strict

### "System locked"
- Wait for lockout period to expire
- Check logs for intrusion events
- Review mobile alerts if configured

---

**Note**: The application is designed to be a security layer. After successful authentication, the window closes and the application exits, allowing normal system use. The application should be configured to start automatically on boot for continuous protection.

