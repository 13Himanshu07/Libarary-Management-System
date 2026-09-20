#!/bin/bash
cd "$(dirname "$0")"

echo "Compiling..."
mkdir -p bin
javac -d bin -cp "lib/*" -sourcepath src src/com/library/Main.java

if [ $? -ne 0 ]; then
    echo ""
    echo "Build failed - see errors above."
    read -p "Press enter to exit"
    exit 1
fi

echo ""
echo "Build successful!"
echo "Starting Library Management System..."
java -cp "bin:lib/*" com.library.Main
