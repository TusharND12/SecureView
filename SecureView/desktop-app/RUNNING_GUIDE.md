# SecureView - Running Guide

## Application Status

The application is now running! Here's what to expect:

## First-Time Run (No Registration Yet)

### What You'll See:
1. **Registration Window** will appear automatically
2. **Camera feed** showing your webcam
3. **Status message**: "Position your face in front of the camera and click 'Capture'"
4. **Two buttons**: 
   - "Capture Face" - Captures your face for registration
   - "Finish Registration" - Completes registration (disabled until face is captured)

### Steps to Register:
1. **Position yourself** in front of the camera
   - Ensure good lighting
   - Face the camera directly
   - Remove glasses/hats if needed
2. **Click "Capture Face"**
   - Wait for "Face captured!" message
   - "Finish Registration" button will be enabled
3. **Click "Finish Registration"**
   - System processes your face
   - Extracts face embedding
   - Encrypts and stores biometric data
   - Shows success message
4. **Automatic Transition**
   - Registration window closes
   - Authentication window opens automatically

## After Registration (Authentication Window)

### What You'll See:
1. **Live Camera Feed** - Continuous monitoring
2. **Status Updates**:
   - "Position your face in front of the camera"
   - "Face detected. Authenticating..."
   - "Authentication successful!" (if match)
   - "Authentication failed. Attempts: X" (if no match)
3. **Progress Bar** - Shows authentication status

### Authentication Process:
- **Continuous Scanning**: Checks for faces ~10 times per second
- **Automatic Recognition**: Compares detected face with registered face
- **Success**: Window closes after 1 second, application exits
- **Failure**: Shows similarity score and increments attempt counter

## Intrusion Detection

### What Happens:
After **3 failed attempts** (configurable):
1. 🚨 **Intrusion Alert Triggered**
2. 📸 **Intruder Image Captured** - Saved to disk
3. 📱 **Mobile Alert Sent** - Push notification to Android app (if configured)
4. 🔒 **System Locked** - Windows system locks
5. ⏰ **Lockout Period** - 5 minutes (configurable)

### During Lockout:
- Status shows: "System locked. Please wait X seconds"
- No authentication attempts processed
- System remains locked

## Configuration Status

### ✅ Working Without Configuration:
- Face detection and recognition
- Registration and authentication
- Intrusion detection
- System locking
- Local logging

### ⚠️ Requires Configuration:
- **Firebase/Mobile Alerts**: 
  - Need Firebase project setup
  - Need service account JSON file
  - Need Android app FCM token
  - See `SETUP_GUIDE.md` for details

### Current Configuration:
The app will create a default config file at:
- **Windows**: `C:\Users\<YourUsername>\.secureview\config.json`

Default settings:
- Face recognition threshold: 0.6
- Max failed attempts: 3
- Lockout duration: 5 minutes (300000 ms)
- Liveness detection: Enabled

## Troubleshooting

### "No face detected"
- ✅ Ensure good lighting
- ✅ Face camera directly
- ✅ Remove obstructions (glasses, hats)
- ✅ Check camera permissions

### "OpenCV not found"
- ✅ Set `OPENCV_DIR` environment variable
- ✅ Or place OpenCV in `C:\opencv`
- ✅ Application will show warning but continue

### "Firebase not configured"
- ⚠️ This is OK for testing
- ⚠️ Mobile alerts won't work
- ⚠️ All other features work normally
- ✅ See `SETUP_GUIDE.md` to configure Firebase

### Application Window Not Appearing
- Check console for errors
- Verify Java is installed: `java -version`
- Check camera is not in use by another app

## Quick Commands

### Stop Application:
- Close the window (after authentication)
- Or press `Ctrl+C` in terminal

### Restart Application:
```bash
cd SecureView/desktop-app
.\run.bat
```

### Check Logs:
- Location: `~/.secureview/logs/`
- View recent logs for debugging

## Next Steps

1. **Test Registration**: Register your face
2. **Test Authentication**: Try authenticating
3. **Test Intrusion**: Cover camera or use different face
4. **Configure Firebase** (optional): Set up mobile alerts
5. **Enable Auto-Start** (optional): Start on Windows boot

## Support

- See `README.md` for full documentation
- See `SETUP_GUIDE.md` for detailed setup
- See `REGISTRATION_FLOW.md` for authentication flow
- Check logs in `~/.secureview/logs/` for errors

---

**The application is running and ready to use!** 🚀

