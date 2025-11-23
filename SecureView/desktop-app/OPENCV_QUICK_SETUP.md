# OpenCV 4.12.0 Quick Setup Guide

## Step 1: Find Your OpenCV Installation

After downloading and extracting OpenCV 4.12.0, note the installation directory.
Common locations:
- `C:\opencv`
- `C:\opencv4120`
- `C:\Program Files\opencv`
- Or any custom location you chose

## Step 2: Set Environment Variable

### Option A: Using Command Prompt (Temporary - Current Session Only)
```cmd
set OPENCV_DIR=C:\path\to\your\opencv
```

### Option B: Using PowerShell (Permanent)
```powershell
[System.Environment]::SetEnvironmentVariable("OPENCV_DIR", "C:\path\to\your\opencv", "User")
```

### Option C: Using Windows GUI
1. Press `Win + R`, type `sysdm.cpl`, press Enter
2. Go to "Advanced" tab
3. Click "Environment Variables"
4. Under "User variables", click "New"
5. Variable name: `OPENCV_DIR`
6. Variable value: `C:\path\to\your\opencv` (your actual OpenCV path)
7. Click OK on all dialogs

## Step 3: Verify Installation

Check that these files exist:
- `%OPENCV_DIR%\build\java\opencv-4120.jar`
- `%OPENCV_DIR%\build\java\x64\opencv_java4120.dll` (for 64-bit Java)
- `%OPENCV_DIR%\build\java\x86\opencv_java4120.dll` (for 32-bit Java)

## Step 4: Rebuild and Run

After setting the environment variable:

1. **Close and reopen your terminal/command prompt** (to load the new environment variable)

2. Rebuild the project:
   ```cmd
   cd desktop-app
   mvn clean package
   ```

3. Run the application:
   ```cmd
   run.bat
   ```
   
   Or manually:
   ```cmd
   java -cp "%OPENCV_DIR%\build\java\opencv-4120.jar;target\secureview-desktop-1.0.0.jar" com.secureview.desktop.SecureViewApplication
   ```

## Troubleshooting

### "OpenCV not found" error
- Verify OPENCV_DIR is set: `echo %OPENCV_DIR%`
- Check the JAR exists: `dir "%OPENCV_DIR%\build\java\opencv-4120.jar"`
- Make sure you restarted your terminal after setting the variable

### "Native library not found" error
- Check DLL exists: `dir "%OPENCV_DIR%\build\java\x64\opencv_java4120.dll"`
- Add DLL directory to PATH:
  ```cmd
  set PATH=%OPENCV_DIR%\build\java\x64;%PATH%
  ```

### Still having issues?
Run the setup script:
```cmd
setup-opencv.bat
```

This will guide you through the setup process.

