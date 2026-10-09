@echo off
setlocal enabledelayedexpansion
title AI ResearchHub Server

echo ===============================================================
echo     Starting AI ResearchHub Web Application Platform
echo ===============================================================

rem Detect Java Runtime
set JAVA_CMD=java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Users\Asus\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260826-1216\jre\bin\java.exe" (
        set "JAVA_CMD=C:\Users\Asus\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260826-1216\jre\bin\java.exe"
    ) else (
        echo [ERROR] No Java runtime (java.exe) found in PATH or Eclipse directories!
        pause
        exit /b 1
    )
)

rem Check if compiled
if not exist "bin\com\airesearchhub\Main.class" (
    echo Binaries not found. Running compilation first...
    call build.bat
)

echo.
echo Launching AI ResearchHub on port 8080...
echo Opening browser at http://localhost:8080 ...
start "" "http://localhost:8080"

"%JAVA_CMD%" -cp "bin;lib/*" "com.airesearchhub.Main"

pause
