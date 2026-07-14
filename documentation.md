# Technical Database Documentation & Data Model

This documentation describes the data architecture, conceptual model, and SQLite database schema for the **Telemedicine System for Diabetic Patients**.

---

## 1. Conceptual Class Diagram

The conceptual class diagram models the application domain entities and their associations, independent of the persistence technology.

```mermaid
classDiagram
    class Doctor {
        +Integer id
        +String taxCode
        +String firstName
        +String lastName
        +String email
        +String username
        +String password
    }
    class Patient {
        +Integer id
        +String taxCode
        +String firstName
        +String lastName
        +String dateOfBirth
        +String username
        +String password
        +String riskFactors
        +String pastPathologies
        +String comorbidities
    }
    class BloodGlucoseMeasurement {
        +Integer id
        +double value
        +String timeSlot
        +String date
        +String time
    }
    class PrescribedTherapy {
        +Integer id
        +String drugName
        +int dailyIntakes
        +String quantityPerIntake
        +String directions
        +String startDate
        +String endDate
        +boolean active
    }
    class DrugIntake {
        +Integer id
        +String date
        +String time
        +String drugName
        +String quantityTaken
    }
    class ConcomitantCondition {
        +Integer id
        +String type
        +String description
        +String startDate
        +String endDate
    }
    class OperationLog {
        +Integer id
        +String operation
        +String timestamp
    }

    Doctor "1" --> "*" Patient : treats (reference doctor)
    Doctor "1" --> "*" PrescribedTherapy : prescribes
    Doctor "1" --> "*" OperationLog : performs
    Patient "1" --> "*" BloodGlucoseMeasurement : records
    Patient "1" --> "*" PrescribedTherapy : follows
    Patient "1" --> "*" DrugIntake : reports-intake
    Patient "1" --> "*" ConcomitantCondition : reports-condition
    Patient "1" --> "?" OperationLog : is-subject-of
    PrescribedTherapy "1" --> "*" DrugIntake : tracks-compliance
```

### Description of Conceptual Classes

- **Doctor**: Represents the authenticated diabetologist who monitors patients, configures therapies, and updates medical histories.
- **Patient**: Represents the monitored diabetic patient who registers daily blood glucose readings and drug intakes.
- **BloodGlucoseMeasurement**: Represents a single blood glucose reading recorded by a patient.
- **PrescribedTherapy**: Represents a drug prescription schema issued by a doctor for a specific patient.
- **DrugIntake**: Represents a patient's self-reported log indicating they took a dose of a prescribed drug.
- **ConcomitantCondition**: Represents symptoms, other concurrent pathologies, or other non-prescribed therapies followed by the patient.
- **OperationLog**: Audit log capturing sensitive actions performed by doctors on patient records (e.g. changing therapies, updating risk factors).

---

## 2. Entity-Relationship (ER) Schema

The ER Schema defines the logical relational database structure implemented within the SQLite database.

