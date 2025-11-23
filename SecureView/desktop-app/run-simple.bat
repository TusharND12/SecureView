@echo off
echo Starting SecureView Application...
echo.

REM Set OpenCV paths directly
set "OPENCV_DIR=C:\Users\TUSHAR\Downloads\opencv"
set "OPENCV_JAR=%OPENCV_DIR%\build\java\opencv-4120.jar"
set "OPENCV_DLL_DIR=%OPENCV_DIR%\build\java\x64"

REM Check if OpenCV exists
if not exist "%OPENCV_JAR%" (
    echo ERROR: OpenCV JAR not found at: %OPENCV_JAR%
    echo Please verify OpenCV installation.
    pause
    exit /b 1
)

echo Found OpenCV at: %OPENCV_DIR%
echo.

REM Add OpenCV DLL to PATH using a simpler method
set "ORIGINAL_PATH=%PATH%"
set "PATH=%OPENCV_DLL_DIR%;%ORIGINAL_PATH%"
echo Added OpenCV DLL directory to PATH
echo.

REM Run the application
echo Running with OpenCV support...
echo OpenCV JAR: %OPENCV_JAR%
echo OpenCV DLL Dir: %OPENCV_DLL_DIR%
echo.

java -cp "%OPENCV_JAR%;target\secureview-desktop-1.0.0.jar" com.secureview.desktop.SecureViewApplication

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)

