#!/bin/bash

# SecureView Runner Script with OpenCV Support
# This script sets up OpenCV and runs SecureView

echo "========================================="
echo "SecureView - Starting with OpenCV"
echo "========================================="
echo ""

# Find OpenCV JAR
OPENCV_JAR=""
if [ -f "/usr/share/java/opencv-460.jar" ]; then
    OPENCV_JAR="/usr/share/java/opencv-460.jar"
elif [ -f "/usr/share/java/opencv4/opencv-460.jar" ]; then
    OPENCV_JAR="/usr/share/java/opencv4/opencv-460.jar"
else
    OPENCV_JAR=$(find /usr/share/java -name "opencv-*.jar" 2>/dev/null | head -1)
fi

# Find OpenCV native library
OPENCV_LIB=""
if [ -f "/usr/lib/jni/libopencv_java460.so" ]; then
    OPENCV_LIB="/usr/lib/jni/libopencv_java460.so"
    export LD_LIBRARY_PATH="/usr/lib/jni:$LD_LIBRARY_PATH"
fi

# Set Java library path
export LD_LIBRARY_PATH="/usr/lib/jni:/usr/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH"

echo "OpenCV Configuration:"
if [ -n "$OPENCV_JAR" ]; then
    echo "  ✓ JAR: $OPENCV_JAR"
else
    echo "  ⚠ JAR: Not found (will try system classpath)"
fi

if [ -n "$OPENCV_LIB" ]; then
    echo "  ✓ Native Library: $OPENCV_LIB"
else
    echo "  ⚠ Native Library: Not found in /usr/lib/jni"
fi

echo ""
echo "Starting SecureView..."
echo ""

# Navigate to desktop app
cd "$(dirname "$0")/SecureView/desktop-app" || exit 1

# Find Java 21 if available (required for OpenCV JAR)
JAVA_CMD="java"
if [ -f "/usr/lib/jvm/java-21-openjdk-amd64/bin/java" ]; then
    JAVA_CMD="/usr/lib/jvm/java-21-openjdk-amd64/bin/java"
    echo "Using Java 21 for OpenCV compatibility"
elif [ -f "/usr/lib/jvm/java-17-openjdk-amd64/bin/java" ]; then
    JAVA_CMD="/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
    echo "Using Java 17 (may have compatibility issues with OpenCV)"
else
    echo "⚠ Warning: Using Java 11 - OpenCV JAR requires Java 21"
    echo "  Install Java 21 with: ./install-java21.sh"
fi

echo ""

# Run with OpenCV JAR in classpath if found
if [ -n "$OPENCV_JAR" ] && [ -f "$OPENCV_JAR" ]; then
    $JAVA_CMD -Djava.library.path="$LD_LIBRARY_PATH" \
         -cp "target/secureview-desktop-1.0.0.jar:$OPENCV_JAR" \
         com.secureview.desktop.SecureViewApplication
else
    $JAVA_CMD -Djava.library.path="$LD_LIBRARY_PATH" \
         -jar target/secureview-desktop-1.0.0.jar
fi

