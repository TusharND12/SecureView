# SecureView Model Installation Guide

## 📍 MODEL LOCATION

**All models must be placed in:**
```
~/.secureview/models/
```

**Full path:**
```
/home/aryan-budukh/.secureview/models/
```

## 📦 REQUIRED MODELS

### 1. RetinaFace (Face Detection)
- **File name:** `retinaface.onnx`
- **Location:** `~/.secureview/models/retinaface.onnx`
- **Expected size:** ~1.7 MB
- **Purpose:** Detects faces in images

### 2. ArcFace/SFace (Face Recognition)
- **File name:** `arcface.onnx` OR `sface.onnx`
- **Location:** `~/.secureview/models/arcface.onnx`
- **Expected size:** ~10-100 MB
- **Purpose:** Extracts face features for recognition

## 🔍 CHECK CURRENT STATUS

Run this command to check your models:
```bash
ls -lh ~/.secureview/models/
```

**Expected output if models are installed:**
```
-rw-rw-r-- 1 user user 1.7M Nov 27 12:00 retinaface.onnx
-rw-rw-r-- 1 user user  45M Nov 27 12:00 arcface.onnx
```

**If files are 0 bytes or missing, they need to be downloaded!**

## 📥 HOW TO DOWNLOAD MODELS

### Option 1: Manual Download (Recommended)

1. **RetinaFace:**
   - Visit: https://github.com/opencv/opencv_zoo/tree/master/models/face_detection_retinaface
   - Download: `face_detection_retinaface_2023mar.onnx`
   - Save as: `~/.secureview/models/retinaface.onnx`

2. **SFace (ArcFace alternative):**
   - Visit: https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface
   - Download: `face_recognition_sface_2021dec.onnx`
   - Save as: `~/.secureview/models/arcface.onnx`

### Option 2: Using wget/curl

```bash
# Create directory
mkdir -p ~/.secureview/models
cd ~/.secureview/models

# Download RetinaFace
wget https://github.com/opencv/opencv_zoo/raw/master/models/face_detection_retinaface/face_detection_retinaface_2023mar.onnx -O retinaface.onnx

# Download SFace
wget https://github.com/opencv/opencv_zoo/raw/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx -O arcface.onnx

# Verify downloads
ls -lh ~/.secureview/models/
```

### Option 3: Alternative Sources

If GitHub URLs don't work, try:
- **Hugging Face:** https://huggingface.co/models?search=retinaface
- **ONNX Model Zoo:** https://github.com/onnx/models
- **InsightFace Releases:** https://github.com/deepinsight/insightface/releases

## ✅ VERIFY INSTALLATION

After downloading, verify:
```bash
# Check file sizes (should be > 1MB)
ls -lh ~/.secureview/models/*.onnx

# Check file type
file ~/.secureview/models/*.onnx

# Should show: "ONNX model data" or "data"
```

## 🚨 TROUBLESHOOTING

### Problem: Models are 0 bytes
**Solution:** Delete and re-download:
```bash
rm ~/.secureview/models/*.onnx
# Then download again using Option 2 above
```

### Problem: "model_proto.has_graph()" error
**Solution:** The model file is corrupted or empty. Delete and re-download.

### Problem: Models not being used
**Solution:** 
1. Check file sizes: `ls -lh ~/.secureview/models/`
2. Files must be > 1MB
3. Restart the application after installing models

## 📝 CURRENT STATUS

Run this to check:
```bash
echo "=== Model Status ==="
echo "Location: ~/.secureview/models/"
echo ""
ls -lh ~/.secureview/models/ 2>/dev/null || echo "Directory doesn't exist!"
echo ""
echo "File sizes:"
find ~/.secureview/models -name "*.onnx" -exec ls -lh {} \; 2>/dev/null || echo "No ONNX files found"
```

## 🎯 WHAT THE APPLICATION LOOKS FOR

The application checks these paths in order:
1. `~/.secureview/models/arcface.onnx`
2. `~/.secureview/models/facenet.onnx`
3. `~/.secureview/models/sface.onnx`
4. `~/.secureview/models/face_recognition_sface_2021dec.onnx`
5. Other model formats (`.pb`, `.t7`)

**For RetinaFace:**
- `~/.secureview/models/retinaface.onnx`

## ⚠️ IMPORTANT NOTES

1. **File size matters:** Models must be > 1MB. Empty files (0 bytes) are ignored.
2. **File format:** Must be `.onnx` format for best compatibility
3. **Naming:** Files must be named exactly as shown above
4. **Restart required:** Restart the application after installing models

## 🔄 WITHOUT MODELS

If models are not installed, the system will:
- Use **Haar Cascade** for face detection (less accurate)
- Use **simplified embeddings** for recognition (works but less accurate)

**Models improve accuracy significantly!**



