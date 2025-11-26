# Rename model file to match expected name
$modelsDir = "$env:USERPROFILE\.secureview\models"
$oldName = "$modelsDir\face_recognition_sface_2021dec.onnx"
$newName = "$modelsDir\sface.onnx"

if (Test-Path $oldName) {
    if (Test-Path $newName) {
        Write-Host "[INFO] sface.onnx already exists, keeping both files" -ForegroundColor Yellow
    } else {
        Rename-Item -Path $oldName -NewName "sface.onnx"
        Write-Host "[OK] Renamed to sface.onnx" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Model files in directory:" -ForegroundColor Cyan
Get-ChildItem $modelsDir -Filter "*.onnx" | ForEach-Object {
    $sizeMB = [math]::Round($_.Length / 1MB, 2)
    Write-Host "  $($_.Name) - $sizeMB MB" -ForegroundColor White
}

