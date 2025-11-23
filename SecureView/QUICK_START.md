# SecureView Quick Start Guide

Get SecureView up and running in 5 minutes!

## Prerequisites Checklist

- [ ] Java 11+ installed
- [ ] Maven installed
- [ ] Webcam connected
- [ ] Android device (for mobile alerts)
- [ ] Firebase account

## Step 1: Desktop Application (5 minutes)

### Build
```bash
cd SecureView/desktop-app
mvn clean package
```

### Configure Firebase
1. Create Firebase project at https://console.firebase.google.com
2. Generate service account key (JSON)
3. Save JSON file path

### Run
```bash
java -jar target/secureview-desktop-1.0.0.jar
```

### Register Your Face
1. Position face in camera
2. Click "Capture Face"
3. Click "Finish Registration"

## Step 2: Android Application (5 minutes)

### Setup
1. Open `SecureView/android-app` in Android Studio
2. Add `google-services.json` to `app/` directory
3. Build and install on device

### Get Token
1. Launch app
2. Copy FCM token displayed
3. Add to desktop config: `~/.secureview/config.json`
   ```json
   {
     "deviceToken": "your-token-here",
     "firebaseProjectId": "your-project-id",
     "firebaseCredentialsPath": "path/to/service-account.json"
   }
   ```

## Step 3: Test

1. Restart desktop application
2. Authenticate with your face
3. Test intrusion: Cover camera or use different face
4. Check Android app for alert

## Troubleshooting

**Camera not working?**
- Check camera permissions
- Ensure no other app is using camera

**Firebase not connecting?**
- Verify credentials file path
- Check project ID matches

**No alerts received?**
- Verify device token in config
- Check internet connection
- Ensure app has notification permissions

## Next Steps

- Enable auto-start (see SETUP_GUIDE.md)
- Adjust recognition threshold in config
- Customize lockout settings

For detailed setup, see SETUP_GUIDE.md

