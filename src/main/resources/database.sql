
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS doctor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tax_code TEXT UNIQUE NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
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
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT,
    UNIQUE(patient_id, drug_name, start_date)
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

-- SEED DATA: Initial demo data for testing and demonstration

-- Doctors (password stored as plain text for demo; in production use hashing)
INSERT OR IGNORE INTO doctor (tax_code, first_name, last_name, email, username, password)
VALUES ('RSSMRA80A01H501Z', 'Mario', 'Rossi', 'mario.rossi@telemedicina.it', 'dottore', 'dottore');

INSERT OR IGNORE INTO doctor (tax_code, first_name, last_name, email, username, password)
VALUES ('VRDLGI75B02F205X', 'Luigi', 'Verdi', 'luigi.verdi@telemedicina.it', 'drverdi', 'drverdi');

-- Patients (assigned to doctor 1)
INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('BNCGNN90C03H501Y', 'Giovanni', 'Bianchi', '1990-03-03', 'paziente', 'paziente', 1, 'Smoker, Obesity', NULL, 'Hypertension');

INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('NRILRA85D04L219K', 'Laura', 'Neri', '1985-04-04', 'lneri', 'lneri', 1, NULL, 'Appendectomy (2010)', NULL);

INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('CNTMRC78E05H501W', 'Marco', 'Conti', '1978-05-05', 'mconti', 'mconti', 2, 'Ex-smoker', NULL, 'Hypertension, Dyslipidemia');

-- Sample therapies for patient 1 (Giovanni Bianchi)
INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (1, 1, 'Metformin', 2, '500mg', 'After meals', '2025-01-01', NULL, 1);

INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (1, 1, 'Rapid Insulin', 3, '10 units', 'Before meals', '2025-03-15', NULL, 1);

-- Sample therapy for patient 2 (Laura Neri)
INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (2, 1, 'Metformin', 1, '850mg', 'After dinner', '2025-02-01', NULL, 1);