```mermaid
erDiagram
    DOCTOR {
        INTEGER id PK
        TEXT tax_code UK
        TEXT first_name
        TEXT last_name
        TEXT email
        TEXT username UK
        TEXT password
    }
    PATIENT {
        INTEGER id PK
        TEXT tax_code UK
        TEXT first_name
        TEXT last_name
        TEXT date_of_birth
        TEXT username UK
        TEXT password
        INTEGER reference_doctor_id FK
        TEXT risk_factors
        TEXT past_pathologies
        TEXT comorbidities
    }
    BLOOD_GLUCOSE_MEASUREMENT {
        INTEGER id PK
        INTEGER patient_id FK
        REAL value
        TEXT time_slot
        TEXT date
        TEXT time
    }
    PRESCRIBED_THERAPY {
        INTEGER id PK
        INTEGER patient_id FK
        INTEGER doctor_id FK
        TEXT drug_name
        INTEGER daily_intakes
        TEXT quantity_per_intake
        TEXT directions
        TEXT start_date
        TEXT end_date
        INTEGER is_active
    }
    DRUG_INTAKE {
        INTEGER id PK
        INTEGER patient_id FK
        INTEGER therapy_id FK
        TEXT date
        TEXT time
        TEXT drug_name
        TEXT quantity_taken
    }
    CONCOMITANT_CONDITION {
        INTEGER id PK
        INTEGER patient_id FK
        TEXT type
        TEXT description
        TEXT start_date
        TEXT end_date
    }
    OPERATION_LOG {
        INTEGER id PK
        INTEGER doctor_id FK
        INTEGER patient_id FK
        TEXT operation
        TEXT timestamp
    }

    DOCTOR ||--o{ PATIENT : "reference doctor"
    DOCTOR ||--o{ PRESCRIBED_THERAPY : "prescribes"
    DOCTOR ||--o{ OPERATION_LOG : "performs"
    PATIENT ||--o{ BLOOD_GLUCOSE_MEASUREMENT : "records"
    PATIENT ||--o{ PRESCRIBED_THERAPY : "has"
    PATIENT ||--o{ DRUG_INTAKE : "performs"
    PATIENT ||--o{ CONCOMITANT_CONDITION : "reports"
    PATIENT |o--o{ OPERATION_LOG : "concerns"
    PRESCRIBED_THERAPY ||--o{ DRUG_INTAKE : "generates"
```

### Relational Rules and Referential Integrity Constraints

1. **Doctor - Patient (1:N)**: A patient is assigned to a reference doctor. If an administrator attempts to delete a doctor who is currently assigned to patients, the action is blocked (`ON DELETE RESTRICT`) to prevent orphan patients.
2. **Patient - BloodGlucoseMeasurement (1:N)**: Readings belong to a specific patient. If the patient record is deleted, all associated measurements are deleted recursively (`ON DELETE CASCADE`).
3. **Patient - PrescribedTherapy (1:N)** and **Doctor - PrescribedTherapy (1:N)**: A therapy belongs to a patient and is linked to the prescribing doctor. Deleting the patient removes the therapies (`ON DELETE CASCADE`). Deleting the doctor is blocked (`ON DELETE RESTRICT`) to preserve historical prescription records.
4. **PrescribedTherapy - DrugIntake (1:N)**: Each logged intake references the active prescription schema. If a therapy is deleted, its compliance data is cascaded (`ON DELETE CASCADE`).

---

## 3. Detailed Database Tables Description

All data types are mapped to SQLite native storage classes (`INTEGER`, `REAL`, `TEXT`). Dates and times are stored as `TEXT` in standard ISO-8601 formatting to allow correct sorting and range checks.

### Table: `doctor`

Stores credentials and demographic information of diabetologists.

- **id**: `INTEGER` (PRIMARY KEY, AUTOINCREMENT). Unique doctor identifier.
- **tax_code**: `TEXT` (UNIQUE, NOT NULL). Codice Fiscale / Tax code.
- **first_name**: `TEXT` (NOT NULL). First name.
- **last_name**: `TEXT` (NOT NULL). Last name.
- **email**: `TEXT` (UNIQUE, NOT NULL). Doctor's email address.
- **username**: `TEXT` (UNIQUE, NOT NULL). Login credential.
- **password**: `TEXT` (NOT NULL). Encrypted login password.

### Table: `patient`

Stores patient profiles, demographics, and clinical history text blocks.

- **id**: `INTEGER` (PRIMARY KEY, AUTOINCREMENT). Unique patient identifier.
- **tax_code**: `TEXT` (UNIQUE, NOT NULL). Patient's Tax code.
- **first_name**: `TEXT` (NOT NULL). First name.
- **last_name**: `TEXT` (NOT NULL). Last name.
- **date_of_birth**: `TEXT` (NOT NULL). Date of birth formatted as `YYYY-MM-DD`.
- **username**: `TEXT` (UNIQUE, NOT NULL). Patient login credential.
- **password**: `TEXT` (NOT NULL). Login password.
- **reference_doctor_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Link to the doctor in charge.
- **risk_factors**: `TEXT` (NULL). Text notes on risk factors (e.g. "smoker, obesity").
- **past_pathologies**: `TEXT` (NULL). Note details on historical conditions.
- **comorbidities**: `TEXT` (NULL). concurrent conditions (e.g. "hypertension").

