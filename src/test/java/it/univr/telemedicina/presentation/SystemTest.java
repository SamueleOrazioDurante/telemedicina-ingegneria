package it.univr.telemedicina.presentation;

import it.univr.telemedicina.domain.*;
import it.univr.telemedicina.logic.MedicalRulesEngine;
import it.univr.telemedicina.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System Tests (Black-Box) - Dominic Centrone (VR516778)
 *
 * These tests simulate end-to-end user workflows by executing the same logic
 * that the GUI controllers use (Controller → Business Logic → DAO → DB),
 * verifying the correct behavior of the complete system from the user's perspective.
 *
 * Unlike unit tests, these tests use a real SQLite database (in-memory / temp file)
 * and verify the integration of all layers together.
 */
public class SystemTest {

    private DatabaseManager dbManager;
    private DoctorDAO doctorDAO;
    private PatientDAO patientDAO;
    private BloodGlucoseMeasurementDAO glucoseDAO;
    private PrescribedTherapyDAO therapyDAO;
    private DrugIntakeDAO intakeDAO;
    private ConcomitantConditionDAO conditionDAO;
    private OperationLogDAO logDAO;
    private MedicalRulesEngine engine;

    // Test data
    private Doctor testDoctor;
    private Patient testPatient;

    @BeforeEach
    public void setUp() throws Exception {
        // Clean up any existing test database file to guarantee a fresh, isolated state
        java.io.File dbFile = new java.io.File("target/system-test-telemedicina.db");
        if (dbFile.exists()) {
            System.gc();
            Thread.sleep(50);
            dbFile.delete();
        }

        dbManager = new DatabaseManager("jdbc:sqlite:target/system-test-telemedicina.db");
        dbManager.initializeDatabase();

        doctorDAO = new DoctorDAO(dbManager);
        patientDAO = new PatientDAO(dbManager);
        glucoseDAO = new BloodGlucoseMeasurementDAO(dbManager);
        therapyDAO = new PrescribedTherapyDAO(dbManager);
        intakeDAO = new DrugIntakeDAO(dbManager);
        conditionDAO = new ConcomitantConditionDAO(dbManager);
        logDAO = new OperationLogDAO(dbManager);
        engine = new MedicalRulesEngine();

        // Create base test data (simulates admin inserting initial users)
        testDoctor = new Doctor("TSTDOC80A01H501Z", "Test", "Doctor", "testdoc", "password123");
        doctorDAO.save(testDoctor);

        testPatient = new Patient("TSTPAT90B02H501Y", "Test", "Patient", "1990-02-02",
                "testpat", "password456", testDoctor.getId());
        patientDAO.save(testPatient);
    }

    
    // TEST 1: Patient Login - Valid credentials lead to successful authentication
    
    @Test
    public void testLoginPatient() throws Exception {
        // Simulate: user enters credentials → system verifies against DB
        Patient found = patientDAO.findByUsername("testpat");

        assertNotNull(found, "Patient should be found by username");
        assertEquals("password456", found.getPassword(), "Password should match");
        assertEquals("Test", found.getFirstName());
        assertEquals(testDoctor.getId(), found.getReferenceDoctorId(), "Should be assigned to correct doctor");
    }

    
    // TEST 2: Doctor Login - Valid credentials lead to successful authentication
    
    @Test
    public void testLoginDoctor() throws Exception {
        Doctor found = doctorDAO.findByUsername("testdoc");

        assertNotNull(found, "Doctor should be found by username");
        assertEquals("password123", found.getPassword(), "Password should match");
        assertEquals("Test", found.getFirstName());
    }

    
    // TEST 3: Login - Invalid credentials return null (no user found)
    
