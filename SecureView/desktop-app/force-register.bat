@echo off
echo ========================================
echo SecureView - Force Registration Mode
echo ========================================
echo.
echo This will clear any existing registration
echo and open the registration window.
echo.

cd /d "%~dp0"

REM Set OpenCV paths
set "OPENCV_DIR=C:\Users\TUSHAR\Downloads\opencv"
set "OPENCV_JAR=%OPENCV_DIR%\build\java\opencv-4120.jar"
set "OPENCV_DLL_DIR=%OPENCV_DIR%\build\java\x64"

if exist "%OPENCV_JAR%" (
    set "PATH=%OPENCV_DLL_DIR%;%PATH%"
    set "OPENCV_DIR=%OPENCV_DIR%"
    echo Found OpenCV at: %OPENCV_DIR%
    echo.
    echo Starting in registration mode...
    echo.
    java -cp "%OPENCV_JAR%;target\secureview-desktop-1.0.0.jar" com.secureview.desktop.SecureViewApplication --register
) else (
    echo WARNING: OpenCV JAR not found!
    echo Running without OpenCV (limited functionality)...
    echo.
    java -jar target\secureview-desktop-1.0.0.jar --register
)

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)




