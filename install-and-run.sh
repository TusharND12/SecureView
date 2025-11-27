#!/bin/bash

# SecureView - Complete Installation and Run Script
# Run this script to install dependencies and run the project

set -e  # Exit on error

echo "========================================="
echo "SecureView - Installation & Run Script"
echo "========================================="
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Step 1: Check/Install Java
echo "Step 1: Checking Java installation..."
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✓ Java found: $JAVA_VERSION"
    
    # Check if it's Java 11+
    JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VER" -lt 11 ]; then
        echo "⚠ Warning: Java version is less than 11. Installing Java 11..."
        sudo apt update
        sudo apt install -y openjdk-11-jdk
    fi
else
    echo "✗ Java not found. Installing Java 11..."
    sudo apt update
    sudo apt install -y openjdk-11-jdk
    
    # Verify installation
    if command_exists java; then
        echo "✓ Java installed successfully"
        java -version
    else
        echo "✗ Java installation failed. Please install manually:"
        echo "  sudo apt install -y openjdk-11-jdk"
        exit 1
    fi
fi

echo ""

# Step 2: Check/Install Maven
echo "Step 2: Checking Maven installation..."
if command_exists mvn; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✓ Maven found: $MVN_VERSION"
else
    echo "✗ Maven not found. Installing Maven..."
    sudo apt install -y maven
    
    # Verify installation
    if command_exists mvn; then
        echo "✓ Maven installed successfully"
        mvn -version
    else
        echo "✗ Maven installation failed. Please install manually:"
        echo "  sudo apt install -y maven"
        exit 1
    fi
fi

echo ""

# Step 3: Navigate to project directory
echo "Step 3: Navigating to project directory..."
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/SecureView/desktop-app" || {
    echo "✗ Error: Could not find desktop-app directory"
    exit 1
}
echo "✓ Current directory: $(pwd)"
echo ""

# Step 4: Build the project
echo "Step 4: Building SecureView Desktop Application..."
echo "This may take a few minutes as Maven downloads dependencies..."
echo ""

mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✓ Build successful!"
    echo "========================================="
    echo ""
    
    # Check if JAR file exists
    if [ -f "target/secureview-desktop-1.0.0.jar" ]; then
        echo "✓ JAR file created: target/secureview-desktop-1.0.0.jar"
        echo ""
        echo "Step 5: Running SecureView..."
        echo "========================================="
        echo ""
        
        # Run the application
        java -jar target/secureview-desktop-1.0.0.jar
        
    else
        echo "✗ JAR file not found. Build may have failed."
        exit 1
    fi
else
    echo ""
    echo "========================================="
    echo "✗ Build failed!"
    echo "========================================="
    echo ""
    echo "Please check the error messages above."
    exit 1
fi

