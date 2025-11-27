#!/bin/bash

# Script to download RetinaFace and ArcFace models for SecureView
# Models will be saved to ~/.secureview/models/

set -e

MODELS_DIR="$HOME/.secureview/models"
mkdir -p "$MODELS_DIR"

echo "=========================================="
echo "SecureView Model Downloader"
echo "=========================================="
echo ""
echo "Models will be downloaded to: $MODELS_DIR"
echo ""

# Check for required tools
if ! command -v wget &> /dev/null && ! command -v curl &> /dev/null; then
    echo "ERROR: Neither wget nor curl is installed."
    echo "Please install one: sudo apt install wget curl"
    exit 1
fi

DOWNLOAD_CMD=""
if command -v wget &> /dev/null; then
    DOWNLOAD_CMD="wget"
    WGET_FLAGS="-O"
elif command -v curl &> /dev/null; then
    DOWNLOAD_CMD="curl"
    CURL_FLAGS="-L -o"
fi

# Function to download with progress
download_file() {
    local url=$1
    local output=$2
    local name=$3
    
    echo "Downloading $name..."
    echo "URL: $url"
    echo "Output: $output"
    
    if [ "$DOWNLOAD_CMD" = "wget" ]; then
        wget --progress=bar:force --show-progress "$url" -O "$output" || {
            echo "ERROR: Failed to download $name"
            return 1
        }
    else
        curl -L --progress-bar "$url" -o "$output" || {
            echo "ERROR: Failed to download $name"
            return 1
        }
    fi
    
    if [ -f "$output" ] && [ -s "$output" ]; then
        echo "✓ $name downloaded successfully ($(du -h "$output" | cut -f1))"
        return 0
    else
        echo "ERROR: Downloaded file is empty or missing"
        return 1
    fi
}

# RetinaFace Model
RETINAFACE_URL="https://github.com/opencv/opencv_zoo/raw/master/models/face_detection_retinaface/face_detection_retinaface_2023mar.onnx"
RETINAFACE_OUTPUT="$MODELS_DIR/retinaface.onnx"

if [ -f "$RETINAFACE_OUTPUT" ] && [ -s "$RETINAFACE_OUTPUT" ]; then
    echo "✓ RetinaFace model already exists: $RETINAFACE_OUTPUT"
    read -p "Re-download? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Skipping RetinaFace download"
    else
        download_file "$RETINAFACE_URL" "$RETINAFACE_OUTPUT" "RetinaFace"
    fi
else
    download_file "$RETINAFACE_URL" "$RETINAFACE_OUTPUT" "RetinaFace"
fi

echo ""

# ArcFace Model - Try multiple sources
ARCFACE_OUTPUT="$MODELS_DIR/arcface.onnx"

if [ -f "$ARCFACE_OUTPUT" ] && [ -s "$ARCFACE_OUTPUT" ]; then
    echo "✓ ArcFace model already exists: $ARCFACE_OUTPUT"
    read -p "Re-download? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Skipping ArcFace download"
        ARCFACE_SKIP=true
    fi
fi

if [ "$ARCFACE_SKIP" != "true" ]; then
    echo "Downloading ArcFace model..."
    echo "Note: ArcFace model may be large (~100MB). This may take a few minutes."
    echo ""
    
    # Try multiple ArcFace sources
    ARCFACE_SOURCES=(
        "https://github.com/opencv/opencv_zoo/raw/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx|sface"
        "https://github.com/onnx/models/raw/main/vision/body_analysis/arcface/model/arcface_r100_v1.onnx|arcface_r100"
    )
    
    ARCFACE_DOWNLOADED=false
    
    for source_info in "${ARCFACE_SOURCES[@]}"; do
        IFS='|' read -r url name <<< "$source_info"
        echo "Trying source: $name"
        
        if download_file "$url" "$ARCFACE_OUTPUT" "ArcFace ($name)"; then
            ARCFACE_DOWNLOADED=true
            break
        else
            echo "Failed to download from $name, trying next source..."
            echo ""
        fi
    done
    
    if [ "$ARCFACE_DOWNLOADED" = "false" ]; then
        echo ""
        echo "⚠ WARNING: Could not download ArcFace from automatic sources."
        echo ""
        echo "MANUAL DOWNLOAD INSTRUCTIONS:"
        echo "1. Visit: https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface"
        echo "2. Download: face_recognition_sface_2021dec.onnx"
        echo "3. Save as: $ARCFACE_OUTPUT"
        echo ""
        echo "OR use SFace (works similarly to ArcFace):"
        echo "1. Visit: https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface"
        echo "2. Download the ONNX model"
        echo "3. Save as: $ARCFACE_OUTPUT"
        echo ""
    fi
fi

echo ""
echo "=========================================="
echo "Download Summary"
echo "=========================================="
echo ""

# Check what we have
if [ -f "$RETINAFACE_OUTPUT" ] && [ -s "$RETINAFACE_OUTPUT" ]; then
    echo "✓ RetinaFace: $RETINAFACE_OUTPUT ($(du -h "$RETINAFACE_OUTPUT" | cut -f1))"
else
    echo "✗ RetinaFace: MISSING"
fi

if [ -f "$ARCFACE_OUTPUT" ] && [ -s "$ARCFACE_OUTPUT" ]; then
    echo "✓ ArcFace: $ARCFACE_OUTPUT ($(du -h "$ARCFACE_OUTPUT" | cut -f1))"
else
    echo "✗ ArcFace: MISSING (will use SFace or FaceNet if available)"
fi

echo ""
echo "=========================================="
echo "Next Steps"
echo "=========================================="
echo ""
echo "1. Models are ready in: $MODELS_DIR"
echo "2. Run SecureView: ./run-secureview.sh"
echo "3. The app will automatically use RetinaFace + ArcFace if models are found"
echo ""
echo "Note: If ArcFace is missing, the app will try to use:"
echo "  - SFace (face_recognition_sface_2021dec.onnx)"
echo "  - FaceNet (facenet.onnx)"
echo "  - Or fall back to basic embedding extraction"
echo ""

