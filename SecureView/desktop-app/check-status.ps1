# Check if SecureView is running
Write-Host "Checking SecureView status..." -ForegroundColor Cyan
Write-Host ""

$processes = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*secureview*" -or $_.Path -like "*secureview*"
}

if ($processes) {
    Write-Host "[OK] SecureView is running!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Processes:" -ForegroundColor Yellow
    $processes | ForEach-Object {
        Write-Host "  PID: $($_.Id) - $($_.ProcessName)" -ForegroundColor White
    }
    Write-Host ""
    Write-Host "The application window should be visible." -ForegroundColor Green
    Write-Host "If you don't see it, check:" -ForegroundColor Yellow
    Write-Host "  1. Taskbar for minimized window" -ForegroundColor Gray
    Write-Host "  2. Alt+Tab to switch windows" -ForegroundColor Gray
    Write-Host "  3. Check logs at: $env:USERPROFILE\.secureview\logs\" -ForegroundColor Gray
} else {
    Write-Host "[INFO] SecureView process not found" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To start the application, run:" -ForegroundColor Cyan
    Write-Host "  java -jar target\secureview-desktop-1.0.0.jar" -ForegroundColor White
    Write-Host ""
    Write-Host "Or use:" -ForegroundColor Cyan
    Write-Host "  .\run.bat" -ForegroundColor White
}

Write-Host ""
Write-Host "Model status:" -ForegroundColor Cyan
$modelPath = "$env:USERPROFILE\.secureview\models\sface.onnx"
if (Test-Path $modelPath) {
    $size = [math]::Round((Get-Item $modelPath).Length / 1MB, 2)
    Write-Host "  [OK] SFace model found ($size MB)" -ForegroundColor Green
} else {
    Write-Host "  [WARNING] Model not found" -ForegroundColor Yellow
}

