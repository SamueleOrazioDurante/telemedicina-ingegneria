#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status
set -e

# Define color codes for pretty output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
RESET='\033[0m'

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed.${RESET}" >&2
    echo "Please install JDK 17 or higher to run this application." >&2
    exit 1
fi

# Function to get Maven command (checks local mvnw first, then global mvn)
get_maven() {
    if [ -f "./mvnw" ]; then
        echo "./mvnw"
    elif command -v mvn &> /dev/null; then
        echo "mvn"
    else
        echo -e "${RED}Error: Neither 'mvn' nor './mvnw' was found.${RESET}" >&2
        echo "Please install Maven or ensure 'mvnw' exists in the project root." >&2
        exit 1
    fi
}

show_help() {
    echo -e "${BLUE}Telemedicine System Build & Run Utility${RESET}"
    echo "Usage: ./run.sh [command]"
    echo ""
    echo "Commands:"
    echo "  dev       Run the application in development mode (using javafx:run)"
    echo "  package   Compile and package the application into a standalone fat JAR"
    echo "  prod      Run the compiled standalone JAR (requires 'package' to be run first)"
    echo "  test      Run the automated tests"
    echo "  clean     Clean build artifacts"
    echo "  help      Show this help message"
}

# Check argument
CMD=${1:-"help"}

case "$CMD" in
    dev)
        MVN=$(get_maven)
        echo -e "${GREEN}Running application in development mode...${RESET}"
        $MVN javafx:run
        ;;
    package)
        MVN=$(get_maven)
        echo -e "${GREEN}Packaging application for production...${RESET}"
        $MVN clean package
        echo -e "${GREEN}Success! Standalone JAR created at: target/telemedicina-ingegneria-1.0-SNAPSHOT.jar${RESET}"
        ;;
    prod)
        JAR_PATH="target/telemedicina-ingegneria-1.0-SNAPSHOT.jar"
        if [ ! -f "$JAR_PATH" ]; then
            echo -e "${YELLOW}Standalone JAR not found. Packaging the application first...${RESET}" >&2
            MVN=$(get_maven)
            $MVN clean package
        fi
        echo -e "${GREEN}Running application in production mode (standalone JAR)...${RESET}"
        java -jar "$JAR_PATH"
        ;;
    test)
        MVN=$(get_maven)
        echo -e "${GREEN}Running JUnit 5 tests...${RESET}"
        $MVN test
        ;;
    clean)
        MVN=$(get_maven)
        echo -e "${GREEN}Cleaning build artifacts...${RESET}"
        $MVN clean
        ;;
    help|*)
        show_help
        ;;
esac
