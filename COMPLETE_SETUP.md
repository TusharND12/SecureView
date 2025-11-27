# 🚀 Complete Setup Guide - SecureView with OpenCV

## ✅ What's Already Done

- ✅ Repository cloned
- ✅ Java 11 installed
- ✅ Maven installed
- ✅ Project structure ready

## 📦 What You Need to Install Now

### OpenCV Installation (Choose One Method)

#### **Option 1: Quick Install (5 minutes) - Try This First**

```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./quick-install-opencv.sh
```

This installs OpenCV via package manager. If Java bindings are missing, use Option 2.

#### **Option 2: Full Build with Java Support (30-60 minutes)**

This builds OpenCV from source with full Java bindings:

```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./install-opencv-linux.sh
```

**Note:** This will take 30-60 minutes to compile, but ensures all features work.

#### **Option 3: Manual Quick Install**

```bash
sudo apt update
sudo apt install -y libopencv-dev libopencv-contrib-dev
```

Then set environment variable:
```bash
export OPENCV_DIR=/usr
echo 'export OPENCV_DIR="/usr"' >> ~/.bashrc
source ~/.bashrc
```

---

## 🔨 Build and Run SecureView

After OpenCV is installed:

### Step 1: Build the Project

```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView/SecureView/desktop-app
mvn clean package
```

### Step 2: Run the Application

```bash
java -jar target/secureview-desktop-1.0.0.jar
```

---

## 📋 Complete Command Sequence

Here's everything in order:

```bash
# 1. Install OpenCV (choose one)
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./quick-install-opencv.sh          # Quick (5 min)
# OR
./install-opencv-linux.sh         # Full build (30-60 min)

# 2. Restart terminal or reload environment
source ~/.bashrc

# 3. Build SecureView
cd SecureView/desktop-app
mvn clean package

# 4. Run SecureView
java -jar target/secureview-desktop-1.0.0.jar
```

---

## 🎯 What SecureView Does

**SecureView** is a face recognition security system that:

1. **🔒 Auto-locks** your laptop on boot
2. **👤 Face unlock** - Uses webcam to recognize your face
3. **🚨 Intrusion detection** - Detects unauthorized access
4. **📱 Mobile alerts** - Sends photos to your phone (if Firebase configured)
5. **🔐 Auto-lockout** - Locks after failed attempts

---

## 🛠️ Troubleshooting

### OpenCV Not Found?
```bash
# Check if OPENCV_DIR is set
echo $OPENCV_DIR

# If not set, find OpenCV
find /usr -name "libopencv*.so" 2>/dev/null | head -1

# Set it manually
export OPENCV_DIR=/usr
export LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH
```

### Build Fails?
- Check Java: `java -version` (should be 11+)
- Check Maven: `mvn -version`
- Check internet connection (Maven downloads dependencies)

### Camera Not Working?
- Check camera permissions
- Ensure no other app is using camera
- On Linux: may need to grant camera access

### OpenCV Java Bindings Missing?
- Use the full build script: `./install-opencv-linux.sh`
- This ensures Java bindings are included

---

## 📁 Project Files Created

- `install-opencv-linux.sh` - Full OpenCV build script
- `quick-install-opencv.sh` - Quick OpenCV install
- `install-and-run.sh` - Complete setup script
- `INSTALL_OPENCV.md` - Detailed OpenCV guide
- `COMPLETE_SETUP.md` - This file

---

## 🎉 Next Steps After Running

1. **First Launch:** Registration window will appear
   - Position your face in front of camera
   - Click "Capture Face"
   - Click "Finish Registration"

2. **Configure Firebase** (Optional, for mobile alerts):
   - Create Firebase project
   - Add service account key
   - Configure in `~/.secureview/config.json`

3. **Set up Android App** (Optional):
   - Open `android-app` in Android Studio
   - Build and install on phone
   - Get FCM token for desktop config

---

## 💡 Quick Reference

```bash
# Check installations
java -version          # Should show Java 11+
mvn -version          # Should show Maven 3.x
echo $OPENCV_DIR      # Should show OpenCV path

# Build project
cd SecureView/desktop-app
mvn clean package

# Run project
java -jar target/secureview-desktop-1.0.0.jar
```

---

**Ready to go!** Run the OpenCV installation script and then build the project! 🚀

