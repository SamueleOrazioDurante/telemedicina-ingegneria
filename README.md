# Telemedicine System for Diabetic Patients

A Java-based desktop application for monitoring and managing diabetic patients, facilitating communication between patients and medical professionals.

## Table of Contents

1. [Project Description](#project-description)
2. [System Architecture](#system-architecture)
3. [Required Dependencies](#required-dependencies)
4. [Compilation and Execution](#compilation-and-execution)
5. [Team & Division of Tasks](#team--division-of-tasks)
6. [Testing Strategy](#testing-strategy)
7. [Project Documentation](#project-documentation)
8. [Submission Guidelines](#submission-guidelines)

---

## Project Description

The **Telemedicine System for Diabetic Patients** is a desktop application designed to bridge the gap between diabetic patients and healthcare professionals. The system allows patients to record their daily blood glucose measurements and receive therapy updates, while enabling doctors to monitor patient data, manage therapies, and receive alerts in case of abnormal blood sugar levels or missing measurements.

The application is built using:

- **Language**: Java
- **Graphical User Interface (GUI)**: JavaFX
- **Database**: SQLite

For detailed functional specifications, please refer to the [Functional Specifications Document (PDF)](./specifications.pdf).

---

## How to compile and run

This project provides utility scripts (`run.sh` for Linux/macOS and `run.bat` for Windows) to simplify compilation and execution in both development and production environments.

### Prerequisites

- **Java Development Kit (JDK) 17** or higher installed on your system.
- **Maven** installed on your system (or `mvnw` / `mvnw.cmd` present in the root directory).

### On Linux / macOS

Make the script executable:

```bash
chmod +x run.sh
```

- **Development (quick launch):**
  ```bash
  ./run.sh dev
  ```
- **Compile for Production (builds a standalone executable fat JAR):**
  ```bash
  ./run.sh package
  ```
- **Run in Production (starts the pre-compiled standalone JAR):**
  ```bash
  ./run.sh prod
  ```
- **Run Tests (JUnit 5):**
  ```bash
  ./run.sh test
  ```
- **Clean Build Artifacts (`target/`):**
  ```bash
  ./run.sh clean
  ```

### On Windows

- **Development:**
  ```cmd
  run.bat dev
  ```
- **Compile for Production:**
  ```cmd
  run.bat package
  ```
- **Run in Production:**
  ```cmd
  run.bat prod
  ```
- **Run Tests:**
  ```cmd
  run.bat test
  ```
- **Clean Build Artifacts:**
  ```cmd
  run.bat clean
  ```

---

## System Architecture

The application follows a **Layered Architecture** style, ensuring a clear separation of concerns. It is divided into three main layers:

1. **Presentation Layer (GUI)**: Implemented using JavaFX. It manages the user interface, views (FXML), and controllers, handling user interactions and displaying alerts or patient dashboards.
2. **Business Logic Layer (Domain & Logic)**: Contains the core domain models (e.g., `Patient`, `Doctor`, `Measurement`, `Therapy`) and implements medical rules. These rules include checking glycemic thresholds, raising alarms if a patient has not logged data for 3 consecutive days, and managing therapies. It also implements software Design Patterns (such as the _Observer Pattern_ for medical alert notifications).
3. **Data Access Layer (Persistence)**: Handles the persistence of information in an SQLite database. It encapsulates SQL queries using the Data Access Object (DAO) pattern to perform CRUD operations.

---

## Required Dependencies

Project dependencies are managed entirely by Maven via the `pom.xml` file. The main libraries used are:

1. **JavaFX (v17.0.10)**:
   - `javafx-controls`: For the graphical user interface.
   - `javafx-fxml`: To load views defined in FXML.
2. **SQLite JDBC (v3.45.1.0)**:
   - Database driver for connecting to the local SQLite database.
3. **JUnit 5 (v5.10.2)**:
   - `junit-jupiter-api` & `junit-jupiter-engine`: For automated unit and integration tests.

All dependencies are automatically downloaded during the first run or during the build phase (`./run.sh dev` or `./run.sh package`).

---

## Team & Division of Tasks

The development team consists of three members, each responsible for specific parts of the codebase, documentation, and testing.

### 1. Tommaso Pasquin (VR518989): Database Administrator (DBA) & Persistence

- **Code**: Creates and initializes the SQLite database. Implements the Data Access Layer (DAO Java classes responsible for executing SQL queries to save and retrieve data).
- **Documentation**: Designs the Entity-Relationship (ER) Schema, writes the database description, and creates the Conceptual Class Diagram.
- **Testing**: Writes Data Consistency Tests to verify database operations and constraints.

### 2. Samuele Orazio Durante (VR519359) : System Architect (Logic & Rules)

- **Code**: Writes the Business Logic Layer. Implements domain classes (`Patient`, `Doctor`) and programs critical medical rules (e.g., blood glucose thresholds, missing data alerts for 3 consecutive days, therapy validation). Implements relevant Design Patterns (such as Observer for alerts).
- **Documentation**: Creates the Software Class Diagram, designs Sequence Diagrams, and writes the technical section explaining the layered architecture and Design Pattern choices.
- **Testing**: Writes Unit Tests using JUnit 5 to validate algorithms and business rules in isolation.

### 3. Dominic Centrone (VR516778): Front-End Developer (Interface & Requirements)

- **Code**: Develops the graphical user interface using JavaFX (forms, tables, visual alerts) and binds the UI components to the Business Logic and Presentation controllers.
- **Documentation**: Identifies system requirements, creates Use Case Diagrams, writes Use Case Specification sheets, and designs Activity Diagrams.
- **Testing**: Handles System Tests (black-box) by simulating end-to-end user workflows on the complete application.

---

## Testing Strategy

The project leverages **JUnit 5** to automate code verification across three distinct testing levels:

1. **Unit Tests (White-Box)**:
   - Written by the Developer/Architect to test individual Business Logic classes in isolation.
   - Example: Verifying that a `checkAlarm()` method returns `true` if the blood glucose value exceeds `180 mg/dL`.
2. **System Tests (Black-Box)**:
   - End-to-end tests validating that the complete application meets all requirements.
   - Example: Simulating a user logging in, entering measurement data, and verifying the correct generation of notifications.
3. **Data Consistency Tests**:
   - Verifies SQLite CRUD operations to guarantee database integrity, foreign key constraints, and transactional consistency.

---

## Project Documentation

All documentation accompanying the source code follows a top-down engineering approach:

- **Requirements Analysis**: Use Case Diagrams, text Use Case Specifications, and Conceptual Class Diagrams.
- **Design & Architecture**: Activity Diagrams, detailed Software Class Diagrams, and Sequence Diagrams.
- **Database Architecture**: ER Schema and SQLite database structure description.
- **Design Decisions**: Layered architecture discussion, Design Patterns breakdown, and testing details.

### Documentation Links

- [Technical Documentation (Markdown File)](./documentation.md)
- [Technical Documentation (PDF File)](./documentation.pdf)
