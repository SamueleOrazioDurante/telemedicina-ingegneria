
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
VALUES ('FRCRSS70A01H501Z', 'Francesco', 'Rossi', 'francesco.rossi@gmail.com', 'franross', 'franross');

INSERT OR IGNORE INTO doctor (tax_code, first_name, last_name, email, username, password)
VALUES ('GVNNCG75B42F205Y', 'Giovanna', 'Bianchi', 'giovanna.bianchi@gmail.com', 'giovbian', 'giovbian');

-- Patients (1, 2, 3 are the students, born in 2005)
INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('PSQTMS05P09H501X', 'Tommaso', 'Pasquin', '2005-09-09', 'tommpasq', 'tommpasq', 1, 'Ex-smoker', NULL, 'Dyslipidemia');

INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('DRNSML05M08F205K', 'Samuele Orazio', 'Durante', '2006-08-08', 'samudura', 'samudura', 1, 'Sedentary Lifestyle', 'Appendectomy (2018)', NULL);

INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('CNTDNC05L07H501W', 'Dominic', 'Centrone', '2005-07-07', 'domicent', 'domicent', 2, 'Smoker, Obesity', NULL, 'Hypertension');

-- Other Patients (with conditions of choice)
INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('VRDLCA85E52F205Y', 'Alice', 'Verdi', '1985-05-12', 'alicverd', 'alicverd', 1, 'Obesity', NULL, 'Hypertension');

INSERT OR IGNORE INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, reference_doctor_id, risk_factors, past_pathologies, comorbidities)
VALUES ('NRIRRT79S20H501T', 'Roberto', 'Neri', '1979-11-20', 'robeneri', 'robeneri', 2, 'Smoker', 'Myocardial Infarction (2020)', NULL);

-- Prescribed Therapies for Patients
INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (1, 1, 'Metformin', 2, '500mg', 'After breakfast and dinner', '2026-05-01', NULL, 1);

INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (2, 1, 'Rapid Insulin', 3, '10 units', 'Before meals', '2026-05-15', NULL, 1);

INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (3, 2, 'Lantus Insulin', 1, '20 units', 'Before sleep', '2026-05-10', NULL, 1);

INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (4, 1, 'Metformin', 1, '850mg', 'After dinner', '2026-05-20', NULL, 1);

INSERT OR IGNORE INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, quantity_per_intake, directions, start_date, end_date, is_active)
VALUES (5, 2, 'Januvia', 1, '100mg', 'In the morning', '2026-05-01', NULL, 1);

-- Blood Glucose Measurements (June and July 2026)
-- Patient 1 (Tommaso)
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 105.0, 'BEFORE_MEAL', '2026-06-05', '08:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 145.0, 'AFTER_MEAL', '2026-06-05', '13:30');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 110.0, 'BEFORE_MEAL', '2026-06-20', '08:15');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 138.0, 'AFTER_MEAL', '2026-06-20', '14:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 98.0, 'BEFORE_MEAL', '2026-07-02', '07:45');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 135.0, 'AFTER_MEAL', '2026-07-02', '13:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 102.0, 'BEFORE_MEAL', '2026-07-12', '08:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (1, 142.0, 'AFTER_MEAL', '2026-07-12', '13:30');

-- Patient 2 (Samuele)
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 125.0, 'BEFORE_MEAL', '2026-06-12', '08:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 175.0, 'AFTER_MEAL', '2026-06-12', '14:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 118.0, 'BEFORE_MEAL', '2026-06-28', '08:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 195.0, 'AFTER_MEAL', '2026-06-28', '14:15'); -- High after meal
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 122.0, 'BEFORE_MEAL', '2026-07-06', '07:50');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 168.0, 'AFTER_MEAL', '2026-07-06', '13:45');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 132.0, 'BEFORE_MEAL', '2026-07-13', '08:05'); -- High before meal
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (2, 185.0, 'AFTER_MEAL', '2026-07-13', '14:10'); -- High after meal

-- Patient 3 (Dominic)
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 95.0, 'BEFORE_MEAL', '2026-06-08', '07:30');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 130.0, 'AFTER_MEAL', '2026-06-08', '13:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 100.0, 'BEFORE_MEAL', '2026-06-22', '07:45');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 140.0, 'AFTER_MEAL', '2026-06-22', '13:15');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 92.0, 'BEFORE_MEAL', '2026-07-05', '07:30');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 128.0, 'AFTER_MEAL', '2026-07-05', '13:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 105.0, 'BEFORE_MEAL', '2026-07-11', '07:40');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (3, 135.0, 'AFTER_MEAL', '2026-07-11', '13:20');

