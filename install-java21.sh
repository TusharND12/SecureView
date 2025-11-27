#!/bin/bash

# Install Java 21 for OpenCV compatibility

echo "========================================="
echo "Installing Java 21 for OpenCV Support"
echo "========================================="
echo ""

echo "Installing OpenJDK 21..."
sudo apt update
sudo apt install -y openjdk-21-jdk

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Java 21 installed successfully!"
    echo ""
    
    # Set Java 21 as default (optional)
    echo "Setting Java 21 as default..."
    sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-21-openjdk-amd64/bin/java 1
    sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java
    
    echo ""
    echo "Verifying installation..."
    java -version
    
    echo ""
    echo "========================================="
    echo "✓ Java 21 Setup Complete!"
    echo "========================================="
    echo ""
    echo "Now you can run SecureView with:"
    echo "  ./run-secureview.sh"
    echo ""
else
    echo ""
    echo "✗ Installation failed"
    exit 1
fi

