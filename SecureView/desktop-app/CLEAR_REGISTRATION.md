# How to Clear Registration and Re-register Your Face

## Problem
If the application opens the **Authentication Window** directly instead of the **Registration Window**, it means there's already a registered face. You need to clear it first.

## Solution Options

### Option 1: Use Force Registration Script (Easiest) ⭐

1. **Run the force registration script:**
   ```bash
   .\force-register.bat
   ```
   
   This will:
   - Clear existing registration automatically
   - Open registration window
   - Allow you to register your face

### Option 2: Use Command-Line Argument

1. **Run with `--register` flag:**
   ```bash
   java -jar target\secureview-desktop-1.0.0.jar --register
   ```
   
   Or with PowerShell:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\run.ps1 --register
   ```

### Option 3: Use Re-register Button (If Authentication Window is Open)

1. **In the Authentication Window:**
   - Look for **"Re-register Face"** button (top-right)
   - Click it
   - Confirm the action
   - Application will close
   - Restart the application to register

### Option 4: Manually Delete Registration File

1. **Find the registration file:**
   - Location: `C:\Users\<YourUsername>\.secureview\data\face_embedding.enc`
   - Or: `%USERPROFILE%\.secureview\data\face_embedding.enc`

2. **Delete the file:**
   - Open File Explorer
   - Navigate to: `C:\Users\<YourUsername>\.secureview\data\`
   - Delete `face_embedding.enc`
   - Restart the application

3. **Using Command Prompt:**
   ```cmd
   del "%USERPROFILE%\.secureview\data\face_embedding.enc"
   ```

## Quick Commands

### Windows Command Prompt:
```cmd
del "%USERPROFILE%\.secureview\data\face_embedding.enc"
```

### PowerShell:
```powershell
Remove-Item "$env:USERPROFILE\.secureview\data\face_embedding.enc" -ErrorAction SilentlyContinue
```

### Run with Force Registration:
```cmd
.\force-register.bat
```

## After Clearing Registration

1. **Restart the application**
2. **Registration window will appear**
3. **Follow registration steps:**
   - Position your face
   - Click "Capture Face"
   - Click "Finish Registration"

## Verification

To verify registration was cleared:
1. Check if file exists:
   ```cmd
   dir "%USERPROFILE%\.secureview\data\face_embedding.enc"
   ```
2. If file doesn't exist → Registration cleared ✅
3. If file exists → Still registered

## Troubleshooting

### "File not found" when trying to delete
- **Cause**: File doesn't exist (already cleared)
- **Solution**: Just restart the application

### "Access denied" when deleting
- **Cause**: File is in use or permission issue
- **Solution**: 
  - Close the application first
  - Run as administrator
  - Or use the force-register script

### Application still opens Authentication
- **Cause**: File still exists
- **Solution**: 
  - Verify file was deleted
  - Check correct user directory
  - Use force-register script instead

---

**Recommended**: Use `force-register.bat` for the easiest way to clear and re-register! 🚀

