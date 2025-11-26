# Quick Model Download Script
# Downloads SFace model for SecureView

$modelsDir = "$env:USERPROFILE\.secureview\models"
$modelPath = "$modelsDir\sface.onnx"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SecureView Model Download" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create directory
if (-not (Test-Path $modelsDir)) {
    New-Item -ItemType Directory -Force -Path $modelsDir | Out-Null
    Write-Host "[OK] Created directory: $modelsDir" -ForegroundColor Green
} else {
    Write-Host "[OK] Directory exists: $modelsDir" -ForegroundColor Green
}

# Check if model already exists
if (Test-Path $modelPath) {
    $size = (Get-Item $modelPath).Length / 1MB
    Write-Host ""
    Write-Host "Model already exists: $modelPath" -ForegroundColor Yellow
    Write-Host "Size: $([math]::Round($size, 2)) MB" -ForegroundColor Yellow
    $overwrite = Read-Host "Download again? (y/n)"
    if ($overwrite -ne "y" -and $overwrite -ne "Y") {
        Write-Host "Skipping download." -ForegroundColor Green
        exit 0
    }
}

Write-Host ""
Write-Host "Downloading SFace model..." -ForegroundColor Yellow
Write-Host "This may take a minute..." -ForegroundColor Gray
Write-Host ""

$url = "https://github.com/opencv/opencv_zoo/raw/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx"

try {
    # Download with progress
    $ProgressPreference = 'Continue'
    Invoke-WebRequest -Uri $url -OutFile $modelPath -UseBasicParsing
    
    if (Test-Path $modelPath) {
        $size = (Get-Item $modelPath).Length / 1MB
        if ($size -gt 1) {
            Write-Host ""
            Write-Host "[OK] Download successful!" -ForegroundColor Green
            Write-Host "     Location: $modelPath" -ForegroundColor Gray
            Write-Host "     Size: $([math]::Round($size, 2)) MB" -ForegroundColor Gray
            Write-Host ""
            Write-Host "========================================" -ForegroundColor Cyan
            Write-Host "Setup Complete!" -ForegroundColor Green
            Write-Host "========================================" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "You can now run SecureView!" -ForegroundColor Green
            Write-Host "The model will be automatically loaded." -ForegroundColor Green
        } else {
            Write-Host "[ERROR] Downloaded file is too small. May be an HTML page." -ForegroundColor Red
            Remove-Item $modelPath -ErrorAction SilentlyContinue
        }
    }
} catch {
    Write-Host ""
    Write-Host "[ERROR] Download failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please download manually:" -ForegroundColor Yellow
    Write-Host "1. Visit: https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface" -ForegroundColor Cyan
    Write-Host "2. Download: face_recognition_sface_2021dec.onnx" -ForegroundColor Cyan
    Write-Host "3. Save to: $modelPath" -ForegroundColor Cyan
    exit 1
}

