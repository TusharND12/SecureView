@echo off
echo This will clear your existing face registration.
echo You will need to re-register your face with the new SFace model.
echo.
pause

REM Clear registration
if exist "%USERPROFILE%\.secureview\data\face_embedding.enc" (
    del "%USERPROFILE%\.secureview\data\face_embedding.enc"
    echo Deleted old face embedding
)

if exist "%USERPROFILE%\.secureview\data\registered_faces\registered_face.jpg" (
    del "%USERPROFILE%\.secureview\data\registered_faces\registered_face.jpg"
    echo Deleted old registered face image
)

echo.
echo Registration cleared. Please restart the application to register again.
echo.
pause