    @Test
    public void testLoginInvalidCredentials() throws Exception {
        Doctor doc = doctorDAO.findByUsername("nonexistent");
        assertNull(doc, "Non-existent username should return null");

        Patient pat = patientDAO.findByUsername("nonexistent");
        assertNull(pat, "Non-existent patient username should return null");

        // Simulate: correct username but wrong password
        Patient found = patientDAO.findByUsername("testpat");
        assertNotNull(found);
        assertNotEquals("wrongpassword", found.getPassword(), "Wrong password should not match");
    }

    
    // TEST 4: Patient enters glucose measurement → saved to DB correctly
    
    @Test
    public void testPatientEnterGlucose() throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Simulate: patient enters glucose reading via the form
        BloodGlucoseMeasurement measurement = new BloodGlucoseMeasurement(
                testPatient.getId(), 115.0, "BEFORE_MEAL", today, "08:30");
        glucoseDAO.save(measurement);

        assertNotNull(measurement.getId(), "Measurement ID should be auto-generated");

        // Verify it's retrievable
        List<BloodGlucoseMeasurement> results = glucoseDAO.findByPatientIdAndPeriod(
                testPatient.getId(), today, today);
        assertEquals(1, results.size(), "Should have exactly one measurement for today");
        assertEquals(115.0, results.get(0).getValue(), 0.01, "Value should match");
        assertEquals("BEFORE_MEAL", results.get(0).getTimeSlot(), "Time slot should match");
    }

    
    // TEST 5: Patient enters abnormal glucose → alert is generated
    
    @Test
    public void testPatientGlucoseAlertGenerated() throws Exception {
        List<String> receivedAlerts = new ArrayList<>();
        engine.registerObserver(receivedAlerts::add);

        // Glucose >130 before meal → alert
        BloodGlucoseMeasurement high = new BloodGlucoseMeasurement(
                testPatient.getId(), 145.0, "BEFORE_MEAL", "2026-07-13", "08:00");
        glucoseDAO.save(high);

        boolean alert = engine.checkGlucoseThreshold(high);
        assertTrue(alert, "Should trigger alert for glucose >130 before meal");
        assertEquals(1, receivedAlerts.size(), "Observer should receive exactly 1 alert");
        assertTrue(receivedAlerts.get(0).contains("Abnormal"), "Alert message should mention abnormal level");

        // Glucose >180 after meal → alert
        receivedAlerts.clear();
        BloodGlucoseMeasurement highAfter = new BloodGlucoseMeasurement(
                testPatient.getId(), 200.0, "AFTER_MEAL", "2026-07-13", "14:00");
        glucoseDAO.save(highAfter);

        alert = engine.checkGlucoseThreshold(highAfter);
        assertTrue(alert, "Should trigger alert for glucose >180 after meal");

        // Normal value → no alert
        receivedAlerts.clear();
        BloodGlucoseMeasurement normal = new BloodGlucoseMeasurement(
                testPatient.getId(), 100.0, "BEFORE_MEAL", "2026-07-13", "07:00");
        glucoseDAO.save(normal);

        alert = engine.checkGlucoseThreshold(normal);
        assertFalse(alert, "Normal value should not trigger alert");
        assertTrue(receivedAlerts.isEmpty(), "No alert for normal value");
    }

    
    // TEST 6: Patient records drug intake → saved and coherent with therapy
    
    @Test
    public void testPatientRecordDrugIntake() throws Exception {
        // Doctor prescribes therapy
        PrescribedTherapy therapy = new PrescribedTherapy(
                testPatient.getId(), testDoctor.getId(), "Metformin", 2, "500mg",
                "After meals", "2025-01-01", null);
        therapyDAO.save(therapy);
        assertNotNull(therapy.getId());

        // Patient records intake
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        DrugIntake intake = new DrugIntake(testPatient.getId(), therapy.getId(),
                today, "13:00", "Metformin", "500mg");
        intakeDAO.save(intake);

        assertNotNull(intake.getId(), "Intake ID should be auto-generated");

        // Verify retrieval
        List<DrugIntake> todayIntakes = intakeDAO.findByPatientIdAndDate(testPatient.getId(), today);
        assertEquals(1, todayIntakes.size(), "Should have 1 intake today");
        assertEquals("Metformin", todayIntakes.get(0).getDrugName());
        assertEquals(therapy.getId(), todayIntakes.get(0).getTherapyId(),
                "Intake should reference the correct therapy");
    }

    
    // TEST 7: Patient reports symptom → saved to DB
    
    @Test
    public void testPatientReportSymptom() throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        ConcomitantCondition symptom = new ConcomitantCondition(
                testPatient.getId(), "SYMPTOM", "Headache and nausea", today, null);
        conditionDAO.save(symptom);
        assertNotNull(symptom.getId());

        ConcomitantCondition pathology = new ConcomitantCondition(
                testPatient.getId(), "PATHOLOGY", "Seasonal flu", today, null);
        conditionDAO.save(pathology);

        // Verify by type
        List<ConcomitantCondition> symptoms = conditionDAO.findByPatientIdAndType(testPatient.getId(), "SYMPTOM");
        assertEquals(1, symptoms.size());
        assertEquals("Headache and nausea", symptoms.get(0).getDescription());

        List<ConcomitantCondition> pathologies = conditionDAO.findByPatientIdAndType(testPatient.getId(), "PATHOLOGY");
        assertEquals(1, pathologies.size());

        // Verify total
        List<ConcomitantCondition> all = conditionDAO.findByPatientId(testPatient.getId());
        assertEquals(2, all.size(), "Should have 2 total conditions");
    }

    
    // TEST 8: Doctor prescribes therapy → saved in DB + operation log created
    
    @Test
    public void testDoctorPrescribeTherapy() throws Exception {
        PrescribedTherapy therapy = new PrescribedTherapy(
                testPatient.getId(), testDoctor.getId(), "Rapid Insulin", 3, "10 units",
                "Before meals", "2026-07-01", null);
        therapyDAO.save(therapy);
        assertNotNull(therapy.getId());
        assertTrue(therapy.isActive());

        // Doctor logs the operation (as TherapyFormController does)
        OperationLog log = new OperationLog(testDoctor.getId(), testPatient.getId(),
                "Prescribed therapy: Rapid Insulin (10 units x3/day)",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logDAO.save(log);
        assertNotNull(log.getId());

        // Verify therapy retrieval
        List<PrescribedTherapy> active = therapyDAO.findActiveByPatientId(testPatient.getId());
        assertEquals(1, active.size());
        assertEquals("Rapid Insulin", active.get(0).getDrugName());

        // Verify audit log
        List<OperationLog> logs = logDAO.findByDoctorId(testDoctor.getId());
        assertTrue(logs.size() >= 1);
        assertTrue(logs.get(0).getOperation().contains("Rapid Insulin"));
    }

    
    // TEST 9: Doctor views patient data → all data is accessible
    
    @Test
    public void testDoctorViewPatientData() throws Exception {
        // Create sample data
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        glucoseDAO.save(new BloodGlucoseMeasurement(testPatient.getId(), 120.0, "BEFORE_MEAL", today, "08:00"));
        glucoseDAO.save(new BloodGlucoseMeasurement(testPatient.getId(), 160.0, "AFTER_MEAL", today, "13:00"));

        PrescribedTherapy therapy = new PrescribedTherapy(testPatient.getId(), testDoctor.getId(),
                "Metformin", 2, "500mg", "After meals", "2025-01-01", null);
        therapyDAO.save(therapy);

        conditionDAO.save(new ConcomitantCondition(testPatient.getId(), "SYMPTOM", "Fatigue", today, null));

        // Simulate doctor retrieving all data (as PatientDetailController does)
        Patient retrieved = patientDAO.findById(testPatient.getId());
        assertNotNull(retrieved);

        List<BloodGlucoseMeasurement> glucose = glucoseDAO.findByPatientId(testPatient.getId());
        assertEquals(2, glucose.size(), "Doctor should see all glucose measurements");

        List<PrescribedTherapy> therapies = therapyDAO.findAllByPatientId(testPatient.getId());
        assertEquals(1, therapies.size(), "Doctor should see all therapies");

        List<ConcomitantCondition> conditions = conditionDAO.findByPatientId(testPatient.getId());
        assertEquals(1, conditions.size(), "Doctor should see all conditions");
    }

    
    // TEST 10: Doctor updates patient info → DB updated + audit log created
    
    @Test
    public void testDoctorUpdatePatientInfo() throws Exception {
        // Initial state: no risk factors
        assertNull(testPatient.getRiskFactors());

        // Doctor updates (as PatientInfoEditController does)
        patientDAO.updateMedicalInfo(testPatient.getId(),
                "Smoker, Obesity", "Previous cardiac event (2020)", "Hypertension");

        // Log the operation
        logDAO.save(new OperationLog(testDoctor.getId(), testPatient.getId(),
                "Updated patient medical info (risk factors, pathologies, comorbidities)",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        // Verify DB
        Patient updated = patientDAO.findById(testPatient.getId());
        assertEquals("Smoker, Obesity", updated.getRiskFactors());
        assertEquals("Previous cardiac event (2020)", updated.getPastPathologies());
        assertEquals("Hypertension", updated.getComorbidities());

        // Verify audit trail
        List<OperationLog> logs = logDAO.findByPatientId(testPatient.getId());
        assertTrue(logs.size() >= 1);
        assertTrue(logs.get(0).getOperation().contains("medical info"));
    }

    
    // TEST 11: Missing therapy for 3+ days → alert generated for doctor
    
    @Test
    public void testMissingTherapyAlertFlow() throws Exception {
        List<String> receivedAlerts = new ArrayList<>();
        engine.registerObserver(receivedAlerts::add);

        // Prescribe therapy requiring 2 intakes/day
        PrescribedTherapy therapy = new PrescribedTherapy(
                testPatient.getId(), testDoctor.getId(), "Metformin", 2, "500mg",
                "After meals", "2025-01-01", null);
        therapyDAO.save(therapy);

        // Patient has NO intakes for the last 3 days
        List<DrugIntake> emptyIntakes = new ArrayList<>();
        boolean missed = engine.checkMissingTherapy(emptyIntakes, therapy, LocalDate.now());
        assertTrue(missed, "Should detect 3 consecutive days without medication");
        assertEquals(1, receivedAlerts.size(), "Alert should be sent to observers");
        assertTrue(receivedAlerts.get(0).contains("missed therapy") || receivedAlerts.get(0).contains("Missed"),
                "Alert should mention missed therapy");
    }

    
    // TEST 12: Therapy compliance check - intakes are coherent with prescriptions
    
    @Test
    public void testTherapyComplianceCheck() throws Exception {
        List<String> receivedAlerts = new ArrayList<>();
        engine.registerObserver(receivedAlerts::add);

        // Prescribe therapy: 1 intake/day
        PrescribedTherapy therapy = new PrescribedTherapy(
                testPatient.getId(), testDoctor.getId(), "Insulin", 1, "10mg",
                "After meal", "2025-01-01", null);
        therapyDAO.save(therapy);

        // Patient took medication for all 3 previous days → compliant
        LocalDate today = LocalDate.now();
        List<DrugIntake> intakes = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String date = today.minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            DrugIntake intake = new DrugIntake(testPatient.getId(), therapy.getId(),
                    date, "08:00", "Insulin", "10mg");
            intakeDAO.save(intake);
            intakes.add(intake);
        }

        boolean missed = engine.checkMissingTherapy(intakes, therapy, today);
        assertFalse(missed, "Patient is compliant - should NOT trigger alert");
        assertTrue(receivedAlerts.isEmpty(), "No alerts for compliant patient");
    }
}
