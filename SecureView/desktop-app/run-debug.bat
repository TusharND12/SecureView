@echo off
echo Starting SecureView Application (Debug Mode)...
echo.

REM Set OpenCV paths
set "OPENCV_DIR=C:\Users\TUSHAR\Downloads\opencv"
set "OPENCV_JAR=%OPENCV_DIR%\build\java\opencv-4120.jar"
set "OPENCV_DLL_DIR=%OPENCV_DIR%\build\java\x64"

REM Add DLL directory to PATH
set "PATH=%OPENCV_DLL_DIR%;%PATH%"

REM Set OPENCV_DIR for Java
set "OPENCV_DIR=%OPENCV_DIR%"

echo OpenCV Directory: %OPENCV_DIR%
echo OpenCV JAR: %OPENCV_JAR%
echo OpenCV DLL Dir: %OPENCV_DLL_DIR%
echo Model Location: %USERPROFILE%\.secureview\models\sface.onnx
echo.

if exist "%OPENCV_JAR%" (
    echo Found OpenCV JAR
    set "CLASSPATH=%OPENCV_JAR%;target\secureview-desktop-1.0.0.jar"
    echo.
    echo Running application...
    echo.
    java -cp "%CLASSPATH%" com.secureview.desktop.SecureViewApplication
) else (
    echo ERROR: OpenCV JAR not found at: %OPENCV_JAR%
    pause
    exit /b 1
)

if errorlevel 1 (
    echo.
    echo Application exited with error code: %ERRORLEVEL%
    pause
)

