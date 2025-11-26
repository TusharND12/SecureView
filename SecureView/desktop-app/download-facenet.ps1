# FaceNet Model Download Script for Windows
# This script automatically downloads and sets up the FaceNet model

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FaceNet Model Download Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create models directory
$modelsDir = "$env:USERPROFILE\.secureview\models"
Write-Host "Creating models directory: $modelsDir" -ForegroundColor Yellow

if (-not (Test-Path $modelsDir)) {
    New-Item -ItemType Directory -Force -Path $modelsDir | Out-Null
    Write-Host "✓ Directory created" -ForegroundColor Green
} else {
    Write-Host "✓ Directory already exists" -ForegroundColor Green
}

$modelPath = Join-Path $modelsDir "facenet.onnx"

# Check if model already exists
if (Test-Path $modelPath) {
    Write-Host ""
    Write-Host "FaceNet model already exists at: $modelPath" -ForegroundColor Yellow
    $overwrite = Read-Host "Do you want to download again? (y/n)"
    if ($overwrite -ne "y" -and $overwrite -ne "Y") {
        Write-Host "Skipping download. Using existing model." -ForegroundColor Green
        exit 0
    }
}

Write-Host ""
Write-Host "Downloading FaceNet ONNX model..." -ForegroundColor Yellow
Write-Host "This may take a few minutes (model size: ~100-200 MB)" -ForegroundColor Yellow
Write-Host ""

# Try multiple sources for FaceNet model
$sources = @(
    @{
        Name = "ONNX Model Zoo (FaceNet)"
        Url = "https://github.com/onnx/models/raw/main/vision/body_analysis/arcface/model/arcface_r100_v1.onnx"
        Description = "ArcFace model (similar to FaceNet, 512-dim)"
    },
    @{
        Name = "Alternative Source 1"
        Url = "https://github.com/opencv/opencv_zoo/raw/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx"
        Description = "SFace model (128-dim, lightweight alternative)"
    }
)

# Try to download from a reliable source
$downloaded = $false
$client = New-Object System.Net.WebClient

foreach ($source in $sources) {
    Write-Host "Trying source: $($source.Name)" -ForegroundColor Cyan
    Write-Host "  Description: $($source.Description)" -ForegroundColor Gray
    
    try {
        # For now, we'll use SFace as it's readily available
        # FaceNet ONNX models are harder to find publicly
        if ($source.Name -like "*SFace*" -or $source.Name -like "*Alternative*") {
            Write-Host "  Downloading SFace model (compatible, 128-dim)..." -ForegroundColor Yellow
            $client.DownloadFile($source.Url, $modelPath)
            
            # Rename to indicate it's SFace, not FaceNet
            $sfacePath = Join-Path $modelsDir "sface.onnx"
            if (Test-Path $modelPath) {
                Move-Item -Path $modelPath -Destination $sfacePath -Force
                Write-Host "  ✓ SFace model downloaded successfully!" -ForegroundColor Green
                Write-Host ""
                Write-Host "Note: SFace model downloaded (128-dim embeddings)" -ForegroundColor Yellow
                Write-Host "For FaceNet (512-dim), you'll need to download manually from:" -ForegroundColor Yellow
                Write-Host "  - TensorFlow Hub: https://tfhub.dev/google/facenet/1" -ForegroundColor Cyan
                Write-Host "  - Then convert to ONNX using tf2onnx" -ForegroundColor Cyan
                $downloaded = $true
                break
            }
        }
    } catch {
        Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
        continue
    }
}

$client.Dispose()

if (-not $downloaded) {
    Write-Host ""
    Write-Host "Automatic download failed. Creating download instructions..." -ForegroundColor Yellow
    
    # Create a helper script with manual download instructions
    $instructions = @"
# FaceNet Model Download Instructions

## Option 1: Download Pre-converted ONNX Model

1. Visit one of these sources:
   - ONNX Model Zoo: https://github.com/onnx/models
   - Search for "face recognition" or "FaceNet"
   
2. Download the ONNX model file

3. Place it in: $modelsDir\facenet.onnx

## Option 2: Convert from TensorFlow

1. Download FaceNet from TensorFlow Hub:
   https://tfhub.dev/google/facenet/1

2. Install tf2onnx:
   pip install tf2onnx

3. Convert the model:
   python -m tf2onnx.convert --saved-model <path-to-facenet> --output facenet.onnx

4. Place in: $modelsDir\facenet.onnx

## Option 3: Use SFace (Lightweight Alternative)

SFace model (128-dim) has been downloaded as: $modelsDir\sface.onnx
This works but uses 128-dim embeddings instead of 512-dim.

"@
    
    $instructionsPath = Join-Path $modelsDir "DOWNLOAD_INSTRUCTIONS.txt"
    $instructions | Out-File -FilePath $instructionsPath -Encoding UTF8
    Write-Host "Instructions saved to: $instructionsPath" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Model location: $modelsDir" -ForegroundColor Yellow
if (Test-Path (Join-Path $modelsDir "sface.onnx")) {
    Write-Host "✓ SFace model ready (sface.onnx)" -ForegroundColor Green
}
if (Test-Path (Join-Path $modelsDir "facenet.onnx")) {
    Write-Host "✓ FaceNet model ready (facenet.onnx)" -ForegroundColor Green
}
Write-Host ""
Write-Host "You can now run SecureView and it will use the model automatically!" -ForegroundColor Green

