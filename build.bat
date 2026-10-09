@echo off
setlocal enabledelayedexpansion
title AI ResearchHub - Build Script

echo ===============================================================
echo     AI ResearchHub - Compiling Java Enterprise Web Application
echo ===============================================================

rem Detect Java Compiler
set JAVAC_CMD=javac
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Users\Asus\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260826-1216\jre\bin\javac.exe" (
        set "JAVAC_CMD=C:\Users\Asus\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260826-1216\jre\bin\javac.exe"
    ) else (
        echo [ERROR] No Java Compiler (javac) found in PATH or Eclipse directories!
        pause
        exit /b 1
    )
)

if not exist "bin" mkdir "bin"
if not exist "data" mkdir "data"
if not exist "uploads" mkdir "uploads"

echo Collecting Java source files...
dir /s /b src\main\java\*.java > sources.txt

echo Compiling Java classes with dependencies in lib\* ...
"%JAVAC_CMD%" -cp "lib/*" -d "bin" "@sources.txt"

if %ERRORLEVEL% equ 0 (
    echo.
    echo ===============================================================
    echo   [SUCCESS] Build completed successfully!
    echo   Classes output to: bin\
    echo ===============================================================
) else (
    echo.
    echo ===============================================================
    echo   [ERROR] Compilation failed!
    echo ===============================================================
    pause
    exit /b 1
)

del sources.txt >nul 2>&1
pause
