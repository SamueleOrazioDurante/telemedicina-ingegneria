
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS doctor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tax_code TEXT UNIQUE NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS patient (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tax_code TEXT UNIQUE NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    date_of_birth TEXT NOT NULL, -- YYYY-MM-DD
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    reference_doctor_id INTEGER NOT NULL,
    risk_factors TEXT,
    past_pathologies TEXT,
    comorbidities TEXT,
    FOREIGN KEY (reference_doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS blood_glucose_measurement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    value REAL NOT NULL,
    time_slot TEXT NOT NULL, -- 'BEFORE_MEAL' or 'AFTER_MEAL'
    date TEXT NOT NULL,      -- YYYY-MM-DD
    time TEXT NOT NULL,      -- HH:MM
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS prescribed_therapy (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    drug_name TEXT NOT NULL,
    daily_intakes INTEGER NOT NULL,
    quantity_per_intake TEXT NOT NULL,
    directions TEXT,
    start_date TEXT NOT NULL, -- YYYY-MM-DD
    end_date TEXT,            -- YYYY-MM-DD
    is_active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS drug_intake (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    therapy_id INTEGER NOT NULL,
    date TEXT NOT NULL, -- YYYY-MM-DD
    time TEXT NOT NULL, -- HH:MM
    drug_name TEXT NOT NULL,
    quantity_taken TEXT NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    FOREIGN KEY (therapy_id) REFERENCES prescribed_therapy(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS concomitant_condition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    type TEXT NOT NULL, -- 'SYMPTOM', 'PATHOLOGY', 'CONCOMITANT_THERAPY'
    description TEXT NOT NULL,
    start_date TEXT NOT NULL, -- YYYY-MM-DD
    end_date TEXT,            -- YYYY-MM-DD
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS operation_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doctor_id INTEGER NOT NULL,
    patient_id INTEGER,
    operation TEXT NOT NULL,
    timestamp TEXT NOT NULL, -- YYYY-MM-DD HH:MM:SS
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE SET NULL
);