### Table: `blood_glucose_measurement`

Stores patient blood glucose logs.

- **id**: `INTEGER` (PRIMARY KEY, AUTOINCREMENT). Log identifier.
- **patient_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Reference to patient.
- **value**: `REAL` (NOT NULL). Glucose reading value (in mg/dL).
- **time_slot**: `TEXT` (NOT NULL). Categorization (must be `'BEFORE_MEAL'` or `'AFTER_MEAL'`).
- **date**: `TEXT` (NOT NULL). Date formatted as `YYYY-MM-DD`.
- **time**: `TEXT` (NOT NULL). Time formatted as `HH:MM`.

### Table: `prescribed_therapy`

Contains active and historical drug prescriptions.

- **id**: `INTEGER` (PRIMARY KEY, AUTOINCREMENT). Prescription identifier.
- **patient_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Reference to patient.
- **doctor_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Reference to doctor who prescribed it.
- **drug_name**: `TEXT` (NOT NULL). Name of drug (e.g. "Metformin", "Rapid Insulin").
- **daily_intakes**: `INTEGER` (NOT NULL). Posology frequency per day.
- **quantity_per_intake**: `TEXT` (NOT NULL). Dose description (e.g. "500mg", "1 pill").
- **directions**: `TEXT` (NULL). Notes (e.g., "after meals", "on empty stomach").
- **start_date**: `TEXT` (NOT NULL). Validity start date (`YYYY-MM-DD`).
- **end_date**: `TEXT` (NULL). Validity end date (`YYYY-MM-DD`). NULL if ongoing.
- **is_active**: `INTEGER` (NOT NULL, DEFAULT 1). Boolean flag (1 = Active, 0 = Stopped/Replaced).

### Table: `drug_intake`

Tracks actual compliance entries reported by patients.

- **id**: `INTEGER` (PRIMARY KEY, AUTOINCREMENT). Entry identifier.
- **patient_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Reference to patient.
- **therapy_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Reference to prescription.
- **date**: `TEXT` (NOT NULL). Log date (`YYYY-MM-DD`).
- **time**: `TEXT` (NOT NULL). Log time (`HH:MM`).
- **drug_name**: `TEXT` (NOT NULL). Logged drug name.
- **quantity_taken**: `TEXT` (NOT NULL). Logged dose.

### Table: `concomitant_condition`

Tracks user-reported symptoms, temporary illnesses, or concurrent therapies.

- **id**: `INTEGER` (PRIMARY KEY, AUTOINCREMENT). Condition identifier.
- **patient_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Reference to patient.
- **type**: `TEXT` (NOT NULL). Category (`'SYMPTOM'`, `'PATHOLOGY'`, or `'CONCOMITANT_THERAPY'`).
- **description**: `TEXT` (NOT NULL). Free description (e.g. "nausea", "headache").
- **start_date**: `TEXT` (NOT NULL). Period start date (`YYYY-MM-DD`).
- **end_date**: `TEXT` (NULL). Period end date (`YYYY-MM-DD`). NULL if ongoing.

### Table: `operation_log`

Audit trail of diabetologist actions for system security.

- **id**: `INTEGER` (PRIMARY KEY, AUTOINCREMENT). Log identifier.
- **doctor_id**: `INTEGER` (NOT NULL, FOREIGN KEY). Reference to doctor.
- **patient_id**: `INTEGER` (NULL, FOREIGN KEY). Target patient or NULL if general. Nullified on patient delete (`ON DELETE SET NULL`).
- **operation**: `TEXT` (NOT NULL). Description of the action (e.g. "Modified therapy: Metformin", "Updated comorbidities").
- **timestamp**: `TEXT` (NOT NULL). Action timestamp (`YYYY-MM-DD HH:MM:SS`).

