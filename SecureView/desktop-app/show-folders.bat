@echo off
echo ========================================
echo SecureView - Folder Locations
echo ========================================
echo.

set "USERPROFILE=%USERPROFILE%"
set "DATA_DIR=%USERPROFILE%\.secureview\data"
set "REGISTERED_FACES_DIR=%DATA_DIR%\registered_faces"

echo Data Directory: %DATA_DIR%
echo Registered Faces Directory: %REGISTERED_FACES_DIR%
echo.

if exist "%DATA_DIR%" (
    echo [OK] Data directory exists
    dir "%DATA_DIR%" /b
) else (
    echo [MISSING] Data directory does not exist
    echo It will be created when you register your face.
)

echo.
if exist "%REGISTERED_FACES_DIR%" (
    echo [OK] Registered faces directory exists
    echo.
    echo Contents:
    dir "%REGISTERED_FACES_DIR%" /b
) else (
    echo [MISSING] Registered faces directory does not exist
    echo It will be created when you register your face.
)

echo.
echo ========================================
echo To open the folder in File Explorer:
echo explorer "%REGISTERED_FACES_DIR%"
echo ========================================
echo.
pause

