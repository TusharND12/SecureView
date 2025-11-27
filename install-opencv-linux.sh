#!/bin/bash

# OpenCV Installation Script for Linux
# This script installs OpenCV 4.x with Java bindings

set -e

echo "========================================="
echo "OpenCV Installation for SecureView"
echo "========================================="
echo ""

# Check if running as root (we'll use sudo)
if [ "$EUID" -eq 0 ]; then 
    echo "Please don't run this script as root. It will use sudo when needed."
    exit 1
fi

# Step 1: Install build dependencies
echo "Step 1: Installing build dependencies..."
sudo apt update
sudo apt install -y \
    build-essential \
    cmake \
    git \
    wget \
    unzip \
    yasm \
    pkg-config \
    libswscale-dev \
    libtbb2 \
    libtbb-dev \
    libjpeg-dev \
    libpng-dev \
    libtiff-dev \
    libavformat-dev \
    libpq-dev \
    libxine2-dev \
    libglew-dev \
    libtiff5-dev \
    zlib1g-dev \
    libjpeg-dev \
    libavcodec-dev \
    libavformat-dev \
    libavutil-dev \
    libswscale-dev \
    libavresample-dev \
    libgstreamer1.0-dev \
    libgstreamer-plugins-base1.0-dev \
    libgtk2.0-dev \
    libgtk-3-dev \
    libv4l-dev \
    libxvidcore-dev \
    libx264-dev \
    libopencore-amrnb-dev \
    libopencore-amrwb-dev \
    libtheora-dev \
    libvorbis-dev \
    libxine2-dev \
    libtbb-dev \
    libeigen3-dev \
    python3-dev \
    python3-tk \
    python-imaging-tk \
    default-jdk \
    ant

echo "✓ Dependencies installed"
echo ""

# Step 2: Set installation directory
OPENCV_VERSION="4.8.0"
OPENCV_DIR="$HOME/opencv"
OPENCV_BUILD_DIR="$OPENCV_DIR/build"

echo "Step 2: Setting up OpenCV installation directory..."
mkdir -p "$OPENCV_DIR"
cd "$OPENCV_DIR"

echo "✓ Installation directory: $OPENCV_DIR"
echo ""

# Step 3: Download OpenCV
echo "Step 3: Downloading OpenCV ${OPENCV_VERSION}..."
if [ ! -d "opencv-${OPENCV_VERSION}" ]; then
    if [ ! -f "opencv-${OPENCV_VERSION}.zip" ]; then
        echo "Downloading OpenCV source (this may take a while)..."
        wget -O "opencv-${OPENCV_VERSION}.zip" "https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.zip"
    fi
    
    echo "Extracting OpenCV source..."
    unzip -q "opencv-${OPENCV_VERSION}.zip"
    mv "opencv-${OPENCV_VERSION}" "opencv"
else
    echo "OpenCV source already exists, skipping download"
    if [ ! -d "opencv" ]; then
        mv "opencv-${OPENCV_VERSION}" "opencv"
    fi
fi

echo "✓ OpenCV source ready"
echo ""

# Step 4: Download OpenCV contrib (optional, for extra modules)
echo "Step 4: Downloading OpenCV contrib modules..."
if [ ! -d "opencv_contrib" ]; then
    if [ ! -f "opencv_contrib-${OPENCV_VERSION}.zip" ]; then
        echo "Downloading OpenCV contrib (optional)..."
        wget -O "opencv_contrib-${OPENCV_VERSION}.zip" "https://github.com/opencv/opencv_contrib/archive/${OPENCV_VERSION}.zip" || echo "Contrib download failed, continuing without it"
    fi
    
    if [ -f "opencv_contrib-${OPENCV_VERSION}.zip" ]; then
        unzip -q "opencv_contrib-${OPENCV_VERSION}.zip"
        mv "opencv_contrib-${OPENCV_VERSION}" "opencv_contrib"
    fi
fi

echo "✓ Contrib modules ready (if downloaded)"
echo ""

# Step 5: Configure and build OpenCV with Java support
echo "Step 5: Configuring OpenCV build with Java support..."
echo "This will take 30-60 minutes depending on your system..."
echo ""

mkdir -p "$OPENCV_BUILD_DIR"
cd "$OPENCV_BUILD_DIR"

# Find Java paths
JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:bin/javac::")
JAVA_INCLUDE_PATH="$JAVA_HOME/include"
JAVA_INCLUDE_PATH2="$JAVA_HOME/include/linux"

