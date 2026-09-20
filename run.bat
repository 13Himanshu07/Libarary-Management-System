@echo off
cd /d "%~dp0"

echo Compiling...

if not exist bin mkdir bin

javac -d bin -cp "lib\*" -sourcepath src src\com\library\Main.java

if errorlevel 1 (
    echo.
    echo Build failed - see errors above.
    pause
    exit /b 1
)

echo.
echo Build successful!
echo Starting Library Management System...

java -cp "bin;lib\*" com.library.Main

pause
