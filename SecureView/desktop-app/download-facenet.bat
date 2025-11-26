@echo off
REM FaceNet Model Download Script for Windows
REM This script automatically downloads and sets up the FaceNet model

echo ========================================
echo FaceNet Model Download Script
echo ========================================
echo.

REM Create models directory
set "MODELS_DIR=%USERPROFILE%\.secureview\models"
echo Creating models directory: %MODELS_DIR%

if not exist "%MODELS_DIR%" (
    mkdir "%MODELS_DIR%"
    echo [OK] Directory created
) else (
    echo [OK] Directory already exists
)

echo.
echo Checking for Python...
python --version >nul 2>&1
if %errorlevel% == 0 (
    echo [OK] Python found
    echo.
    echo Running Python download script...
    python download-facenet.py
) else (
    echo [WARNING] Python not found
    echo.
    echo Running PowerShell script instead...
    powershell -ExecutionPolicy Bypass -File download-facenet.ps1
)

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Model location: %MODELS_DIR%
echo.
pause

