# PowerShell script to run SecureView with OpenCV support
Write-Host "Starting SecureView Application..." -ForegroundColor Green
Write-Host ""

# Try to find OpenCV installation
$opencvJar = $null
$opencvDllDir = $null
$opencvDir = $null

# Check OPENCV_DIR environment variable
if ($env:OPENCV_DIR) {
    $jarPath = Join-Path $env:OPENCV_DIR "build\java\opencv-4120.jar"
    if (Test-Path $jarPath) {
        $opencvJar = $jarPath
        $opencvDllDir = Join-Path $env:OPENCV_DIR "build\java\x64"
        $opencvDir = $env:OPENCV_DIR
        Write-Host "Found OpenCV at: $opencvDir" -ForegroundColor Cyan
    }
}

# If not found, try common paths
if (-not $opencvJar) {
    $possiblePaths = @(
        "C:\Users\TUSHAR\Downloads\opencv",
        "C:\opencv",
        "C:\opencv4120"
    )
    
    foreach ($path in $possiblePaths) {
        $jarPath = Join-Path $path "build\java\opencv-4120.jar"
        if (Test-Path $jarPath) {
            $opencvJar = $jarPath
            $opencvDllDir = Join-Path $path "build\java\x64"
            $opencvDir = $path
            Write-Host "Found OpenCV at: $opencvDir" -ForegroundColor Cyan
            break
        }
    }
}

# Add OpenCV DLL directory to PATH for this session
if ($opencvDllDir -and (Test-Path $opencvDllDir)) {
    $env:PATH = "$opencvDllDir;$env:PATH"
    Write-Host "Added OpenCV DLL directory to PATH" -ForegroundColor Yellow
}

# Set OPENCV_DIR environment variable
if ($opencvDir) {
    $env:OPENCV_DIR = $opencvDir
}

# Run the application
if ($opencvJar) {
    Write-Host ""
    Write-Host "Running with OpenCV support..." -ForegroundColor Green
    Write-Host "OpenCV JAR: $opencvJar" -ForegroundColor Gray
    Write-Host "OpenCV DLL Dir: $opencvDllDir" -ForegroundColor Gray
    Write-Host ""
    
    $classpath = "$opencvJar;target\secureview-desktop-1.0.0.jar"
    java -cp $classpath com.secureview.desktop.SecureViewApplication
} else {
    Write-Host ""
    Write-Host "WARNING: OpenCV JAR not found!" -ForegroundColor Yellow
    Write-Host "Running without OpenCV (limited functionality)..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To enable full functionality:" -ForegroundColor Yellow
    Write-Host "1. Set OPENCV_DIR environment variable to your OpenCV installation directory" -ForegroundColor Yellow
    Write-Host "2. Or place OpenCV in C:\opencv" -ForegroundColor Yellow
    Write-Host ""
    
    java -jar target\secureview-desktop-1.0.0.jar
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Application exited with an error." -ForegroundColor Red
    Read-Host "Press Enter to exit"
}

