# How to Register Your Face in SecureView

## Step-by-Step Registration Guide

### Prerequisites
- ✅ Webcam connected and working
- ✅ Good lighting (face should be clearly visible)
- ✅ Application running

---

## Registration Process

### Step 1: Launch Application
When you first start SecureView, if no face is registered, the **Registration Window** will appear automatically.

**What you'll see:**
- Live camera feed showing your webcam
- Status message: "Position your face in front of the camera and click 'Capture'"
- "Capture Face" button
- "Finish Registration" button (disabled until face is captured)

### Step 2: Position Your Face
1. **Sit in front of your webcam**
2. **Ensure good lighting:**
   - Face should be well-lit
   - Avoid backlighting (bright light behind you)
   - Natural light or room lighting works best
3. **Position yourself:**
   - Face the camera directly
   - Look straight ahead
   - Keep your face centered in the camera view
   - Maintain a neutral expression
4. **Remove obstructions:**
   - Remove glasses (if possible) for better detection
   - Remove hat or cap
   - Keep hair away from face
   - Remove face masks

### Step 3: Capture Your Face
1. **Click the "Capture Face" button**
2. **Wait for confirmation:**
   - Status will change to "Capturing face..."
   - If face is detected: "Face captured! Click 'Finish Registration' to complete."
   - If no face detected: "No face detected. Please try again."
3. **If face not detected:**
   - Adjust your position
   - Improve lighting
   - Ensure face is clearly visible
   - Click "Capture Face" again

### Step 4: Complete Registration
1. **Once face is captured:**
   - "Finish Registration" button becomes enabled
   - Status shows: "Face captured! Click 'Finish Registration' to complete."
2. **Click "Finish Registration"**
3. **Wait for processing:**
   - Status shows: "Registering user... Please wait..."
   - System processes your face:
     - Extracts face embedding
     - Encrypts the data
     - Saves to secure storage
4. **Success message appears:**
   - "User registered successfully!"
   - "You will now be taken to the authentication screen."
5. **Automatic transition:**
   - Registration window closes
   - Authentication window opens automatically

---

## Visual Guide

```
┌─────────────────────────────────────────┐
│   SecureView - User Registration        │
├─────────────────────────────────────────┤
│                                         │
│   ┌───────────────────────────────┐    │
│   │                               │    │
│   │     [Live Camera Feed]        │    │
│   │                               │    │
│   │   Your face should appear     │    │
│   │   clearly in this area        │    │
│   │                               │    │
│   └───────────────────────────────┘    │
│                                         │
│   Status: Position your face...         │
│                                         │
│   [Capture Face]  [Finish Registration] │
│                                         │
└─────────────────────────────────────────┘
```

---

## Tips for Best Results

### ✅ DO:
- **Good lighting**: Face should be clearly visible
- **Direct eye contact**: Look at the camera
- **Neutral expression**: Natural, relaxed face
- **Stable position**: Sit still while capturing
- **Clean background**: Simple background helps

### ❌ DON'T:
- **Don't move**: Stay still while capturing
- **Don't wear sunglasses**: Eyes must be visible
- **Don't cover face**: Remove masks, scarves
- **Don't use poor lighting**: Dark or backlit
- **Don't be too close/far**: Maintain proper distance (2-3 feet)

---

## Troubleshooting

### "No face detected"
**Possible causes:**
- Face not clearly visible
- Poor lighting
- Face too close or too far
- Camera not working

**Solutions:**
1. Check camera is working (you should see yourself)
2. Improve lighting
3. Adjust distance from camera (2-3 feet)
4. Ensure face is centered
5. Remove glasses/hats if needed
6. Click "Capture Face" again

### "Registration failed"
**Possible causes:**
- Face embedding extraction failed
- Liveness detection failed
- Technical error

**Solutions:**
1. Try capturing again with better lighting
2. Ensure stable internet (if using cloud features)
3. Check application logs for details
4. Restart application and try again

### Camera not showing
**Solutions:**
1. Check camera permissions
2. Ensure no other app is using camera
3. Restart application
4. Check camera drivers

---

## What Happens After Registration

1. **Face embedding is saved:**
   - Location: `~/.secureview/data/face_embedding.enc`
   - Format: Encrypted mathematical representation
   - Security: AES-256-GCM encryption

2. **Authentication window opens:**
   - System now compares your face with registered face
   - Continuous monitoring starts
   - Ready for authentication

3. **You can now authenticate:**
   - Position face in camera
   - System compares with registered face
   - Unlocks if match found

---

## Re-registration

If you need to re-register (update your face):
1. Delete the file: `~/.secureview/data/face_embedding.enc`
2. Restart the application
3. Registration window will appear again
4. Follow the same steps

---

## Security Notes

- **Your face data is encrypted** before storage
- **Only mathematical representation** is stored (not actual image)
- **Cannot be reverse-engineered** to recreate your face
- **Stored locally** on your computer only
- **Never sent to servers** (unless intrusion detected)

---

## Quick Reference

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Launch app | Registration window appears |
| 2 | Position face | Face visible in camera |
| 3 | Click "Capture Face" | "Face captured!" message |
| 4 | Click "Finish Registration" | "Registration Complete" |
| 5 | Wait | Authentication window opens |

---

**That's it!** Once registered, you can use face authentication every time you start the application.

