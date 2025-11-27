#!/bin/bash

# Quick OpenCV Installation - Try package manager first
# This is faster but may need additional configuration

echo "========================================="
echo "Quick OpenCV Installation for SecureView"
echo "========================================="
echo ""

echo "This script will try to install OpenCV via package manager."
echo "If you need full Java bindings, use install-opencv-linux.sh instead."
echo ""

# Install OpenCV via apt
echo "Installing OpenCV via package manager..."
sudo apt update
sudo apt install -y libopencv-dev libopencv-contrib-dev

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ OpenCV installed via package manager"
    echo ""
    echo "Finding OpenCV installation..."
    
    # Try to find OpenCV
    OPENCV_PKG_CONFIG=$(pkg-config --variable=prefix opencv4 2>/dev/null || pkg-config --variable=prefix opencv 2>/dev/null)
    
    if [ -n "$OPENCV_PKG_CONFIG" ]; then
        echo "Found OpenCV at: $OPENCV_PKG_CONFIG"
        
        # Try to find Java bindings
        OPENCV_JAR=$(find /usr -name "opencv-*.jar" 2>/dev/null | head -1)
        OPENCV_SO=$(find /usr -name "libopencv_java*.so" 2>/dev/null | head -1)
        
        if [ -n "$OPENCV_JAR" ]; then
            echo "✓ Found OpenCV JAR: $OPENCV_JAR"
        else
            echo "⚠ OpenCV JAR not found - Java bindings may not be available"
            echo "  You may need to build OpenCV from source with Java support"
        fi
        
        if [ -n "$OPENCV_SO" ]; then
            echo "✓ Found OpenCV native library: $OPENCV_SO"
            OPENCV_LIB_DIR=$(dirname "$OPENCV_SO")
            export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$OPENCV_LIB_DIR"
        else
            echo "⚠ OpenCV native library not found"
        fi
        
        # Set environment variables
        if ! grep -q "OPENCV_DIR" ~/.bashrc 2>/dev/null; then
            echo "" >> ~/.bashrc
            echo "# OpenCV Configuration" >> ~/.bashrc
            echo "export OPENCV_DIR=\"$OPENCV_PKG_CONFIG\"" >> ~/.bashrc
            if [ -n "$OPENCV_LIB_DIR" ]; then
                echo "export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH:$OPENCV_LIB_DIR\"" >> ~/.bashrc
            fi
        fi
        
        export OPENCV_DIR="$OPENCV_PKG_CONFIG"
        
        echo ""
        echo "========================================="
        echo "Installation complete!"
        echo "========================================="
        echo ""
        echo "⚠ Note: If Java bindings are missing, you may need to:"
        echo "   1. Build OpenCV from source (use install-opencv-linux.sh)"
        echo "   2. Or download pre-built OpenCV with Java support"
        echo ""
        echo "Restart your terminal or run: source ~/.bashrc"
    else
        echo "⚠ Could not detect OpenCV installation path"
        echo "  OpenCV may still work, but you may need to set OPENCV_DIR manually"
    fi
else
    echo ""
    echo "✗ Installation failed"
    echo "  Try the full installation script: ./install-opencv-linux.sh"
    exit 1
fi

