# OpenCV Setup Instructions

## Important Note

The SecureView application requires **OpenCV 4.x** to be installed on your system. The Maven dependency only provides the Java bindings, but you need the native libraries installed separately.

## Installation Steps

### Windows

1. **Download OpenCV**
   - Go to https://opencv.org/releases/
   - Download OpenCV 4.8.0 or later for Windows
   - Extract to a directory (e.g., `C:\opencv`)

2. **Set Environment Variables**
   ```powershell
   # Set OPENCV_DIR
   [System.Environment]::SetEnvironmentVariable("OPENCV_DIR", "C:\opencv", "User")
   
   # Add to PATH
   $currentPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
   [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;C:\opencv\build\java\x64", "User")
   ```

3. **Copy Native Library**
   - Copy `opencv_java480.dll` from `C:\opencv\build\java\x64\` to your system PATH or project directory
   - Or add `C:\opencv\build\java\x64` to your system PATH

4. **Verify Installation**
   ```java
   System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
   ```

### Alternative: Use System Dependency

If you have OpenCV installed, you can modify `pom.xml` to use a system dependency:

```xml
<dependency>
    <groupId>org.opencv</groupId>
    <artifactId>opencv</artifactId>
    <version>4.8.0</version>
    <scope>system</scope>
    <systemPath>${opencv.dir}/build/java/opencv-480.jar</systemPath>
</dependency>
```

## Current Status

The project is configured to use OpenCV 2.4.9 from Maven (which is available), but the code uses OpenCV 4.x APIs. You have two options:

1. **Install OpenCV 4.x** (Recommended)
   - Follow the steps above
   - Update the code to use OpenCV 4.x package structure

2. **Use OpenCV 2.4.9** (Quick test)
   - Update code to use OpenCV 2.4.9 APIs
   - Some features may not be available

## For Development/Testing

For now, you can comment out OpenCV-dependent code and test other components, or install OpenCV 4.x following the instructions above.

