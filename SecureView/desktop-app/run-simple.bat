@echo off
echo Starting SecureView Application...
echo.

REM Set OpenCV paths
set "OPENCV_DIR=C:\Users\TUSHAR\Downloads\opencv"
set "OPENCV_JAR=%OPENCV_DIR%\build\java\opencv-4120.jar"
set "OPENCV_DLL_DIR=%OPENCV_DIR%\build\java\x64"

REM Add DLL directory to PATH
set "PATH=%OPENCV_DLL_DIR%;%PATH%"

REM Check if OpenCV JAR exists
if exist "%OPENCV_JAR%" (
    echo Found OpenCV at: %OPENCV_DIR%
    echo.
    echo Running with OpenCV support...
    set "CLASSPATH=%OPENCV_JAR%;target\secureview-desktop-1.0.0.jar"
    java -cp "%CLASSPATH%" com.secureview.desktop.SecureViewApplication
) else (
    echo WARNING: OpenCV JAR not found at: %OPENCV_JAR%
    echo Running without OpenCV...
    java -jar target\secureview-desktop-1.0.0.jar
)

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)
