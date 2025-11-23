# Push SecureView to GitHub

## Step 1: Create a GitHub Repository

1. Go to https://github.com/new
2. Repository name: `SecureView` (or any name you prefer)
3. Description: "Advanced Laptop Authentication System with Face Recognition, Firebase Integration, and Android Companion App"
4. Choose **Public** or **Private**
5. **DO NOT** initialize with README, .gitignore, or license (we already have these)
6. Click "Create repository"

## Step 2: Add Remote and Push

After creating the repository, GitHub will show you commands. Use these:

```bash
cd "T:\COLLEGE LIFE\projects\SecureView"

# Add your GitHub repository as remote (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/SecureView.git

# Push to GitHub
git push -u origin main
```

## Alternative: Using SSH

If you have SSH keys set up:

```bash
git remote add origin git@github.com:YOUR_USERNAME/SecureView.git
git push -u origin main
```

## Step 3: Verify

After pushing, visit your repository on GitHub to verify all files are uploaded.

## What's Included

✅ Complete Java desktop application
✅ Android companion app
✅ All source code
✅ Documentation (README, SETUP_GUIDE, ARCHITECTURE)
✅ Configuration files
✅ Build scripts

## What's Excluded (via .gitignore)

❌ Compiled JAR files
❌ Build artifacts
❌ Firebase credentials
❌ User configuration files
❌ Encryption keys
❌ Face embeddings
❌ Intruder images

## Next Steps After Push

1. Update README with your repository URL
2. Add repository topics: `java`, `opencv`, `face-recognition`, `firebase`, `android`, `security`, `authentication`
3. Add a license file if needed
4. Create releases for stable versions

