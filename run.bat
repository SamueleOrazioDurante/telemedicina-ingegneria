@echo off

:: Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed.
    echo Please install JDK 17 or higher to run this application.
    exit /b 1
)

:: Find Maven
set "MVN=mvn"
if exist "mvnw.cmd" set "MVN=mvnw.cmd"
if exist "tools\apache-maven-3.9.6\bin\mvn.cmd" set "MVN=tools\apache-maven-3.9.6\bin\mvn.cmd"

:: Parse arguments
set "CMD=%~1"
if "%CMD%"=="" set "CMD=help"

if "%CMD%"=="dev" goto :dev
if "%CMD%"=="package" goto :package
if "%CMD%"=="prod" goto :prod
if "%CMD%"=="test" goto :test
if "%CMD%"=="clean" goto :clean
goto :help

:dev
echo Running application in development mode...
call %MVN% javafx:run
goto :eof

:package
echo Packaging application for production...
call %MVN% clean package
echo Success! Standalone JAR created at: target\telemedicina-ingegneria.jar
goto :eof

:prod
set "JAR_PATH=target\telemedicina-ingegneria.jar"
if not exist "%JAR_PATH%" (
    echo Standalone JAR not found. Packaging the application first...
    call %MVN% clean package
)
echo Running application in production mode (standalone JAR)...
java -jar "%JAR_PATH%"
goto :eof

:test
echo Running JUnit 5 tests...
call %MVN% test
goto :eof

:clean
echo Cleaning build artifacts...
call %MVN% clean
goto :eof

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

