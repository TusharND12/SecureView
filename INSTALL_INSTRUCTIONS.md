# SecureView Installation Instructions

## Quick Summary
**SecureView** is a face recognition security system for your laptop that:
- 🔒 Locks your laptop automatically on boot
- 👤 Unlocks using face recognition
- 📱 Sends alerts to your phone when someone tries to access your laptop
- 🚨 Captures photos of unauthorized access attempts

## Installation Steps

### Step 1: Install Java 11
```bash
sudo apt update
sudo apt install -y openjdk-11-jdk
```

Verify installation:
```bash
java -version
```

### Step 2: Install Maven
```bash
sudo apt install -y maven
```

Verify installation:
```bash
mvn -version
```

### Step 3: Build the Project
```bash
cd SecureView/desktop-app
mvn clean package
```

This will create: `target/secureview-desktop-1.0.0.jar`

### Step 4: Run the Application
```bash
java -jar target/secureview-desktop-1.0.0.jar
```

## What You Need to Configure

### 1. Firebase Setup (for mobile alerts)
- Create a Firebase project at https://console.firebase.google.com
- Generate a service account key (JSON file)
- Add the path to config file: `~/.secureview/config.json`

### 2. OpenCV (for face recognition)
- Download from https://opencv.org/releases/
- Extract to a directory
- Set environment variable: `export OPENCV_DIR=/path/to/opencv`

**Note:** The app will work without OpenCV but face recognition features will be limited.

### 3. First-Time Registration
- When you first run the app, it will ask you to register your face
- Position your face in front of the camera
- Click "Capture Face" and "Finish Registration"

## Quick Run (After Installation)

```bash
# Navigate to desktop app
cd SecureView/desktop-app

# Build
mvn clean package

# Run
java -jar target/secureview-desktop-1.0.0.jar
```

## Troubleshooting

**Java not found?**
- Make sure Java 11+ is installed: `java -version`
- If installed but not found, add to PATH

**Maven not found?**
- Install with: `sudo apt install maven`
- Verify: `mvn -version`

**Build fails?**
- Check internet connection (Maven downloads dependencies)
- Ensure Java 11+ is installed
- Check error messages for specific issues

**Camera not working?**
- Check camera permissions
- Ensure no other app is using the camera
- Try different camera index in code

## Project Structure

```
SecureView/
├── desktop-app/          # Java desktop application (main app)
│   └── src/main/java/    # Source code
├── android-app/          # Android app (for receiving alerts)
└── README.md            # Full documentation
```

## Next Steps

1. Install Java and Maven (see above)
2. Build the project
3. Configure Firebase (optional, for mobile alerts)
4. Run and register your face
5. Test the authentication

For detailed setup, see `SETUP_GUIDE.md` or `README.md`

