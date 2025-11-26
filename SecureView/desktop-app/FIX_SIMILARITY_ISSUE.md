# Fix: Similarity Score 0.000 Issue

## Problem
The application shows similarity score of 0.000, meaning face recognition is not working.

## Root Cause
Your face was registered using the **old simplified embedding method** (histogram-based), but now the application is using the **SFace model** (deep learning). These two methods produce incompatible embeddings, so they can't be compared.

## Solution: Re-register Your Face

You need to re-register your face with the new SFace model.

### Option 1: Use Force Registration (Recommended)

1. **Run the force registration script**:
   ```batch
   .\force-reregister.bat
   ```

2. **Or manually delete the old registration**:
   ```batch
   del "%USERPROFILE%\.secureview\data\face_embedding.enc"
   del "%USERPROFILE%\.secureview\data\registered_faces\registered_face.jpg"
   ```

3. **Restart the application** - it will show the registration window

### Option 2: Use Command Line Flag

Run the application with the `--register` flag:
```batch
java -jar target\secureview-desktop-1.0.0.jar --register
```

This will automatically clear the old registration and show the registration window.

## What Happens After Re-registration

1. ✅ SFace model (36.9 MB) will extract 128-dimensional embeddings
2. ✅ New embeddings will be saved (encrypted)
3. ✅ Authentication will work with similarity scores > 0.85 (typical)

## Verification

After re-registering, check the logs for:
- "Face recognition model loaded successfully from: ..."
- "Detected SFace model - using 96x96 input, 128-dim embeddings"
- "Face embedding extracted successfully. Dimensions: 128"
- Similarity scores should be > 0.85 for your face

## Why This Happened

- **Old method**: Histogram-based features (128-dim, but different features)
- **New method**: SFace deep learning model (128-dim, learned features)
- **Result**: Incompatible - can't compare old vs new embeddings

## Next Steps

1. Run `.\force-reregister.bat` or delete old registration files
2. Restart the application
3. Register your face again
4. Test authentication - should work now!

