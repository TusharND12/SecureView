# 🚀 Quick Start - Run This Project

## Option 1: Automated Installation (Recommended)

I've created an automated script that will install everything and run the project for you.

**Just run this command:**
```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./install-and-run.sh
```

This script will:
1. ✅ Check if Java 11 is installed (install if needed)
2. ✅ Check if Maven is installed (install if needed)
3. ✅ Build the project automatically
4. ✅ Run the application

**Note:** You'll be prompted for your sudo password to install Java and Maven.

---

## Option 2: Manual Installation

If you prefer to install manually:

### Step 1: Install Dependencies
```bash
sudo apt update
sudo apt install -y openjdk-11-jdk maven
```

### Step 2: Build the Project
```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView/SecureView/desktop-app
mvn clean package
```

### Step 3: Run the Application
```bash
java -jar target/secureview-desktop-1.0.0.jar
```

---

## What to Expect

When you run the application:
1. **First Time:** A registration window will appear
   - Position your face in front of the camera
   - Click "Capture Face"
   - Click "Finish Registration"

2. **After Registration:** Authentication window appears
   - The app will try to recognize your face
   - If recognized, system unlocks
   - If not recognized, it locks and sends alerts (if Firebase is configured)

---

## Troubleshooting

**If the script asks for sudo password:**
- This is normal - it needs to install Java and Maven
- Enter your user password when prompted

**If build fails:**
- Check your internet connection (Maven downloads dependencies)
- Make sure Java 11+ is installed: `java -version`
- Make sure Maven is installed: `mvn -version`

**If camera doesn't work:**
- Check camera permissions
- Make sure no other app is using the camera
- On Linux, you may need to grant camera access

**If OpenCV warning appears:**
- The app will still run but face recognition features will be limited
- To enable full features, install OpenCV (see README.md)

---

## Next Steps After Running

1. **Configure Firebase** (optional, for mobile alerts):
   - Create Firebase project at https://console.firebase.google.com
   - Add service account key to config

2. **Set up Android app** (optional):
   - Open `android-app` in Android Studio
   - Build and install on your phone
   - Get FCM token and add to desktop config

3. **Enable auto-start** (optional):
   - See SETUP_GUIDE.md for instructions

---

## Quick Commands Reference

```bash
# Run the automated script
./install-and-run.sh

# Or manually:
cd SecureView/desktop-app
mvn clean package
java -jar target/secureview-desktop-1.0.0.jar

# Check Java version
java -version

# Check Maven version
mvn -version
```

