# Fix OpenCV Not Found Issue

## Problem
The application shows "OpenCV not found!" warning because:
1. OpenCV is not installed, OR
2. OpenCV is installed but the application can't find it (Linux vs Windows paths)

## Solution Options

### Option 1: Install OpenCV via Package Manager (Quick - 5 minutes)

Run these commands in your terminal:

```bash
sudo apt update
sudo apt install -y libopencv-dev libopencv-contrib-dev
```

Then run the application using the helper script:

```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./run-with-opencv.sh
```

### Option 2: Build OpenCV from Source (30-60 minutes)

This ensures Java bindings are included:

```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./install-opencv-linux.sh
```

After installation, restart your terminal and run:

```bash
cd SecureView/desktop-app
java -jar target/secureview-desktop-1.0.0.jar
```

### Option 3: Manual Setup

If OpenCV is already installed but not detected:

1. Find OpenCV libraries:
   ```bash
   find /usr -name "libopencv*.so" 2>/dev/null
   ```

2. Set environment variables:
   ```bash
   export LD_LIBRARY_PATH="/usr/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH"
   export OPENCV_DIR="/usr"
   ```

3. Run the application:
   ```bash
   cd SecureView/desktop-app
   java -Djava.library.path="$LD_LIBRARY_PATH" -jar target/secureview-desktop-1.0.0.jar
   ```

## Quick Fix Command

Run this to install OpenCV and run the app:

```bash
sudo apt update && sudo apt install -y libopencv-dev libopencv-contrib-dev && \
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView && \
./run-with-opencv.sh
```

## Verify OpenCV Installation

After installing, verify it works:

```bash
# Check if libraries are found
ldconfig -p | grep opencv

# Check if JAR exists (optional, for Java bindings)
find /usr -name "opencv*.jar" 2>/dev/null
```

## Note

The application will work without OpenCV but with **limited functionality**:
- ❌ Face recognition won't work
- ❌ Face detection won't work
- ✅ Other features may still work

For full functionality, OpenCV must be installed and properly configured.

