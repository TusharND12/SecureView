@echo off
setlocal enabledelayedexpansion
echo Starting SecureView Application...
echo.

REM Try to find OpenCV installation
set "OPENCV_JAR="
set "OPENCV_DLL_DIR="
set "OPENCV_DIR="

REM Check OPENCV_DIR environment variable
if defined OPENCV_DIR (
    if exist "!OPENCV_DIR!\build\java\opencv-4120.jar" (
        set "OPENCV_JAR=!OPENCV_DIR!\build\java\opencv-4120.jar"
        set "OPENCV_DLL_DIR=!OPENCV_DIR!\build\java\x64"
        echo Found OpenCV at: !OPENCV_DIR!
    )
)

REM If not found, try common paths
if not defined OPENCV_JAR (
    REM Check user's OpenCV installation first
    if exist "C:\Users\TUSHAR\Downloads\opencv\build\java\opencv-4120.jar" (
        set "OPENCV_JAR=C:\Users\TUSHAR\Downloads\opencv\build\java\opencv-4120.jar"
        set "OPENCV_DLL_DIR=C:\Users\TUSHAR\Downloads\opencv\build\java\x64"
        set "OPENCV_DIR=C:\Users\TUSHAR\Downloads\opencv"
        echo Found OpenCV at: C:\Users\TUSHAR\Downloads\opencv
    ) else if exist "C:\opencv\build\java\opencv-4120.jar" (
        set "OPENCV_JAR=C:\opencv\build\java\opencv-4120.jar"
        set "OPENCV_DLL_DIR=C:\opencv\build\java\x64"
        set "OPENCV_DIR=C:\opencv"
        echo Found OpenCV at: C:\opencv
    ) else if exist "C:\opencv4120\build\java\opencv-4120.jar" (
        set "OPENCV_JAR=C:\opencv4120\build\java\opencv-4120.jar"
        set "OPENCV_DLL_DIR=C:\opencv4120\build\java\x64"
        set "OPENCV_DIR=C:\opencv4120"
        echo Found OpenCV at: C:\opencv4120
    )
)

REM Add OpenCV DLL directory to PATH for this session
if defined OPENCV_DLL_DIR (
    set "PATH=!OPENCV_DLL_DIR!;!PATH!"
    echo Added OpenCV DLL directory to PATH
)

REM Set OPENCV_DIR environment variable
if defined OPENCV_DIR (
    set "OPENCV_DIR=!OPENCV_DIR!"
)

REM Run the application
if defined OPENCV_JAR (
    echo.
    echo Running with OpenCV support...
    echo OpenCV JAR: !OPENCV_JAR!
    echo OpenCV DLL Dir: !OPENCV_DLL_DIR!
    echo.
    set "CLASSPATH=!OPENCV_JAR!;target\secureview-desktop-1.0.0.jar"
    java -cp "!CLASSPATH!" com.secureview.desktop.SecureViewApplication
) else (
    echo.
    echo WARNING: OpenCV JAR not found!
    echo Running without OpenCV (limited functionality)...
    echo.
    echo To enable full functionality:
    echo 1. Set OPENCV_DIR environment variable to your OpenCV installation directory
    echo 2. Or place OpenCV in C:\opencv
    echo.
    java -jar target\secureview-desktop-1.0.0.jar
)

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)

endlocal

