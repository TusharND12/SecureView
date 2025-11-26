@echo off
echo Starting SecureView Application...
echo.
echo Model location: %USERPROFILE%\.secureview\models
echo.
java -jar target\secureview-desktop-1.0.0.jar
echo.
echo Application exited.
pause

