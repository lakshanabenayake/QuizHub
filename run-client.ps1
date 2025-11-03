# PowerShell script to run QuizHub Client
Write-Host "Starting QuizHub Client..." -ForegroundColor Green
Write-Host ""

# Check if bin directory exists
if (-Not (Test-Path "bin")) {
    Write-Host "ERROR: bin directory not found. Please run build.bat first!" -ForegroundColor Red
    pause
    exit
}

# Run the client
java -cp bin client.QuizClient

Write-Host ""
Write-Host "Client stopped." -ForegroundColor Yellow
pause