---

## 4. Software Class Diagram (Business Logic)

The following class diagram represents the design of the Domain and Business Logic layers of the application, incorporating the Observer Design Pattern for medical alerts.

```mermaid
classDiagram
    class AlertObserver {
        <<interface>>
        +onAlert(String alertMessage)
    }

    class AlertSubject {
        <<interface>>
        +registerObserver(AlertObserver observer)
        +removeObserver(AlertObserver observer)
        +notifyObservers(String alertMessage)
    }

    class MedicalRulesEngine {
        -List~AlertObserver~ observers
        +registerObserver(AlertObserver observer)
        +removeObserver(AlertObserver observer)
        +notifyObservers(String alertMessage)
        +checkGlucoseThreshold(BloodGlucoseMeasurement measurement) boolean
        +checkMissingTherapy(List~DrugIntake~ intakes, PrescribedTherapy therapy, LocalDate today) boolean
    }

    AlertSubject <|.. MedicalRulesEngine : implements
    MedicalRulesEngine o--> AlertObserver : notifies
```

## 5. Sequence Diagram

The following sequence diagram illustrates the flow when a patient inserts a new blood glucose reading and the rules engine evaluates it, eventually notifying the doctor.

```mermaid
sequenceDiagram
    actor Patient
    participant GUI as Presentation Layer
    participant Engine as MedicalRulesEngine
    participant DB as Persistence Layer (DAO)
    actor Doctor as AlertObserver (Doctor UI)

    Patient->>GUI: Inserts Glucose Measurement
    GUI->>DB: saveMeasurement(measurement)
    DB-->>GUI: success
    GUI->>Engine: checkGlucoseThreshold(measurement)
    alt Value exceeds threshold
        Engine->>Doctor: onAlert("Abnormal glucose level detected")
    end
    Engine-->>GUI: threshold result
    GUI-->>Patient: Display confirmation
```

## 6. Architecture and Design Patterns

### Layered Architecture

The application adopts a **Layered Architecture** strictly separating Presentation, Business Logic, and Persistence:

- **Presentation Layer (JavaFX)**: Only handles FXML views and controllers. It delegates all decision-making to the logic layer and formatting to domain objects.
- **Business Logic Layer (`it.univr.telemedicina.logic`)**: Centralizes the medical rules (`MedicalRulesEngine`). It does not contain GUI code or SQL queries.
- **Persistence Layer (`it.univr.telemedicina.persistence`)**: Manages SQLite connections and executes CRUD operations via DAOs.

### Design Patterns

