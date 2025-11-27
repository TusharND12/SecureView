#!/bin/bash

# Download working RetinaFace and ArcFace models
# Uses reliable sources with proper error handling

set -e

MODELS_DIR="$HOME/.secureview/models"
echo "Creating models directory: $MODELS_DIR"
mkdir -p "$MODELS_DIR"
if [ ! -d "$MODELS_DIR" ]; then
    echo "ERROR: Failed to create models directory!"
    exit 1
fi
echo "Models directory created successfully: $MODELS_DIR"
cd "$MODELS_DIR" || exit 1
echo "Current directory: $(pwd)"

echo "=========================================="
echo "Downloading Face Recognition Models"
echo "=========================================="
echo ""

# Function to download with retry
download_with_retry() {
    local url=$1
    local output=$2
    local name=$3
    local max_retries=3
    
    for i in $(seq 1 $max_retries); do
        echo "Attempt $i/$max_retries: Downloading $name..."
        
        if wget --progress=bar:force --show-progress "$url" -O "$output" 2>&1; then
            if [ -f "$output" ] && [ -s "$output" ]; then
                size=$(du -h "$output" | cut -f1)
                echo "✓ $name downloaded successfully ($size)"
                return 0
            else
                echo "✗ Downloaded file is empty, retrying..."
                rm -f "$output"
            fi
        else
            echo "✗ Download failed, retrying..."
        fi
        sleep 2
    done
    
    return 1
}

# RetinaFace Model - Use OpenCV Zoo (most reliable)
echo "Downloading RetinaFace (Face Detection)..."
RETINAFACE_URL="https://github.com/opencv/opencv_zoo/raw/master/models/face_detection_retinaface/face_detection_retinaface_2023mar.onnx"
if download_with_retry "$RETINAFACE_URL" "retinaface.onnx" "RetinaFace"; then
    echo "✓ RetinaFace ready"
else
    echo "✗ RetinaFace download failed - will use Haar Cascade fallback"
fi

echo ""

# ArcFace/SFace Model - Use OpenCV Zoo SFace (most reliable)
echo "Downloading SFace (Face Recognition - ArcFace alternative)..."
SFACE_URL="https://github.com/opencv/opencv_zoo/raw/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx"
if download_with_retry "$SFACE_URL" "arcface.onnx" "SFace/ArcFace"; then
    echo "✓ SFace/ArcFace ready"
else
    echo "✗ SFace download failed - will use simplified embeddings"
fi

echo ""
echo "=========================================="
echo "Verification"
echo "=========================================="
ls -lh "$MODELS_DIR"/*.onnx 2>/dev/null || echo "No ONNX models found"

echo ""
echo "=========================================="
echo "Done!"
echo "=========================================="

