@echo off
setlocal enabledelayedexpansion

:: Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed.
    echo Please install JDK 17 or higher to run this application.
    exit /b 1
)

:: Find Maven
set "MVN="
if exist "mvnw.cmd" (
    set "MVN=mvnw.cmd"
) else (
    where mvn >nul 2>&1
    if %errorlevel% eq 0 (
        set "MVN=mvn"
    ) else (
        echo Error: Neither 'mvn' nor 'mvnw.cmd' was found.
        echo Please install Maven or ensure 'mvnw.cmd' exists in the project root.
        exit /b 1
    )
)

:: Parse arguments
set "CMD=%~1"
if "%CMD%"=="" set "CMD=help"

if "%CMD%"=="dev" (
    echo Running application in development mode...
    call %MVN% javafx:run
    goto :eof
)

if "%CMD%"=="package" (
    echo Packaging application for production...
    call %MVN% clean package
    echo Success! Standalone JAR created at: target\telemedicina-ingegneria-1.0-SNAPSHOT.jar
    goto :eof
)

if "%CMD%"=="prod" (
    set "JAR_PATH=target\telemedicina-ingegneria-1.0-SNAPSHOT.jar"
    if not exist "!JAR_PATH!" (
        echo Standalone JAR not found. Packaging the application first...
        call %MVN% clean package
    )
    echo Running application in production mode (standalone JAR)...
    java -jar "!JAR_PATH!"
    goto :eof
)

if "%CMD%"=="test" (
    echo Running JUnit 5 tests...
    call %MVN% test
    goto :eof
)

if "%CMD%"=="clean" (
    echo Cleaning build artifacts...
    call %MVN% clean
    goto :eof
)

:help
echo Telemedicine System Build ^& Run Utility
echo Usage: run.bat [command]
echo.
echo Commands:
echo   dev       Run the application in development mode (using javafx:run)
echo   package   Compile and package the application into a standalone fat JAR
echo   prod      Run the compiled standalone JAR (requires 'package' to be run first)
echo   test      Run the automated tests
echo   clean     Clean build artifacts
echo   help      Show this help message