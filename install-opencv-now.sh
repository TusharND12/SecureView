#!/bin/bash

# Quick OpenCV Installation Script
# Run this to install OpenCV and then run SecureView

echo "========================================="
echo "Installing OpenCV for SecureView"
echo "========================================="
echo ""

echo "Step 1: Installing OpenCV libraries..."
sudo apt update
sudo apt install -y libopencv-dev libopencv-contrib-dev

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ OpenCV installed successfully!"
    echo ""
    
    # Update library cache
    echo "Step 2: Updating library cache..."
    sudo ldconfig
    
    echo ""
    echo "Step 3: Verifying installation..."
    OPENCV_LIBS=$(ldconfig -p | grep -i opencv | wc -l)
    if [ "$OPENCV_LIBS" -gt 0 ]; then
        echo "✓ Found $OPENCV_LIBS OpenCV libraries"
        ldconfig -p | grep -i opencv | head -3
    else
        echo "⚠ Warning: OpenCV libraries not found in cache"
    fi
    
    echo ""
    echo "========================================="
    echo "OpenCV Installation Complete!"
    echo "========================================="
    echo ""
    echo "Now you can run SecureView:"
    echo "  cd SecureView/desktop-app"
    echo "  java -jar target/secureview-desktop-1.0.0.jar"
    echo ""
else
    echo ""
    echo "✗ Installation failed. Please check the error messages above."
    exit 1
fi

