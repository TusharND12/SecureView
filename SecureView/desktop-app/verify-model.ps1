# Verify Model Setup
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Model Setup Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$modelsDir = "$env:USERPROFILE\.secureview\models"

if (Test-Path $modelsDir) {
    Write-Host "[OK] Models directory exists" -ForegroundColor Green
    Write-Host "Location: $modelsDir" -ForegroundColor Gray
    Write-Host ""
    
    $models = Get-ChildItem $modelsDir -Filter "*.onnx" -ErrorAction SilentlyContinue
    if ($models) {
        Write-Host "Model files found:" -ForegroundColor Yellow
        foreach ($model in $models) {
            $sizeMB = [math]::Round($model.Length / 1MB, 2)
            Write-Host "  [OK] $($model.Name) - $sizeMB MB" -ForegroundColor Green
        }
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "Setup Complete!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Your model is ready to use!" -ForegroundColor Green
        Write-Host "SecureView will automatically load it when you run the application." -ForegroundColor Gray
    } else {
        Write-Host "[WARNING] No .onnx model files found" -ForegroundColor Yellow
        Write-Host "Please place a model file in: $modelsDir" -ForegroundColor Yellow
    }
} else {
    Write-Host "[ERROR] Models directory not found" -ForegroundColor Red
}

