# SecureView Setup Guide

This guide provides step-by-step instructions for setting up SecureView on both desktop and mobile platforms.

## Table of Contents

1. [Desktop Application Setup](#desktop-application-setup)
2. [Android Application Setup](#android-application-setup)
3. [Firebase Configuration](#firebase-configuration)
4. [First-Time Registration](#first-time-registration)
5. [Auto-Start Configuration](#auto-start-configuration)
6. [Troubleshooting](#troubleshooting)

## Desktop Application Setup

### Step 1: Install Java

1. Download and install Java 11 or higher from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
2. Verify installation:
   ```bash
   java -version
   ```

### Step 2: Install Maven

1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract and add to PATH
3. Verify installation:
   ```bash
   mvn -version
   ```

### Step 3: Install OpenCV

**Option A: Using Maven (Recommended)**
- OpenCV dependency is included in `pom.xml`
- Maven will download automatically during build

**Option B: Manual Installation**
1. Download OpenCV from https://opencv.org/releases/
2. Extract to a directory (e.g., `C:\opencv`)
3. Set environment variable:
   ```bash
   set OPENCV_DIR=C:\opencv
   ```
4. Add to PATH:
   ```bash
   set PATH=%PATH%;%OPENCV_DIR%\build\java\x64
   ```

### Step 4: Build the Application

```bash
cd SecureView/desktop-app
mvn clean package
```

This will create `target/secureview-desktop-1.0.0.jar`

### Step 5: Configure Firebase

See [Firebase Configuration](#firebase-configuration) section below.

### Step 6: Run the Application

```bash
java -jar target/secureview-desktop-1.0.0.jar
```

## Android Application Setup

### Step 1: Install Android Studio

1. Download from [Android Studio](https://developer.android.com/studio)
2. Install with default settings
3. Install Android SDK 24+ (Android 7.0+)

### Step 2: Open Project

1. Open Android Studio
2. File → Open → Select `SecureView/android-app`
3. Wait for Gradle sync to complete

### Step 3: Configure Firebase

1. Follow [Firebase Configuration](#firebase-configuration)
2. Download `google-services.json`
3. Place in `android-app/app/` directory

### Step 4: Build and Install

1. Connect Android device or start emulator
2. Click "Run" button in Android Studio
3. App will install and launch automatically

### Step 5: Get FCM Token

1. Launch the app
2. Copy the device token displayed
3. Save for desktop configuration

## Firebase Configuration

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Enter project name: "SecureView"
4. Follow setup wizard

### Step 2: Enable Cloud Messaging

1. In Firebase Console, go to "Cloud Messaging"
2. Click "Get Started"
3. Note your Server Key (for future use)

### Step 3: Generate Service Account Key

1. Go to Project Settings → Service Accounts
2. Click "Generate New Private Key"
3. Save JSON file securely
4. Note the file path

### Step 4: Add Android App to Firebase

1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.secureview.android`
3. Download `google-services.json`
4. Place in `android-app/app/` directory

### Step 5: Configure Desktop App

1. Edit `~/.secureview/config.json`:
   ```json
   {
     "firebaseProjectId": "your-project-id",
     "firebaseCredentialsPath": "C:\\path\\to\\service-account.json",
     "deviceToken": "your-fcm-token-from-android-app"
   }
   ```

## First-Time Registration

### Step 1: Launch Application

```bash
java -jar secureview-desktop-1.0.0.jar
```

### Step 2: Register Your Face

1. Registration window appears automatically
2. Position face in front of camera
3. Ensure good lighting
4. Click "Capture Face"
5. Click "Finish Registration"

### Step 3: Verify Registration

1. Application will close
2. Restart application
3. Authentication window should appear
4. Test authentication with your face

## Auto-Start Configuration

### Windows Startup Folder Method

1. Create shortcut to JAR file
2. Copy shortcut to:
   ```
   %APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup
   ```

### Registry Method (Requires Admin)

1. Open Registry Editor (regedit)
2. Navigate to:
   ```
   HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Run
   ```
3. Create new String Value:
   - Name: `SecureView`
   - Value: `java -jar "C:\path\to\secureview-desktop-1.0.0.jar"`

### Programmatic Method

Use `AutoStartManager` class:
```java
AutoStartManager.enableAutoStart("path/to/secureview-desktop-1.0.0.jar");
```

## Troubleshooting

### Desktop Application Issues

**Problem: Camera not detected**
- Solution: Check camera permissions in Windows Settings
- Try different camera index in code (0, 1, 2...)
- Ensure no other application is using the camera

**Problem: OpenCV native library error**
- Solution: Ensure OpenCV native libraries are in PATH
- Check `OPENCV_DIR` environment variable
- Verify Java architecture matches OpenCV (32-bit vs 64-bit)

**Problem: Firebase connection failed**
- Solution: Verify credentials file path is correct
- Check file permissions
- Ensure service account has FCM permissions
- Verify project ID matches

**Problem: Face not detected**
- Solution: Improve lighting conditions
- Ensure face is clearly visible
- Check camera focus
- Try adjusting face detection parameters

### Android Application Issues

**Problem: FCM token not generated**
- Solution: Check internet connection
- Verify `google-services.json` is in correct location
- Ensure Google Play Services is updated
- Check Firebase project configuration

**Problem: Notifications not received**
- Solution: Check notification permissions
- Verify device token in desktop config
- Ensure app is not in battery optimization
- Check Firebase Cloud Messaging status

**Problem: App crashes on launch**
- Solution: Check Android Studio logcat for errors
- Verify minimum SDK version (24+)
- Ensure all dependencies are synced
- Check for missing resources

### General Issues

**Problem: Authentication always fails**
- Solution: Re-register your face
- Adjust `faceRecognitionThreshold` in config
- Check lighting conditions
- Verify liveness detection is not too strict

**Problem: Intrusion alerts not sent**
- Solution: Verify Firebase configuration
- Check device token is correct
- Ensure internet connection is active
- Check Firebase service account permissions

## Additional Resources

- [OpenCV Documentation](https://docs.opencv.org/)
- [Firebase Cloud Messaging Guide](https://firebase.google.com/docs/cloud-messaging)
- [Java Documentation](https://docs.oracle.com/javase/11/)

## Support

For additional help, please:
1. Check the main README.md
2. Review error logs in `~/.secureview/logs/`
3. Open an issue on GitHub

