#!/bin/bash

# Download RetinaFace and ArcFace Models
# Tries multiple sources

echo "========================================="
echo "Downloading Face Recognition Models"
echo "========================================="
echo ""

mkdir -p ~/.secureview/models
cd ~/.secureview/models

# RetinaFace Model
echo "Downloading RetinaFace model..."
echo "Trying source 1: OpenCV Zoo..."

# Try OpenCV Zoo (alternative URL format)
wget "https://raw.githubusercontent.com/opencv/opencv_zoo/master/models/face_detection_retinaface/face_detection_retinaface_2023mar.onnx" -O retinaface.onnx 2>&1 | grep -E "(saved|ERROR|404)" || echo "Download attempt completed"

if [ ! -f retinaface.onnx ] || [ ! -s retinaface.onnx ]; then
    echo "Trying source 2: Direct InsightFace..."
    wget "https://github.com/deepinsight/insightface/releases/download/v0.7/retinaface_r50_v1.onnx" -O retinaface.onnx 2>&1 | grep -E "(saved|ERROR|404)" || echo "Download attempt completed"
fi

# ArcFace Model
echo ""
echo "Downloading ArcFace model..."
wget "https://github.com/deepinsight/insightface/releases/download/v0.7/arcface_r50_v1.onnx" -O arcface.onnx 2>&1 | grep -E "(saved|ERROR|404)" || echo "Download attempt completed"

echo ""
echo "Verifying downloads..."
ls -lh ~/.secureview/models/

if [ -f retinaface.onnx ] && [ -s retinaface.onnx ]; then
    echo "✓ RetinaFace model downloaded"
else
    echo "✗ RetinaFace model download failed"
    echo "  Manual download needed from:"
    echo "  https://github.com/opencv/opencv_zoo/tree/master/models/face_detection_retinaface"
fi

if [ -f arcface.onnx ] && [ -s arcface.onnx ]; then
    echo "✓ ArcFace model downloaded"
else
    echo "✗ ArcFace model download failed"
    echo "  Manual download needed from:"
    echo "  https://github.com/deepinsight/insightface/releases"
fi

echo ""
echo "========================================="


