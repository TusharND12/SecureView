# FaceNet Model Setup Guide

## Overview

FaceNet is now configured as the primary face recognition model for SecureView. FaceNet provides high-accuracy face recognition using 512-dimensional embeddings.

## FaceNet Specifications

- **Input Size**: 160x160 pixels
- **Embedding Size**: 512 dimensions
- **Model Format**: ONNX (.onnx) or TensorFlow (.pb)
- **Accuracy**: Very high (state-of-the-art)
- **Model Size**: ~100-200 MB

## Download Options

### Option 1: Pre-converted ONNX Model (Recommended)

**Easiest method** - Download a pre-converted FaceNet ONNX model:

1. **From ONNX Model Zoo**:
   - Search for "FaceNet" or "face recognition" models
   - Download the ONNX format model
   - Place in: `~/.secureview/models/facenet.onnx`

2. **From GitHub**:
   - Many repositories provide pre-converted FaceNet ONNX models
   - Example: https://github.com/onnx/models (search for face recognition)

### Option 2: Convert TensorFlow FaceNet to ONNX

If you have a TensorFlow FaceNet model:

1. **Download FaceNet from TensorFlow Hub**:
   ```bash
   # Visit: https://tfhub.dev/google/facenet/1
   # Download the model files
   ```

2. **Convert to ONNX**:
   ```bash
   # Install tf2onnx
   pip install tf2onnx
   
   # Convert the model
   python -m tf2onnx.convert --saved-model /path/to/facenet --output facenet.onnx
   ```

3. **Place the converted model**:
   ```bash
   # Windows
   mkdir %USERPROFILE%\.secureview\models
   copy facenet.onnx %USERPROFILE%\.secureview\models\
   
   # Linux/Mac
   mkdir -p ~/.secureview/models
   cp facenet.onnx ~/.secureview/models/
   ```

### Option 3: Use TensorFlow .pb Format Directly

If you have a TensorFlow SavedModel or .pb file:

1. **Download FaceNet**:
   - From TensorFlow Hub: https://tfhub.dev/google/facenet/1
   - Extract the model files

2. **Place the model**:
   ```bash
   # Place facenet.pb in:
   ~/.secureview/models/facenet.pb
   ```

## Quick Setup Steps

### Step 1: Create Models Directory

**Windows:**
```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\.secureview\models"
```

**Linux/Mac:**
```bash
mkdir -p ~/.secureview/models
```

### Step 2: Download FaceNet Model

Choose one of the download options above and place the model file in:
- `~/.secureview/models/facenet.onnx` (preferred)
- OR `~/.secureview/models/facenet.pb`

### Step 3: Verify Installation

1. Start your SecureView application
2. Check the logs - you should see:
   ```
   INFO: Detected FaceNet model - using 160x160 input, 512-dim embeddings
   INFO: Face recognition model loaded successfully from: ...
   ```

## Model File Locations

The application will automatically search for FaceNet in these locations (in order):

1. `~/.secureview/models/facenet.onnx`
2. `~/.secureview/models/facenet.pb`
3. `models/facenet.onnx` (relative to application)
4. `models/facenet.pb` (relative to application)

## FaceNet vs Other Models

| Feature | FaceNet | OpenFace | SFace |
|---------|---------|----------|-------|
| Embedding Size | 512 | 128 | 128 |
| Input Size | 160x160 | 96x96 | 96x96 |
| Accuracy | Very High | Good | High |
| Model Size | ~100-200 MB | ~50 MB | ~10 MB |
| Speed | Medium | Fast | Very Fast |

## Troubleshooting

### "Model file not found"
- Ensure the model file is in the correct location
- Check file name matches: `facenet.onnx` or `facenet.pb`
- Verify file permissions (read access required)

### "Failed to load model"
- Check if the model format is supported (.onnx or .pb)
- Verify OpenCV DNN module supports the model version
- Try downloading a different FaceNet model variant

### "Embedding size mismatch"
- FaceNet should produce 512-dim embeddings
- If you see errors, the model might not be FaceNet
- Check logs for detected model type

### Model Loading Issues
- Ensure OpenCV was built with DNN support
- Check OpenCV version (4.5+ recommended)
- Verify model file is not corrupted

## Testing FaceNet

After setup, test the model:

1. **Register a face** - The system will extract 512-dim embeddings
2. **Authenticate** - Compare with registered face
3. **Check logs** - Should show FaceNet model loaded and working

## Performance Notes

- **First load**: May take 2-5 seconds to load the model
- **Inference**: ~50-100ms per face (depending on hardware)
- **Memory**: ~200-300 MB additional RAM usage
- **Accuracy**: FaceNet typically achieves 99%+ accuracy

## Alternative: Quick Test with SFace

If you need a quick test without downloading FaceNet:

1. Download SFace (smaller, faster):
   ```bash
   # From: https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface
   # Place as: ~/.secureview/models/sface.onnx
   ```

2. SFace uses 128-dim embeddings (compatible with existing registrations)

## Next Steps

1. ✅ Download FaceNet model
2. ✅ Place in `~/.secureview/models/`
3. ✅ Restart application
4. ✅ Re-register your face (to use 512-dim embeddings)
5. ✅ Test authentication

## Support

If you encounter issues:
- Check application logs in `~/.secureview/logs/`
- Verify model file integrity
- Ensure OpenCV DNN module is working
- Try with a different FaceNet model variant

---

**Note**: After installing FaceNet, you may need to re-register your face since FaceNet uses 512-dim embeddings instead of 128-dim. The system will automatically handle this.