1. **Observer Pattern**: Used to decouple the component that verifies medical rules (`MedicalRulesEngine` as the Subject) from the components that must react to abnormalities (e.g. Doctors' dashboards or push notification services as Observers). This ensures that adding a new type of notification mechanism doesn't require modifying the core logic.
2. **Data Access Object (DAO) Pattern**: Encapsulates all access to the SQLite database. The Logic layer depends on domain objects (like `Patient`) and interacts with DAOs, completely oblivious to the underlying SQL dialect.

---

## 7. Requirements Analysis

### 7.1 Functional Requirements

| ID    | Requirement                                                                                                                                   | Actor           | Priority |
| ----- | --------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | -------- |
| FR-01 | The system shall authenticate users (patients and doctors) via username and password.                                                         | Patient, Doctor | High     |
| FR-02 | Patients shall record daily blood glucose measurements, specifying value, time slot (before/after meal), date, and time.                      | Patient         | High     |
| FR-03 | The system shall alert patients when glucose levels exceed thresholds (>130 mg/dL before meal, >180 mg/dL after meal, <80 mg/dL before meal). | System          | High     |
| FR-04 | Patients shall record drug intakes linked to active prescribed therapies, specifying drug, quantity, date, and time.                          | Patient         | High     |
| FR-05 | Patients shall report concomitant conditions (symptoms, pathologies, concurrent therapies) with a description and time period.                | Patient         | Medium   |
| FR-06 | Doctors shall prescribe therapies specifying drug name, daily intakes, quantity per intake, directions, and dates.                            | Doctor          | High     |
| FR-07 | Doctors shall view patient data (glucose measurements, therapies, conditions) including synthetic summaries (weekly/monthly averages).        | Doctor          | High     |
| FR-08 | Doctors shall update patient medical information (risk factors, past pathologies, comorbidities), with audit logging.                         | Doctor          | Medium   |
| FR-09 | The system shall alert doctors when patients miss prescribed therapy intakes for 3+ consecutive days.                                         | System          | High     |
| FR-10 | Patients shall be able to email their reference doctor via system integration with the native mail client.                                    | Patient         | Low      |
| FR-11 | Doctors shall be able to view all audit logs of their own database operations.                                                                | Doctor          | Medium   |

---

## 8. Use Case Diagram

```mermaid
flowchart LR
    %% Style Definitions
    classDef actor fill:#eff6ff,stroke:#3b82f6,stroke-width:2px,font-weight:bold,color:#1e3a8a;
    classDef usecase fill:#fffbeb,stroke:#f59e0b,stroke-width:2px,color:#78350f;

    %% Actors
    P["Patient"]:::actor
    D["Doctor"]:::actor
    S["System"]:::actor

    %% System Boundary
    subgraph TelemedicineSystem ["Telemedicine System"]
        UC1(["UC-01: Login"]):::usecase
        UC2(["UC-02: Record Glucose Measurement"]):::usecase
        UC3(["UC-03: View Glucose History"]):::usecase
        UC4(["UC-04: Record Drug Intake"]):::usecase
        UC5(["UC-05: Report Concomitant Condition"]):::usecase
        UC6(["UC-06: View Prescribed Therapies"]):::usecase
        UC7(["UC-07: Email Reference Doctor"]):::usecase
        UC8(["UC-08: Prescribe Therapy"]):::usecase
        UC9(["UC-09: View Patient Data"]):::usecase
        UC10(["UC-10: Update Patient Medical Info"]):::usecase
        UC11(["UC-11: View Glucose Trend"]):::usecase
        UC12(["UC-12: Generate Glucose Alert"]):::usecase
        UC13(["UC-13: Generate Missing Therapy Alert"]):::usecase
        UC14(["UC-14: View Doctor Audit Logs"]):::usecase
    end

    %% Actor to Use Case Associations (Standard solid lines)
    P --- UC1
    P --- UC2
    P --- UC3
    P --- UC4
    P --- UC5
    P --- UC6
    P --- UC7

    D --- UC1
    D --- UC8
    D --- UC9
    D --- UC10
    D --- UC11
    D --- UC14

    %% Use Case to Use Case/System Relationships
    UC2 -.->|"<<include>>"| UC12
    UC12 --- S
    UC13 --- S
    UC4 -.->|"<<include>>"| UC13

    %% Styling Subgraph
    style TelemedicineSystem fill:#f8fafc,stroke:#94a3b8,stroke-width:2px,color:#0f172a,stroke-dasharray: 5 5;
```

---

## 9. Use Case Specification Sheets

### UC-01: Login

| Field                | Description                                                                                                                                                                                             |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**               | UC-01                                                                                                                                                                                                   |
| **Name**             | Login                                                                                                                                                                                                   |
| **Actor(s)**         | Patient, Doctor                                                                                                                                                                                         |
| **Precondition**     | User has valid credentials created by the system administrator.                                                                                                                                         |
| **Main Flow**        | 1. User navigates to the login screen. 2. User enters username and password. 3. System verifies credentials against the database. 4. System redirects to the appropriate dashboard (patient or doctor). |
| **Alternative Flow** | 3a. Credentials are invalid → System displays an error message and remains on the login screen.                                                                                                         |
| **Postcondition**    | User is authenticated and has access to their role-specific dashboard.                                                                                                                                  |

### UC-02: Record Glucose Measurement

| Field                | Description                                                                                                                                                                                                                                                                                                                        |
| -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**               | UC-02                                                                                                                                                                                                                                                                                                                              |
| **Name**             | Record Glucose Measurement                                                                                                                                                                                                                                                                                                         |
| **Actor(s)**         | Patient                                                                                                                                                                                                                                                                                                                            |
| **Precondition**     | Patient is authenticated and on the glucose entry screen.                                                                                                                                                                                                                                                                          |
| **Main Flow**        | 1. Patient enters glucose value (mg/dL). 2. Patient selects time slot (before/after meal). 3. Patient selects date and enters time. 4. Patient clicks "Save". 5. System validates input. 6. System saves measurement to the database. 7. System checks glucose thresholds via MedicalRulesEngine. 8. System displays confirmation. |
| **Alternative Flow** | 5a. Input is invalid → error message shown. 7a. Value exceeds threshold → warning displayed to patient and alert sent to doctor.                                                                                                                                                                                                   |
| **Postcondition**    | Measurement is persisted. If abnormal, alert is generated.                                                                                                                                                                                                                                                                         |

### UC-03: View Glucose History

| Field             | Description                                                                                                                                                                                       |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**            | UC-03                                                                                                                                                                                             |
| **Name**          | View Glucose History                                                                                                                                                                              |
| **Actor(s)**      | Patient                                                                                                                                                                                           |
| **Precondition**  | Patient is authenticated.                                                                                                                                                                         |
| **Main Flow**     | 1. Patient navigates to glucose history. 2. System loads all measurements. 3. Patient optionally filters by date range. 4. System displays measurements in a table with status (Normal/Abnormal). |
| **Postcondition** | Patient can see all past glucose readings with visual status indicators.                                                                                                                          |

### UC-04: Record Drug Intake

| Field                | Description                                                                                                                                                                                                                                                  |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **ID**               | UC-04                                                                                                                                                                                                                                                        |
| **Name**             | Record Drug Intake                                                                                                                                                                                                                                           |
| **Actor(s)**         | Patient                                                                                                                                                                                                                                                      |
| **Precondition**     | Patient is authenticated and has at least one active therapy prescribed.                                                                                                                                                                                     |
| **Main Flow**        | 1. Patient selects an active therapy from the dropdown. 2. System auto-fills drug name and suggested quantity. 3. Patient confirms/adjusts quantity, date, and time. 4. Patient clicks "Save". 5. System saves intake to the database linked to the therapy. |
| **Alternative Flow** | 1a. No active therapies → message shown that no therapies are prescribed.                                                                                                                                                                                    |
| **Postcondition**    | Drug intake is recorded and linked to the prescribed therapy for compliance tracking.                                                                                                                                                                        |

### UC-05: Report Concomitant Condition

| Field             | Description                                                                                                                                                                                                                    |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **ID**            | UC-05                                                                                                                                                                                                                          |
| **Name**          | Report Concomitant Condition                                                                                                                                                                                                   |
| **Actor(s)**      | Patient                                                                                                                                                                                                                        |
| **Precondition**  | Patient is authenticated.                                                                                                                                                                                                      |
| **Main Flow**     | 1. Patient selects condition type (Symptom, Pathology, Concomitant Therapy). 2. Patient enters description. 3. Patient selects start date (and optional end date). 4. Patient clicks "Save". 5. System persists the condition. |
| **Postcondition** | Condition is saved and visible to the patient's doctor.                                                                                                                                                                        |

### UC-06: View Prescribed Therapies

| Field             | Description                                                                                                                                                                      |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**            | UC-06                                                                                                                                                                            |
| **Name**          | View Prescribed Therapies                                                                                                                                                        |
| **Actor(s)**      | Patient                                                                                                                                                                          |
| **Precondition**  | Patient is authenticated.                                                                                                                                                        |
| **Main Flow**     | 1. Patient navigates to therapies view. 2. System loads all therapies (active and stopped). 3. System displays them in a table with drug, dosage, directions, dates, and status. |
| **Postcondition** | Patient sees all current and past therapies.                                                                                                                                     |

### UC-08: Prescribe Therapy

| Field                | Description                                                                                                                                                                                                                                                                                                                   |
| -------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**               | UC-08                                                                                                                                                                                                                                                                                                                         |
| **Name**             | Prescribe Therapy                                                                                                                                                                                                                                                                                                             |
| **Actor(s)**         | Doctor                                                                                                                                                                                                                                                                                                                        |
| **Precondition**     | Doctor is authenticated.                                                                                                                                                                                                                                                                                                      |
| **Main Flow**        | 1. Doctor selects a patient from the dashboard. 2. Doctor clicks "Therapy" to open the prescription form. 3. Doctor enters drug name, daily intakes, quantity, directions, and start date. 4. Doctor clicks "Save Therapy". 5. System saves therapy to the database. 6. System creates an audit log entry in `operation_log`. |
| **Alternative Flow** | 4a. Required fields are missing → error shown.                                                                                                                                                                                                                                                                                |
| **Postcondition**    | New therapy is active and visible to the patient. Operation is logged.                                                                                                                                                                                                                                                        |

### UC-09: View Patient Data

| Field             | Description                                                                                                                                                                                                        |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **ID**            | UC-09                                                                                                                                                                                                              |
| **Name**          | View Patient Data                                                                                                                                                                                                  |
| **Actor(s)**      | Doctor                                                                                                                                                                                                             |
| **Precondition**  | Doctor is authenticated.                                                                                                                                                                                           |
| **Main Flow**     | 1. Doctor views patient list on dashboard. 2. Doctor clicks "View" on a patient. 3. System loads patient info, glucose measurements, therapies, and conditions. 4. System displays all data in organized sections. |
| **Postcondition** | Doctor has complete visibility over the patient's medical data.                                                                                                                                                    |

### UC-10: Update Patient Medical Info

| Field             | Description                                                                                                                                                                                                                             |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**            | UC-10                                                                                                                                                                                                                                   |
| **Name**          | Update Patient Medical Info                                                                                                                                                                                                             |
| **Actor(s)**      | Doctor                                                                                                                                                                                                                                  |
| **Precondition**  | Doctor is authenticated.                                                                                                                                                                                                                |
| **Main Flow**     | 1. Doctor clicks "Info" on a patient. 2. System loads current risk factors, pathologies, and comorbidities. 3. Doctor edits the fields. 4. Doctor clicks "Save Changes". 5. System updates the database and creates an audit log entry. |
| **Postcondition** | Patient info is updated. The operation is tracked with the doctor's identity in `operation_log`.                                                                                                                                        |

### UC-11: View Glucose Trend

| Field             | Description                                                                                                                                                                                                                                       |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ID**            | UC-11                                                                                                                                                                                                                                             |
| **Name**          | View Glucose Trend (Synthetic Summary)                                                                                                                                                                                                            |
| **Actor(s)**      | Doctor                                                                                                                                                                                                                                            |
| **Precondition**  | Doctor is authenticated and viewing a patient's detail.                                                                                                                                                                                           |
| **Main Flow**     | 1. Doctor clicks "Glucose Chart". 2. Doctor selects period (weekly or monthly). 3. System computes average glucose values (before/after meal), total measurements, and abnormal count for each period. 4. System displays the summary in a table. |
| **Postcondition** | Doctor sees the glucose evolution over time in a synthetic format.                                                                                                                                                                                |

### UC-14: View Doctor Audit Logs

| Field             | Description                                                                                                                                                                                                                                                              |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **ID**            | UC-14                                                                                                                                                                                                                                                                    |
| **Name**          | View Doctor Audit Logs                                                                                                                                                                                                                                                   |
| **Actor(s)**      | Doctor                                                                                                                                                                                                                                                                   |
| **Precondition**  | Doctor is authenticated.                                                                                                                                                                                                                                                 |
| **Main Flow**     | 1. Doctor clicks "View Audit Logs" on dashboard. 2. System retrieves all logs from `operation_log` where `doctor_id` matches the current doctor. 3. System maps target patient IDs to full names. 4. System displays the logs in a table sorted by timestamp descending. |
| **Postcondition** | Doctor views a modal list of their performed medical database actions.                                                                                                                                                                                                   |

---

## 10. Activity Diagrams

### 10.1 Activity Diagram: Login and Authentication

```mermaid
flowchart TD
    A([Start]) --> B[Display Login Screen]
    B --> C[User enters username and password]
    C --> D{Fields empty?}
    D -->|Yes| E[Show error: fill all fields]
    E --> C
    D -->|No| F{Check Doctor DB}
    F -->|Found & password matches| G[Set session as Doctor]
    G --> H[Navigate to Doctor Dashboard]
    F -->|Not found| I{Check Patient DB}
    I -->|Found & password matches| J[Set session as Patient]
    J --> K[Navigate to Patient Dashboard]
    I -->|Not found| L[Show error: invalid credentials]
    L --> C
    H --> M([End])
    K --> M
```

### 10.2 Activity Diagram: Record Blood Glucose Measurement

```mermaid
flowchart TD
    A([Start]) --> B[Patient opens glucose entry form]
    B --> C[Enter glucose value, time slot, date, time]
    C --> D{Validate input}
    D -->|Invalid| E[Show validation error]
    E --> C
    D -->|Valid| F[Save measurement to DB]
    F --> G{Check glucose thresholds}
    G -->|Normal| H[Show success confirmation]
    G -->|Abnormal: before meal outside 80-130| I[Show warning to patient]
    G -->|Abnormal: after meal above 180| I
    I --> J[Notify doctor via Observer pattern]
    J --> H
    H --> K([End])
```

### 10.3 Activity Diagram: Record Drug Intake

```mermaid
flowchart TD
    A([Start]) --> B[Patient opens drug intake form]
    B --> C{Active therapies exist?}
    C -->|No| D[Show message: no active therapies]
    D --> Z([End])
    C -->|Yes| E[Load active therapies in dropdown]
    E --> F[Patient selects therapy]
    F --> G[Auto-fill drug name and quantity]
    G --> H[Patient confirms date, time, quantity]
    H --> I{Validate input}
    I -->|Invalid| J[Show validation error]
    J --> H
    I -->|Valid| K[Save drug intake to DB linked to therapy]
    K --> L[Show success confirmation]
    L --> Z
```

### 10.4 Activity Diagram: Prescribe Therapy

```mermaid
flowchart TD
    A([Start]) --> B[Doctor selects patient from dashboard]
    B --> C[Doctor opens therapy form]
    C --> D[Enter drug name, daily intakes, quantity, directions, dates]
    D --> E{Validate input}
    E -->|Invalid| F[Show validation error]
    F --> D
    E -->|Valid| G[Save therapy to DB as active]
    G --> H[Create audit log entry in operation_log]
    H --> I[Show success confirmation]
    I --> J([End])
```

### 10.5 Activity Diagram: Missing Therapy Alert Generation

```mermaid
flowchart TD
    A([Start: Doctor Dashboard loads]) --> B[For each patient]
    B --> C[Load active therapies]
    C --> D[For each active therapy]
    D --> E[Check drug intakes for last 3 days]
    E --> F{Intakes missing for 3 consecutive days?}
    F -->|No| G[No alert for this therapy]
    F -->|Yes| H[Generate ALERT via MedicalRulesEngine]
    H --> I[Notify doctor via Observer pattern]
    I --> J[Display alert badge on dashboard]
    G --> K{More therapies?}
    J --> K
    K -->|Yes| D
    K -->|No| L{More patients?}
    L -->|Yes| B
    L -->|No| M([End])
```
