#!/bin/bash

# Script to run SecureView with OpenCV support on Linux

echo "========================================="
echo "SecureView - Running with OpenCV Setup"
echo "========================================="
echo ""

# Check if OpenCV is installed
if ! dpkg -l | grep -q libopencv; then
    echo "⚠ OpenCV not found via package manager"
    echo ""
    echo "To install OpenCV, run:"
    echo "  sudo apt update"
    echo "  sudo apt install -y libopencv-dev libopencv-contrib-dev"
    echo ""
    echo "Or use the full build script:"
    echo "  ./install-opencv-linux.sh"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Find OpenCV installation
OPENCV_LIB_DIR=""
OPENCV_JAR=""

# Try to find OpenCV libraries
if [ -d "/usr/lib/x86_64-linux-gnu" ]; then
    OPENCV_SO=$(find /usr/lib/x86_64-linux-gnu -name "libopencv_java*.so" 2>/dev/null | head -1)
    if [ -n "$OPENCV_SO" ]; then
        OPENCV_LIB_DIR=$(dirname "$OPENCV_SO")
        echo "✓ Found OpenCV native library: $OPENCV_SO"
    fi
fi

# Try to find OpenCV JAR
OPENCV_JAR=$(find /usr -name "opencv-*.jar" 2>/dev/null | head -1)
if [ -n "$OPENCV_JAR" ]; then
    echo "✓ Found OpenCV JAR: $OPENCV_JAR"
fi

# Set up environment variables
if [ -n "$OPENCV_LIB_DIR" ]; then
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$OPENCV_LIB_DIR"
    echo "✓ Added to LD_LIBRARY_PATH: $OPENCV_LIB_DIR"
fi

# Try to set OPENCV_DIR to a reasonable location
if [ -z "$OPENCV_DIR" ]; then
    # Try common locations
    if [ -d "/usr" ]; then
        export OPENCV_DIR="/usr"
        echo "✓ Set OPENCV_DIR=/usr"
    fi
fi

# Add OpenCV JAR to classpath if found
CLASSPATH_EXTRA=""
if [ -n "$OPENCV_JAR" ]; then
    CLASSPATH_EXTRA=":$OPENCV_JAR"
fi

echo ""
echo "========================================="
echo "Starting SecureView..."
echo "========================================="
echo ""

# Navigate to desktop app directory
cd "$(dirname "$0")/SecureView/desktop-app" || exit 1

# Run the application
if [ -n "$CLASSPATH_EXTRA" ]; then
    java -cp "target/secureview-desktop-1.0.0.jar$CLASSPATH_EXTRA" \
         -Djava.library.path="$LD_LIBRARY_PATH" \
         com.secureview.desktop.SecureViewApplication
else
    java -Djava.library.path="$LD_LIBRARY_PATH" \
         -jar target/secureview-desktop-1.0.0.jar
fi

