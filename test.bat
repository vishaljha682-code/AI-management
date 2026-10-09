@echo off
setlocal enabledelayedexpansion
title AI ResearchHub - Test Runner

echo ===============================================================
echo     Executing Automated Platform & Rubric Test Suite
echo ===============================================================

rem Detect Java Runtime
set JAVA_CMD=java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Users\Asus\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260826-1216\jre\bin\java.exe" (
        set "JAVA_CMD=C:\Users\Asus\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260826-1216\jre\bin\java.exe"
    ) else (
        echo [ERROR] No Java runtime (java.exe) found!
        pause
        exit /b 1
    )
)

"%JAVA_CMD%" -cp "bin;lib/*" "com.airesearchhub.PlatformTestSuite"

pause
