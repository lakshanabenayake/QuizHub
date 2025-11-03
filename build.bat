@echo off
REM Compilation script for QuizHub application (Windows)

echo ========================================
echo Building QuizHub Application
echo ========================================

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

echo.
echo Compiling source files...

REM Compile all Java files
javac -d bin -sourcepath src ^
    src\common\*.java ^
    src\model\*.java ^
    src\server\*.java ^
    src\client\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build successful!
    echo ========================================
    echo.
    echo To run the server:
    echo   java -cp bin server.QuizServer
    echo.
    echo To run the client:
    echo   java -cp bin client.QuizClient
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Build failed! Please check the errors above.
    echo ========================================
)

pause

