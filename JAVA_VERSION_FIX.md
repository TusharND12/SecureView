# Java Version Issue - Quick Fix

## Problem
OpenCV JAR requires **Java 21**, but we're using **Java 11**.

## Solution

### Step 1: Install Java 21

Run this command:

```bash
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./install-java21.sh
```

Or manually:
```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java
```

### Step 2: Run SecureView

After Java 21 is installed:

```bash
./run-secureview.sh
```

## What I Updated

✅ **Updated `run-secureview.sh`** to automatically detect and use Java 21 if available  
✅ **Created `install-java21.sh`** to install Java 21 easily

## Quick Command

```bash
# Install Java 21 and run
cd /home/aryan-budukh/Desktop/SecureVIew/SecureView
./install-java21.sh && ./run-secureview.sh
```

## Alternative: Run Without OpenCV JAR

If you want to test the app without OpenCV (limited functionality):

```bash
cd SecureView/desktop-app
java -jar target/secureview-desktop-1.0.0.jar
```

But face recognition won't work without OpenCV.