-- Patient 4 (Alice)
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 115.0, 'BEFORE_MEAL', '2026-06-10', '08:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 155.0, 'AFTER_MEAL', '2026-06-10', '13:45');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 120.0, 'BEFORE_MEAL', '2026-06-25', '08:10');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 160.0, 'AFTER_MEAL', '2026-06-25', '14:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 112.0, 'BEFORE_MEAL', '2026-07-04', '08:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 150.0, 'AFTER_MEAL', '2026-07-04', '13:30');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 118.0, 'BEFORE_MEAL', '2026-07-12', '08:05');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (4, 158.0, 'AFTER_MEAL', '2026-07-12', '13:50');

-- Patient 5 (Roberto)
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 140.0, 'BEFORE_MEAL', '2026-06-04', '07:30'); -- High before meal
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 190.0, 'AFTER_MEAL', '2026-06-04', '13:00');  -- High after meal
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 135.0, 'BEFORE_MEAL', '2026-06-18', '07:45'); -- High before meal
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 185.0, 'AFTER_MEAL', '2026-06-18', '13:15');  -- High after meal
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 128.0, 'BEFORE_MEAL', '2026-07-03', '07:30');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 178.0, 'AFTER_MEAL', '2026-07-03', '13:00');
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 142.0, 'BEFORE_MEAL', '2026-07-10', '07:40'); -- High before meal
INSERT OR IGNORE INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (5, 205.0, 'AFTER_MEAL', '2026-07-10', '13:20');  -- High after meal

-- Drug Intakes (June and July 2026)
-- Patient 1 (Metformin, 2 daily intakes - therapy ID 1)
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-06-05', '08:30', '500mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-06-05', '20:30', '500mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-06-20', '08:30', '500mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-06-20', '20:30', '500mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-07-02', '08:15', '500mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-07-02', '20:15', '500mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-07-12', '08:30', '500mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (1, 1, '2026-07-12', '20:30', '500mg');

-- Patient 2 (Rapid Insulin, 3 daily intakes - therapy ID 2)
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-06-12', '08:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-06-12', '13:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-06-12', '20:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-06-28', '08:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-06-28', '13:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-06-28', '20:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-07-06', '08:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-07-06', '13:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-07-06', '20:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-07-13', '08:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-07-13', '13:00', '10 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (2, 2, '2026-07-13', '20:00', '10 units');

-- Patient 3 (Lantus Insulin, 1 daily intake - therapy ID 3)
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (3, 3, '2026-06-08', '22:00', '20 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (3, 3, '2026-06-22', '22:00', '20 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (3, 3, '2026-07-05', '22:00', '20 units');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (3, 3, '2026-07-11', '22:00', '20 units');

-- Patient 4 (Metformin, 1 daily intake - therapy ID 4)
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (4, 4, '2026-06-10', '20:30', '850mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (4, 4, '2026-06-25', '20:45', '850mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (4, 4, '2026-07-04', '20:15', '850mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (4, 4, '2026-07-12', '20:30', '850mg');

-- Patient 5 (Januvia, 1 daily intake - therapy ID 5)
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (5, 5, '2026-06-04', '08:00', '100mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (5, 5, '2026-06-18', '08:00', '100mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (5, 5, '2026-07-03', '08:00', '100mg');
INSERT OR IGNORE INTO drug_intake (patient_id, therapy_id, date, time, quantity_taken) VALUES (5, 5, '2026-07-10', '08:00', '100mg');

-- Concomitant Conditions (June and July 2026)
INSERT OR IGNORE INTO concomitant_condition (patient_id, type, description, start_date, end_date) VALUES (1, 'SYMPTOM', 'Mild headache', '2026-06-05', NULL);
INSERT OR IGNORE INTO concomitant_condition (patient_id, type, description, start_date, end_date) VALUES (1, 'SYMPTOM', 'Fatigue in the evening', '2026-07-02', '2026-07-03');
INSERT OR IGNORE INTO concomitant_condition (patient_id, type, description, start_date, end_date) VALUES (2, 'SYMPTOM', 'Nausea', '2026-06-12', NULL);
INSERT OR IGNORE INTO concomitant_condition (patient_id, type, description, start_date, end_date) VALUES (2, 'SYMPTOM', 'Dizziness after exercise', '2026-07-06', NULL);
INSERT OR IGNORE INTO concomitant_condition (patient_id, type, description, start_date, end_date) VALUES (5, 'PATHOLOGY', 'Seasonal allergy', '2026-06-01', '2026-06-20');
