# PowerShell script to run QuizHub Server
Write-Host "Starting QuizHub Server..." -ForegroundColor Green
Write-Host "Server will start on port 8888" -ForegroundColor Cyan
Write-Host ""

# Check if bin directory exists
if (-Not (Test-Path "bin")) {
    Write-Host "ERROR: bin directory not found. Please run build.bat first!" -ForegroundColor Red
    pause
    exit
}

# Run the server
java -cp bin server.QuizServer

Write-Host ""
Write-Host "Server stopped." -ForegroundColor Yellow
pause

