# Where is My Registered Face Folder? 📁

## Folder Location

Your registered face images are stored in:

```
C:\Users\YOUR_USERNAME\.secureview\data\registered_faces\
```

**Example:**
```
C:\Users\TUSHAR\.secureview\data\registered_faces\
```

## How to Find It

### Method 1: Using File Explorer
1. Press `Windows + R`
2. Type: `%USERPROFILE%\.secureview\data\registered_faces`
3. Press Enter

### Method 2: Using the Script
Run the provided script:
```batch
show-folders.bat
```

This will:
- Show the exact folder location
- List all files in the folder
- Open the folder in File Explorer

### Method 3: Manual Navigation
1. Open File Explorer
2. Navigate to: `C:\Users\YOUR_USERNAME\.secureview\data\registered_faces\`
3. (Replace `YOUR_USERNAME` with your Windows username)

## What's Inside?

After registration, you should see:
- `registered_face.jpg` - Your registered face image

## When is the Folder Created?

The folder is created:
1. **Automatically** when the application starts (if it doesn't exist)
2. **During registration** when you capture your face
3. **When you run** `show-folders.bat` (it creates it if missing)

## Troubleshooting

### Folder Not Found?

1. **Check if you've registered:**
   - The folder is created during registration
   - If you haven't registered yet, the folder won't exist

2. **Create it manually:**
   ```batch
   mkdir "%USERPROFILE%\.secureview\data\registered_faces"
   ```

3. **Run the application:**
   - The app will create the folder automatically on startup

4. **Check the logs:**
   - Location: `%USERPROFILE%\.secureview\logs\`
   - Look for messages about folder creation

## Quick Access

### Create a Desktop Shortcut:
1. Right-click on Desktop → New → Shortcut
2. Enter location:
   ```
   %USERPROFILE%\.secureview\data\registered_faces
   ```
3. Name it: "My Registered Faces"
4. Click Finish

### Add to Quick Access (File Explorer):
1. Navigate to the folder
2. Right-click on "registered_faces" folder
3. Select "Pin to Quick access"

## File Details

### registered_face.jpg
- **Purpose**: Your registered face image
- **Used for**: Authentication comparison
- **Size**: Typically 10-50 KB
- **Format**: JPEG image
- **When created**: During face registration

## Security Note

⚠️ **Important**: This folder contains your biometric data (face image).
- Keep it secure
- Don't share these images
- The folder is in your user directory (private by default)

---

**Need Help?** Run `show-folders.bat` to see the exact location and open the folder!

