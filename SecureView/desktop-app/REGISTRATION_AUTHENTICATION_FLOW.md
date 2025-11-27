# SecureView - Registration and Authentication Flow

## Overview

SecureView follows a **two-step process**:
1. **REGISTRATION** - User must register their face FIRST
2. **AUTHENTICATION** - System compares detected face with registered face

## Step 1: Registration (MUST HAPPEN FIRST) 🔐

### What Happens:
1. **Application Starts** → Checks if user is registered
2. **If NOT Registered** → Shows Registration Window
3. **User Captures Face** → Clicks "Capture Face" button
4. **System Processes**:
   - Detects face in captured image
   - Extracts face embedding (mathematical representation)
   - Encrypts embedding using AES-256-GCM
   - Stores encrypted embedding to disk: `face_embedding.enc`
5. **Registration Complete** → Face data saved securely

### Registration Process Details:
```
User Face → Face Detection → Embedding Extraction → Encryption → Storage
```

**Storage Location**: `~/.secureview/data/face_embedding.enc`

**Security**: 
- Face embedding is encrypted
- Only mathematical representation stored (not actual image)
- Cannot be reverse-engineered to recreate face

## Step 2: Authentication (Compares with Registered Face) ✅

### What Happens:
1. **Application Starts** → Checks if user is registered
2. **If Registered** → Shows Authentication Window
3. **Continuous Monitoring**:
   - Camera captures frames
   - System detects faces in frames
   - For each detected face:
     - Extracts face embedding (same process as registration)
     - Loads stored embedding from `face_embedding.enc`
     - **COMPARES** current embedding with stored embedding
     - Calculates similarity score (0.0 to 1.0)
4. **Decision**:
   - If similarity ≥ threshold → **SUCCESS** (match found)
   - If similarity < threshold → **FAILURE** (no match)

### Authentication Process Details:
```
Camera Frame → Face Detection → Embedding Extraction → Load Stored Embedding → Compare → Similarity Score
```

**Comparison Method**: Cosine Similarity
- **1.0** = Perfect match (same person)
- **0.8-0.9** = Very similar (likely same person)
- **0.6-0.7** = Somewhat similar (might be same person)
- **< 0.6** = Different person

**Threshold**: Configurable (default: 0.6)
- Lower threshold = More lenient (easier to authenticate)
- Higher threshold = More strict (harder to authenticate)

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION START                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │  Is User Registered?  │
            └───────┬───────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
        ▼                       ▼
   ┌─────────┐          ┌──────────────┐
   │   NO    │          │     YES      │
   └────┬────┘          └──────┬───────┘
        │                     │
        ▼                     ▼
┌───────────────┐    ┌──────────────────┐
│ REGISTRATION  │    │  AUTHENTICATION  │
│   WINDOW      │    │     WINDOW       │
└───────┬───────┘    └────────┬─────────┘
        │                     │
        ▼                     ▼
┌───────────────┐    ┌──────────────────┐
│ 1. Capture    │    │ 1. Detect Face   │
│    Face       │    │    in Frame      │
│               │    │                  │
│ 2. Extract    │    │ 2. Extract       │
│    Embedding  │    │    Embedding     │
│               │    │                  │
│ 3. Encrypt    │    │ 3. Load Stored   │
│    & Store    │    │    Embedding     │
│               │    │                  │
│ 4. Save to    │    │ 4. COMPARE       │
│    Disk       │    │    Embeddings    │
└───────┬───────┘    │                  │
        │            │ 5. Calculate     │
        │            │    Similarity    │
        │            │                  │
        │            │ 6. Match?        │
        │            │    YES → Success │
        │            │    NO  → Failure │
        │            └──────────────────┘
        │
        ▼
┌───────────────┐
│ Registration  │
│   Complete    │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Transition to │
│ Authentication│
└───────────────┘
```

## Key Points

### ✅ Registration is MANDATORY
- **Cannot authenticate without registration**
- Application checks: `isUserRegistered()`
- If not registered → Shows registration window
- If registered → Shows authentication window

### ✅ Authentication COMPARES with Registered Face
- **Loads stored embedding** from registration
- **Extracts current embedding** from camera
- **Compares both embeddings** using cosine similarity
- **Decides** based on similarity score

### ✅ Security Features
- Face embeddings are encrypted
- Only mathematical representation stored
- Cannot recreate face from embedding
- Liveness detection prevents spoofing

## Logging

The system logs detailed information:

### Registration Logs:
```
=== REGISTRATION PROCESS STARTED ===
Step 1: Registering new user...
Step 2: Extracting face embedding...
Face embedding extracted successfully. Dimensions: 128
Step 3: Encrypting and storing face embedding...
=== REGISTRATION COMPLETE ===
Face embedding stored at: ~/.secureview/data/face_embedding.enc
User registered successfully. Authentication can now compare faces.
```

### Authentication Logs:
```
=== AUTHENTICATION PROCESS STARTED ===
Step 1: Loading registered face embedding...
Registered embedding loaded. Dimensions: 128
Step 2: Extracting current face embedding...
Current embedding extracted. Dimensions: 128
Step 3: Comparing registered face with current face...
=== AUTHENTICATION RESULT ===
Face similarity score: 0.85 (1.0 = perfect match, 0.0 = no match)
Threshold: 0.6
Match: YES
```

## Testing the Flow

### Test Registration:
1. Delete `~/.secureview/data/face_embedding.enc` (if exists)
2. Start application → Should show registration window
3. Capture face → Should save embedding
4. Check logs → Should see "REGISTRATION COMPLETE"

### Test Authentication:
1. Start application (with registered face)
2. Should show authentication window
3. Position face in camera
4. Check logs → Should see "COMPARING registered face with current face"
5. Should see similarity score and match result

## Troubleshooting

### "No registered user found"
- **Cause**: Registration not completed
- **Solution**: Complete registration first

### "Authentication failed" (even with your face)
- **Cause**: Similarity score below threshold
- **Solution**: 
  - Lower threshold in config
  - Re-register with better lighting
  - Ensure face is clearly visible

### "Failed to extract face embedding"
- **Cause**: Face detection or embedding extraction failed
- **Solution**: 
  - Check camera is working
  - Ensure good lighting
  - Position face directly in front of camera

---

**Summary**: Registration happens FIRST and stores your face embedding. Authentication compares the current face with the registered face embedding to determine if it's the same person.




