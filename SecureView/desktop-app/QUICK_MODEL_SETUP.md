# Quick Model Setup - FaceNet/SFace

## ✅ What I've Done For You

1. **Updated Code**: Your application now supports FaceNet (512-dim) and SFace (128-dim) models
2. **Created Download Scripts**: 
   - `download-facenet.py` - Python script
   - `download-facenet.ps1` - PowerShell script  
   - `download-facenet.bat` - Windows batch file
3. **Created Models Directory**: `~/.secureview/models/`

## 🚀 Quick Setup (Choose One)

### Option 1: Use SFace Model (Recommended for Quick Start)

**SFace is lightweight (10 MB) and works great!**

1. **Download manually**:
   - Visit: https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface
   - Click on `face_recognition_sface_2021dec.onnx`
   - Click "Download" or "Raw"
   - Save to: `C:\Users\TUSHAR\.secureview\models\sface.onnx`

2. **Or use command line**:
   ```powershell
   # Create directory
   New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\.secureview\models"
   
   # Download (using curl or Invoke-WebRequest)
   Invoke-WebRequest -Uri "https://github.com/opencv/opencv_zoo/raw/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx" -OutFile "$env:USERPROFILE\.secureview\models\sface.onnx"
   ```

### Option 2: Use FaceNet Model (Higher Accuracy)

**FaceNet provides 512-dim embeddings for better accuracy**

1. **Download from TensorFlow Hub**:
   - Visit: https://tfhub.dev/google/facenet/1
   - Download the model files
   - Convert to ONNX using tf2onnx:
     ```bash
     pip install tf2onnx
     python -m tf2onnx.convert --saved-model <path-to-facenet> --output facenet.onnx
     ```
   - Place in: `C:\Users\TUSHAR\.secureview\models\facenet.onnx`

2. **Or find pre-converted ONNX**:
   - Search GitHub for "facenet onnx"
   - Download and place in models directory

## 📁 File Locations

Your models should be in:
```
C:\Users\TUSHAR\.secureview\models\
├── sface.onnx          (SFace - 128-dim, ~10 MB)
└── facenet.onnx        (FaceNet - 512-dim, ~100-200 MB)
```

## ✅ Verification

After placing the model file:

1. **Check file exists**:
   ```powershell
   dir "$env:USERPROFILE\.secureview\models"
   ```

2. **Run SecureView**:
   - The app will automatically detect and load the model
   - Check logs for: "Face recognition model loaded successfully"

3. **Re-register your face** (if switching models):
   - FaceNet uses 512-dim embeddings
   - SFace uses 128-dim embeddings
   - You may need to re-register when switching

## 🎯 Current Status

- ✅ Code updated to support FaceNet
- ✅ Auto-detection configured
- ✅ Models directory created
- ⏳ Model file needs to be downloaded (see options above)

## 💡 Recommendation

**Start with SFace** - it's:
- Small (10 MB)
- Fast to download
- Works great for face recognition
- Easy to get started

**Upgrade to FaceNet later** if you need:
- Higher accuracy
- 512-dim embeddings
- State-of-the-art performance

## 📝 Next Steps

1. Download a model (SFace recommended for quick start)
2. Place it in `~/.secureview/models/`
3. Run SecureView
4. Re-register your face
5. Test authentication

---

**Note**: The download scripts I created will help, but manual download from GitHub is often more reliable due to redirects and authentication requirements.

