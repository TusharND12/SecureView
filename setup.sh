#!/bin/bash

# SecureView Setup Script
# This script helps install dependencies and build the project

echo "========================================="
echo "SecureView Setup Script"
echo "========================================="
echo ""

# Check if Java is installed
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✓ Java found: $JAVA_VERSION"
else
    echo "✗ Java not found"
    echo ""
    echo "To install Java 11, run:"
    echo "  sudo apt update"
    echo "  sudo apt install -y openjdk-11-jdk"
    echo ""
    exit 1
fi

# Check if Maven is installed
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✓ Maven found: $MVN_VERSION"
else
    echo "✗ Maven not found"
    echo ""
    echo "To install Maven, run:"
    echo "  sudo apt install -y maven"
    echo ""
    exit 1
fi

echo ""
echo "Building SecureView Desktop Application..."
echo ""

cd SecureView/desktop-app

# Build the project
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✓ Build successful!"
    echo "========================================="
    echo ""
    echo "JAR file location: target/secureview-desktop-1.0.0.jar"
    echo ""
    echo "To run the application:"
    echo "  cd SecureView/desktop-app"
    echo "  java -jar target/secureview-desktop-1.0.0.jar"
    echo ""
    echo "Note: You'll need to configure Firebase before running."
    echo "See README.md for setup instructions."
else
    echo ""
    echo "========================================="
    echo "✗ Build failed!"
    echo "========================================="
    echo ""
    exit 1
fi

