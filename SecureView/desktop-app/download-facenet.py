#!/usr/bin/env python3
"""
FaceNet Model Download Script
Automatically downloads and sets up the FaceNet model for SecureView
"""

import os
import sys
import urllib.request
import urllib.error
from pathlib import Path

def main():
    print("=" * 50)
    print("FaceNet Model Download Script")
    print("=" * 50)
    print()
    
    # Create models directory
    models_dir = Path.home() / ".secureview" / "models"
    
    print(f"Creating models directory: {models_dir}")
    models_dir.mkdir(parents=True, exist_ok=True)
    print("[OK] Directory ready")
    print()
    
    model_path = models_dir / "facenet.onnx"
    sface_path = models_dir / "sface.onnx"
    
    # Check if model already exists
    if model_path.exists() or sface_path.exists():
        print("Face recognition model already exists.")
        if model_path.exists():
            print(f"  FaceNet: {model_path}")
        if sface_path.exists():
            print(f"  SFace: {sface_path}")
        response = input("Do you want to download again? (y/n): ")
        if response.lower() != 'y':
            print("Skipping download. Using existing model.")
            return
    
    print("Downloading face recognition model...")
    print("This may take a few minutes (model size: ~10-200 MB)")
    print()
    
    # Try multiple sources for face recognition models
    sources = [
        {
            "name": "SFace (OpenCV Zoo)",
            "url": "https://raw.githubusercontent.com/opencv/opencv_zoo/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx",
            "path": sface_path,
            "description": "SFace model - 128-dim embeddings, lightweight (~10 MB)"
        }
    ]
    
    downloaded = False
    downloaded_model = None
    
    for source in sources:
        print(f"Trying source: {source['name']}")
        print(f"  Description: {source['description']}")
        
        try:
            def show_progress(block_num, block_size, total_size):
                if total_size > 0:
                    downloaded_bytes = block_num * block_size
                    percent = min(downloaded_bytes * 100 / total_size, 100)
                    bar_length = 40
                    filled = int(bar_length * percent / 100)
                    bar = '=' * filled + '-' * (bar_length - filled)
                    print(f"\r  [{bar}] {percent:.1f}%", end='', flush=True)
            
            print("  Downloading...")
            urllib.request.urlretrieve(source['url'], str(source['path']), show_progress)
            print()
            
            # Verify file was downloaded
            if source['path'].exists() and source['path'].stat().st_size > 1000:
                file_size_mb = source['path'].stat().st_size / (1024 * 1024)
                print(f"[OK] Model downloaded successfully!")
                print(f"     Location: {source['path']}")
                print(f"     Size: {file_size_mb:.2f} MB")
                downloaded = True
                downloaded_model = source
                break
            else:
                print(f"  [WARNING] Downloaded file seems too small, trying next source...")
                if source['path'].exists():
                    source['path'].unlink()
        except Exception as e:
            print(f"  [ERROR] Failed: {str(e)}")
            if source['path'].exists():
                source['path'].unlink()
            continue
    
    print()
    
    if downloaded:
        print("=" * 50)
        print("Setup Complete!")
        print("=" * 50)
        print()
        print(f"Model location: {models_dir}")
        print(f"[OK] Model ready: {downloaded_model['path']}")
        print()
        
        if "sface" in downloaded_model['name'].lower():
            print("Note: SFace model (128-dim) is ready to use.")
            print("For FaceNet (512-dim), see instructions below.")
        print()
        print("You can now run SecureView and it will use the model automatically!")
        print()
        
        # Create instructions for FaceNet
        instructions = f"""# FaceNet Model Download Instructions

## Current Status
{downloaded_model['name']} model has been downloaded and is ready to use.
Location: {downloaded_model['path']}

## To Use FaceNet (512-dim) Instead:

### Option 1: Download Pre-converted ONNX Model
1. Visit ONNX Model Zoo: https://github.com/onnx/models
2. Search for "face recognition" or "FaceNet"
3. Download the ONNX model file
4. Place it in: {model_path}

### Option 2: Convert from TensorFlow
1. Download FaceNet from TensorFlow Hub:
   https://tfhub.dev/google/facenet/1

2. Install tf2onnx:
   pip install tf2onnx

3. Convert the model:
   python -m tf2onnx.convert --saved-model <path-to-facenet> --output facenet.onnx

4. Place in: {model_path}

### Option 3: Use Keras-FaceNet Package
1. Install: pip install keras-facenet
2. The package includes pre-trained FaceNet models
3. You'll need to convert to ONNX format for OpenCV

## Note
- Current model ({downloaded_model['name']}): Ready to use
- FaceNet (512-dim): Requires manual download (see above)
- Both models work with SecureView, FaceNet provides higher accuracy
"""
        
        instructions_path = models_dir / "DOWNLOAD_INSTRUCTIONS.txt"
        with open(instructions_path, 'w', encoding='utf-8') as f:
            f.write(instructions)
        
        print(f"Detailed instructions saved to: {instructions_path}")
    else:
        print("[ERROR] All download attempts failed.")
        print()
        print("Creating manual download instructions...")
        
        instructions = f"""# FaceNet Model Download Instructions

## Automatic Download Failed

Please download the model manually:

### Option 1: Download SFace (Lightweight, 128-dim)
Visit: https://github.com/opencv/opencv_zoo/tree/master/models/face_recognition_sface
Download: face_recognition_sface_2021dec.onnx
Save to: {sface_path}

### Option 2: Download FaceNet (High Accuracy, 512-dim)
1. Visit: https://tfhub.dev/google/facenet/1
2. Download and convert to ONNX using tf2onnx
3. Save to: {model_path}

## After Download
Place the model file in: {models_dir}
"""
        
        instructions_path = models_dir / "DOWNLOAD_INSTRUCTIONS.txt"
        with open(instructions_path, 'w', encoding='utf-8') as f:
            f.write(instructions)
        
        print(f"Instructions saved to: {instructions_path}")
        sys.exit(1)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nDownload cancelled by user.")
        sys.exit(1)
    except Exception as e:
        print(f"\n[ERROR] Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
