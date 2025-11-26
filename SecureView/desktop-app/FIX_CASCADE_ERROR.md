# Fix: Cascade Classifier Loading Error

## Problem
The application shows an error:
```
Failed to load face cascade classifier from: 
C:\Users\TUSHAR\Downloads\opencv\build\etc\haarcascades\haarcascade_frontalface_alt.xml
```

## Root Cause
The cascade file exists, but OpenCV native library isn't loaded properly, so `CascadeClassifier.load()` fails.

## Solution

### Option 1: Set OPENCV_DIR Environment Variable (Recommended)

1. **Set Environment Variable**:
   ```powershell
   [System.Environment]::SetEnvironmentVariable("OPENCV_DIR", "C:\Users\TUSHAR\Downloads\opencv", "User")
   ```

2. **Add OpenCV DLL to PATH**:
   ```powershell
   $currentPath = [System.Environment]::SetEnvironmentVariable("Path", "User")
   $opencvDllPath = "C:\Users\TUSHAR\Downloads\opencv\build\java\x64"
   [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$opencvDllPath", "User")
   ```

3. **Restart your terminal/application** for changes to take effect

### Option 2: Verify OpenCV Installation

Check if OpenCV DLL exists:
```powershell
Test-Path "C:\Users\TUSHAR\Downloads\opencv\build\java\x64\opencv_java*.dll"
```

If not found, you may need to:
1. Download OpenCV from https://opencv.org/releases/
2. Extract to `C:\Users\TUSHAR\Downloads\opencv`
3. Ensure `build\java\x64\opencv_java*.dll` exists

### Option 3: Use run.bat Script

The `run.bat` script should automatically find OpenCV. Make sure:
- OpenCV is installed at `C:\Users\TUSHAR\Downloads\opencv`
- Or set `OPENCV_DIR` environment variable

## What I Fixed

1. ✅ Improved path handling in `FaceDetector.java`
2. ✅ Better error messages showing what went wrong
3. ✅ Multiple path format attempts (forward/backward slashes)
4. ✅ Better cascade file detection

## Next Steps

1. Set `OPENCV_DIR` environment variable (see Option 1)
2. Restart your terminal
3. Run the application again:
   ```batch
   cd "T:\COLLEGE LIFE\projects\SecureView\SecureView\desktop-app"
   java -jar target\secureview-desktop-1.0.0.jar
   ```

## Verification

After setting up, the application should:
- Load OpenCV native library successfully
- Find and load the cascade file
- Initialize face detection
- Load the SFace model (36.9 MB) for face recognition

## If Still Not Working

Check the logs at: `C:\Users\TUSHAR\.secureview\logs\`

Look for:
- "OpenCV native library loaded from: ..." ✅
- "Found cascade file at: ..." ✅
- "Face Detector initialized successfully" ✅

If you see errors about OpenCV not being found, you need to install OpenCV properly.

