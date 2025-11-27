# OpenCV Installation Guide for Linux

## Quick Installation

I've created an automated script to install OpenCV with Java bindings. **This will take 30-60 minutes to build.**

### Option 1: Automated Installation (Recommended)

Run the installation script:
```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./install-opencv-linux.sh
```

**Note:** This script will:
- Install all build dependencies
- Download OpenCV 4.8.0 source code
- Build OpenCV with Java support (takes 30-60 minutes)
- Set up environment variables
- Configure everything for SecureView

### Option 2: Quick Install via Package Manager (Faster, but may not have Java bindings)

If you want a quicker installation (but may need additional setup):

```bash
sudo apt update
sudo apt install -y libopencv-dev libopencv-contrib-dev
```

Then you'll need to:
1. Find the OpenCV JAR file
2. Set OPENCV_DIR environment variable
3. Configure the native library path

## After Installation

1. **Restart your terminal** or run:
   ```bash
   source ~/.bashrc
   ```

2. **Verify installation:**
   ```bash
   echo $OPENCV_DIR
   ls $OPENCV_DIR/build/lib  # Should show .so files
   ```

3. **Build SecureView:**
   ```bash
   cd SecureView/desktop-app
   mvn clean package
   ```

4. **Run SecureView:**
   ```bash
   java -jar target/secureview-desktop-1.0.0.jar
   ```

## Manual Installation Steps

If you prefer to install manually:

### Step 1: Install Dependencies
```bash
sudo apt update
sudo apt install -y build-essential cmake git wget unzip \
    libopencv-dev libopencv-contrib-dev default-jdk ant
```

### Step 2: Download OpenCV
```bash
cd ~
wget https://github.com/opencv/opencv/archive/4.8.0.zip
unzip 4.8.0.zip
mv opencv-4.8.0 opencv
cd opencv
```

### Step 3: Build OpenCV with Java
```bash
mkdir build && cd build
cmake -D CMAKE_BUILD_TYPE=RELEASE \
      -D BUILD_JAVA=ON \
      -D BUILD_opencv_java=ON \
      ..
make -j$(nproc)
sudo make install
```

### Step 4: Set Environment Variables
```bash
echo 'export OPENCV_DIR="$HOME/opencv"' >> ~/.bashrc
echo 'export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/usr/local/lib"' >> ~/.bashrc
source ~/.bashrc
```

## Troubleshooting

**Build fails?**
- Make sure you have enough disk space (OpenCV build needs ~2GB)
- Check that all dependencies are installed
- Try building with fewer cores: `make -j2`

**Java bindings not found?**
- Ensure `BUILD_JAVA=ON` and `BUILD_opencv_java=ON` in CMake
- Check that Java JDK is installed: `java -version`
- Verify JAVA_HOME is set correctly

**Library not found at runtime?**
- Check LD_LIBRARY_PATH includes OpenCV lib directory
- Verify OPENCV_DIR is set correctly
- Try: `export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH`

## What Gets Installed

- OpenCV 4.8.0 source code in `~/opencv`
- Built libraries in `~/opencv/build/lib`
- Java JAR file in `~/opencv/build/bin/opencv-480.jar` (or similar)
- Native library: `libopencv_java480.so`

## Next Steps

After OpenCV is installed:
1. Build SecureView: `cd SecureView/desktop-app && mvn clean package`
2. Run the application: `java -jar target/secureview-desktop-1.0.0.jar`
3. Register your face when prompted

