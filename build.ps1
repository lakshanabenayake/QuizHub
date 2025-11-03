# PowerShell build script for QuizHub application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Building QuizHub Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create bin directory if it doesn't exist
if (-Not (Test-Path "bin")) {
    Write-Host "Creating bin directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path bin | Out-Null
}

Write-Host "Compiling source files..." -ForegroundColor Yellow
Write-Host ""

# Compile all Java files
javac -d bin -sourcepath src `
    src\common\*.java `
    src\model\*.java `
    src\server\*.java `
    src\client\*.java

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Build successful!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "To run the server:" -ForegroundColor Cyan
    Write-Host "  .\run-server.ps1  (or)  java -cp bin server.QuizServer" -ForegroundColor White
    Write-Host ""
    Write-Host "To run the client:" -ForegroundColor Cyan
    Write-Host "  .\run-client.ps1  (or)  java -cp bin client.QuizClient" -ForegroundColor White
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
}

Write-Host ""
pause

