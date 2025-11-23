@echo off
echo Setting up OpenCV 4.12.0 for SecureView...
echo.

REM Check if OPENCV_DIR is already set
if defined OPENCV_DIR (
    echo OPENCV_DIR is currently set to: %OPENCV_DIR%
    echo.
    set /p confirm="Do you want to change it? (y/n): "
    if /i "%confirm%"=="n" goto :build
)

REM Prompt for OpenCV installation directory
echo Please enter the path to your OpenCV installation directory.
echo Example: C:\opencv
echo.
set /p opencv_path="OpenCV installation path: "

REM Validate path
if not exist "%opencv_path%\build\java\opencv-4120.jar" (
    echo.
    echo ERROR: OpenCV JAR not found at: %opencv_path%\build\java\opencv-4120.jar
    echo Please verify your OpenCV installation path.
    pause
    exit /b 1
)

REM Set environment variable for current session
set OPENCV_DIR=%opencv_path%
echo.
echo OPENCV_DIR set to: %OPENCV_DIR%

REM Set environment variable permanently
setx OPENCV_DIR "%opencv_path%" >nul 2>&1
if errorlevel 1 (
    echo Warning: Could not set OPENCV_DIR permanently. You may need to set it manually.
    echo You can set it by running: setx OPENCV_DIR "%opencv_path%"
) else (
    echo OPENCV_DIR has been set permanently.
)

echo.
echo OpenCV setup complete!
echo.
echo Next steps:
echo 1. Close and reopen your terminal/command prompt
echo 2. Run: mvn clean package
echo 3. Run: java -jar target\secureview-desktop-1.0.0.jar
echo.
pause

:build
echo.
echo Building project...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo Build failed. Please check the errors above.
    pause
    exit /b 1
) else (
    echo.
    echo Build successful!
    echo.
    echo You can now run the application with:
    echo java -jar target\secureview-desktop-1.0.0.jar
    echo.
    pause
)