# Configure with CMake
cmake -D CMAKE_BUILD_TYPE=RELEASE \
      -D CMAKE_INSTALL_PREFIX=/usr/local \
      -D INSTALL_PYTHON_EXAMPLES=OFF \
      -D INSTALL_C_EXAMPLES=OFF \
      -D OPENCV_ENABLE_NONFREE=ON \
      -D CMAKE_INSTALL_PREFIX="$OPENCV_DIR/install" \
      -D BUILD_JAVA=ON \
      -D BUILD_opencv_java=ON \
      -D JAVA_AWT_INCLUDE_PATH="$JAVA_INCLUDE_PATH" \
      -D JAVA_AWT_LIBRARY="$JAVA_HOME/lib/libjawt.so" \
      -D JAVA_INCLUDE_PATH="$JAVA_INCLUDE_PATH" \
      -D JAVA_INCLUDE_PATH2="$JAVA_INCLUDE_PATH2" \
      -D JAVA_JVM_LIBRARY="$JAVA_HOME/lib/server/libjvm.so" \
      -D WITH_OPENGL=ON \
      -D WITH_IPP=OFF \
      -D WITH_TBB=ON \
      -D WITH_EIGEN=ON \
      -D WITH_V4L=ON \
      -D BUILD_TESTS=OFF \
      -D BUILD_PERF_TESTS=OFF \
      -D BUILD_EXAMPLES=OFF \
      -D BUILD_DOCS=OFF \
      ../opencv

echo ""
echo "Step 6: Building OpenCV (this will take a while)..."
echo "Building with $(nproc) cores..."
make -j$(nproc)

echo ""
echo "Step 7: Installing OpenCV..."
sudo make install
sudo ldconfig

echo ""
echo "========================================="
echo "✓ OpenCV build and installation complete!"
echo "========================================="
echo ""

# Step 8: Set environment variables
echo "Step 8: Setting up environment variables..."

# Find the OpenCV JAR and native library
OPENCV_JAR=$(find "$OPENCV_BUILD_DIR" -name "opencv-*.jar" | head -1)
OPENCV_SO=$(find "$OPENCV_BUILD_DIR" -name "libopencv_java*.so" | head -1)

if [ -z "$OPENCV_JAR" ]; then
    # Try alternative location
    OPENCV_JAR=$(find /usr/local/share/opencv4 -name "opencv-*.jar" 2>/dev/null | head -1)
fi

if [ -z "$OPENCV_SO" ]; then
    OPENCV_SO=$(find /usr/local/lib -name "libopencv_java*.so" 2>/dev/null | head -1)
fi

echo "OpenCV JAR: $OPENCV_JAR"
echo "OpenCV SO: $OPENCV_SO"

# Add to bashrc
if ! grep -q "OPENCV_DIR" ~/.bashrc; then
    echo "" >> ~/.bashrc
    echo "# OpenCV Configuration for SecureView" >> ~/.bashrc
    echo "export OPENCV_DIR=\"$OPENCV_DIR\"" >> ~/.bashrc
    if [ -n "$OPENCV_JAR" ]; then
        echo "export OPENCV_JAR=\"$OPENCV_JAR\"" >> ~/.bashrc
    fi
    if [ -n "$OPENCV_SO" ]; then
        OPENCV_LIB_DIR=$(dirname "$OPENCV_SO")
        echo "export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH:$OPENCV_LIB_DIR\"" >> ~/.bashrc
    fi
    echo "✓ Environment variables added to ~/.bashrc"
else
    echo "⚠ Environment variables already in ~/.bashrc"
fi

# Export for current session
export OPENCV_DIR="$OPENCV_DIR"
if [ -n "$OPENCV_JAR" ]; then
    export OPENCV_JAR="$OPENCV_JAR"
fi
if [ -n "$OPENCV_SO" ]; then
    OPENCV_LIB_DIR=$(dirname "$OPENCV_SO")
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$OPENCV_LIB_DIR"
fi

echo ""
echo "========================================="
echo "Installation Summary"
echo "========================================="
echo "OpenCV Directory: $OPENCV_DIR"
echo "OpenCV JAR: $OPENCV_JAR"
echo "OpenCV Native Library: $OPENCV_SO"
echo ""
echo "⚠ IMPORTANT: Please restart your terminal or run:"
echo "   source ~/.bashrc"
echo ""
echo "Then you can build and run SecureView!"
echo "========================================="

