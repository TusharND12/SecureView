# Image-Based Authentication Implementation

## Overview
The authentication system has been updated to use a **360-degree multi-angle face capture** approach, similar to mobile face unlock. Instead of using face embeddings, the system now:

1. **Captures multiple face images** from different angles during registration
2. **Saves images locally** to the `Image Data` folder
3. **Compares current face** with stored images during authentication

## Changes Made

### 1. Registration Process (`RegistrationWindow.java`)
- **Multi-angle capture**: Captures 8 face angles (0°, 45°, 90°, 135°, 180°, 225°, 270°, 315°)
- **Interactive UI**: Guides user through each angle with clear instructions
- **Progress tracking**: Shows how many angles have been captured (e.g., "Captured: 3/8 angles")
- **Flexible completion**: Allows registration with at least 3 angles (recommended: 4-8)

### 2. Image Storage (`FaceRecognitionService.java`)
- **New method**: `registerUserMultiAngle(List<Mat> faceImages)`
- **Storage location**: `T:\COLLEGE LIFE\projects\SecureView\SecureView\desktop-app\Image Data`
- **File naming**: Images saved as `face_angle_001.jpg`, `face_angle_002.jpg`, etc.
- **Auto-cleanup**: Clears existing images before saving new registration

### 3. Image Comparison Service (`ImageComparisonService.java`)
- **Multiple comparison methods**:
  - **Template Matching**: Uses OpenCV template matching for structural similarity
  - **Histogram Comparison**: Compares color/intensity distributions
  - **Structural Similarity**: Simplified SSIM calculation
- **Best score selection**: Uses the highest similarity score from all methods
- **Reference image loading**: Automatically loads all images from the Image Data folder

### 4. Authentication Process (`FaceRecognitionService.java`)
- **Image-based authentication**: Primary method uses stored images for comparison
- **Fallback support**: Falls back to embedding-based authentication if no images found
- **Automatic detection**: Detects which registration method was used (images vs embeddings)

### 5. OpenCV Stub Updates
- **Core.java**: Added `normalize()`, `minMaxLoc()`, `subtract()` methods
- **Imgproc.java**: Added `matchTemplate()`, `compareHist()` methods
- **Constants**: Added `TM_CCOEFF_NORMED`, `HISTCMP_CORREL`, `NORM_MINMAX`

## How It Works

### Registration Flow
1. User clicks "Capture Current Angle" for each face position
2. System detects face and captures the image
3. User rotates face to next angle (guided by UI)
4. Process repeats for 8 angles (minimum 3 required)
5. User clicks "Finish Registration"
6. All images are saved to `Image Data` folder

### Authentication Flow
1. System checks if `Image Data` folder contains face images
2. If images exist:
   - Loads all reference images from folder
   - Compares current face with each reference image
   - Uses multiple comparison methods (template matching, histogram, structural)
   - Returns best similarity score
3. If no images (old registration):
   - Falls back to embedding-based authentication

## File Structure

```
SecureView/desktop-app/
├── Image Data/                    # User face images stored here
│   ├── face_angle_001.jpg
│   ├── face_angle_002.jpg
│   ├── face_angle_003.jpg
│   └── ...
├── src/main/java/com/secureview/desktop/
│   ├── RegistrationWindow.java    # Multi-angle capture UI
│   └── face/
│       ├── FaceRecognitionService.java      # Registration & authentication
│       └── comparison/
│           └── ImageComparisonService.java  # Image comparison logic
```

## Usage

### First-Time Registration
1. Run the application
2. When prompted, position your face **front** (0°)
3. Click "Capture Current Angle"
4. Rotate your face to the next angle (45°)
5. Click "Capture Current Angle" again
6. Repeat for all 8 angles (or at least 3)
7. Click "Finish Registration"

### Authentication
- The system automatically uses image-based comparison if images are found
- No changes needed - authentication works as before

### Re-registration
- Use the "Re-register Face" button in the authentication window
- This clears old images and allows new registration

## Benefits

1. **More Secure**: Multiple angles provide better face coverage
2. **Mobile-like Experience**: Similar to modern phone face unlock
3. **Flexible**: Works with 3-8 angles (more angles = better security)
4. **Backward Compatible**: Still supports old embedding-based registration
5. **Local Storage**: All images stored locally, no cloud dependency

## Technical Details

### Image Comparison Methods
- **Template Matching**: Normalized correlation coefficient (0-1 scale)
- **Histogram Comparison**: Correlation between histograms, normalized to 0-1
- **Structural Similarity**: Mean difference calculation, normalized to 0-1

### Similarity Threshold
- Uses the same threshold as before (configurable in `ConfigManager`)
- Default threshold: 0.7 (70% similarity required)

### Performance
- Image comparison is slightly slower than embedding-based (loads multiple images)
- Still fast enough for real-time authentication
- Template matching is the fastest method

## Troubleshooting

### "No face detected" during capture
- Ensure good lighting
- Position face clearly in camera view
- Wait for face detection before clicking capture

### Low similarity scores
- Ensure you captured enough angles (recommended: 4-8)
- Try to match the same angles during authentication
- Check lighting conditions match registration

### Images not saving
- Check that the `Image Data` folder exists and is writable
- Verify disk space is available
- Check application logs for errors

## Future Enhancements

Possible improvements:
- Automatic angle detection (detect when user rotates face)
- Quality check before saving (blur detection, brightness check)
- More sophisticated comparison algorithms
- Face alignment/normalization before comparison

