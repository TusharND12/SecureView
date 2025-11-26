# Simple PowerShell script to run SecureView
$ErrorActionPreference = "Continue"

Write-Host "Starting SecureView Application..." -ForegroundColor Green
Write-Host ""

# Set OpenCV paths
$OPENCV_DIR = "C:\Users\TUSHAR\Downloads\opencv"
$OPENCV_JAR = "$OPENCV_DIR\build\java\opencv-4120.jar"
$OPENCV_DLL_DIR = "$OPENCV_DIR\build\java\x64"

# Add DLL directory to PATH
$env:PATH = "$OPENCV_DLL_DIR;$env:PATH"

Write-Host "OpenCV Directory: $OPENCV_DIR"
Write-Host "OpenCV JAR: $OPENCV_JAR"
Write-Host "OpenCV DLL Dir: $OPENCV_DLL_DIR"
Write-Host ""

# Check if JAR exists
if (Test-Path "target\secureview-desktop-1.0.0.jar") {
    Write-Host "Found application JAR" -ForegroundColor Green
} else {
    Write-Host "ERROR: Application JAR not found!" -ForegroundColor Red
    Write-Host "Please run: mvn package -DskipTests" -ForegroundColor Yellow
    exit 1
}

if (Test-Path $OPENCV_JAR) {
    Write-Host "Found OpenCV JAR" -ForegroundColor Green
} else {
    Write-Host "WARNING: OpenCV JAR not found at: $OPENCV_JAR" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Running application..." -ForegroundColor Cyan
Write-Host ""

# Run the application
$CLASSPATH = "$OPENCV_JAR;target\secureview-desktop-1.0.0.jar"
java -cp $CLASSPATH com.secureview.desktop.SecureViewApplication

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Application exited with error code: $LASTEXITCODE" -ForegroundColor Red
    Write-Host "Press any key to continue..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

