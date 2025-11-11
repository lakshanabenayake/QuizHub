#!/bin/bash
# Compilation script for QuizHub application (Linux/Mac)

echo "========================================"
echo "Building QuizHub Application"
echo "========================================"

# Create bin directory if it doesn't exist
mkdir -p bin

echo ""
echo "Compiling source files..."

# Compile all Java files
javac -d bin -sourcepath src \
    src/common/*.java \
    src/model/*.java \
    src/server/*.java \
    src/client/*.java

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Build successful!"
    echo "========================================"
    echo ""
    echo "To run the server:"
    echo "  java -cp bin server.QuizServer"
    echo ""
    echo "To run the client:"
    echo "  java -cp bin client.QuizClient"
    echo "========================================"
else
    echo ""
    echo "========================================"
    echo "Build failed! Please check the errors above."
    echo "========================================"
fi

