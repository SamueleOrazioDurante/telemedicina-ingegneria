package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataConsistencyTest {
    private DatabaseManager dbManager;
    private DoctorDAO doctorDAO;
    private PatientDAO patientDAO;
    private BloodGlucoseMeasurementDAO glucoseDAO;
    private PrescribedTherapyDAO therapyDAO;
    private DrugIntakeDAO intakeDAO;
    private ConcomitantConditionDAO conditionDAO;
    private OperationLogDAO logDAO;

    @BeforeEach
    public void setUp() throws Exception {
        // Clean up any existing test database file to guarantee a fresh, isolated state
        java.io.File dbFile = new java.io.File("target/test-telemedicina.db");
        if (dbFile.exists()) {
            System.gc();
            Thread.sleep(50);
            dbFile.delete();
        }

        dbManager = new DatabaseManager("jdbc:sqlite:target/test-telemedicina.db");
        dbManager.initializeDatabase();

        doctorDAO = new DoctorDAO(dbManager);
        patientDAO = new PatientDAO(dbManager);
        glucoseDAO = new BloodGlucoseMeasurementDAO(dbManager);
        therapyDAO = new PrescribedTherapyDAO(dbManager);
        intakeDAO = new DrugIntakeDAO(dbManager);
        conditionDAO = new ConcomitantConditionDAO(dbManager);
        logDAO = new OperationLogDAO(dbManager);
    }

    @Test
    public void testCreateAndRetrieveDoctorAndPatient() throws SQLException {
        Doctor doctor = new Doctor("MRNRSS80A01H501Z", "Mario", "Rossi", "mariorossi", "password123");
        doctorDAO.save(doctor);
        assertNotNull(doctor.getId(), "Doctor ID should be generated");

        Doctor retrievedDoctor = doctorDAO.findById(doctor.getId());
        assertNotNull(retrievedDoctor);
        assertEquals("Mario", retrievedDoctor.getFirstName());
        assertEquals("MRNRSS80A01H501Z", retrievedDoctor.getTaxCode());

        Patient patient = new Patient("LBRGNN90B02H501Y", "Gianni", "Liberi", "1990-02-02", "gianniliberi", "pazpass", doctor.getId());
        patientDAO.save(patient);
        assertNotNull(patient.getId(), "Patient ID should be generated");

        Patient retrievedPatient = patientDAO.findById(patient.getId());
        assertNotNull(retrievedPatient);
        assertEquals("Gianni", retrievedPatient.getFirstName());
        assertEquals(doctor.getId(), retrievedPatient.getReferenceDoctorId());
    }

    @Test
    public void testUniqueConstraints() throws SQLException {
        Doctor doctor = new Doctor("CF1", "Doc", "One", "sameuser", "pass");
        doctorDAO.save(doctor);

        // Attempting to save another doctor with the same username should throw SQLException
        Doctor duplicateUserDoctor = new Doctor("CF2", "Doc", "Two", "sameuser", "pass");
        assertThrows(SQLException.class, () -> doctorDAO.save(duplicateUserDoctor), 
                "Should fail due to duplicate username constraint");

        // Attempting to save another doctor with the same Tax Code should throw SQLException
        Doctor duplicateCfDoctor = new Doctor("CF1", "Doc", "Three", "otheruser", "pass");
        assertThrows(SQLException.class, () -> doctorDAO.save(duplicateCfDoctor), 
                "Should fail due to duplicate tax code constraint");
    }

    @Test
    public void testForeignKeyDoctorDoesNotExist() {
        Patient patient = new Patient("CF_PAT", "Pat", "Test", "1995-05-05", "patuser", "pass", 999);
        assertThrows(SQLException.class, () -> patientDAO.save(patient),
                "Should throw SQLException due to foreign key violation (doctor does not exist)");
    }

    @Test
    public void testDoctorDeletionRestricted() throws SQLException {
        Doctor doctor = new Doctor("CF_DOC", "Doc", "Test", "docuser", "pass");
        doctorDAO.save(doctor);

        Patient patient = new Patient("CF_PAT", "Pat", "Test", "1995-05-05", "patuser", "pass", doctor.getId());
        patientDAO.save(patient);

        // Deleting doctor must fail due to ON DELETE RESTRICT on reference_doctor_id
        try (java.sql.Connection conn = dbManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("DELETE FROM doctor WHERE id = ?")) {
            pstmt.setInt(1, doctor.getId());
            assertThrows(SQLException.class, () -> pstmt.executeUpdate(),
                    "Should fail to delete doctor because of RESTRICT foreign key constraint on patient");
        }
    }

    @Test
    public void testCascadingDeletePatient() throws SQLException {
        Doctor doctor = new Doctor("CF_DOC", "Doc", "Test", "docuser", "pass");
        doctorDAO.save(doctor);

        Patient patient = new Patient("CF_PAT", "Pat", "Test", "1995-05-05", "patuser", "pass", doctor.getId());
        patientDAO.save(patient);

        BloodGlucoseMeasurement glucose = new BloodGlucoseMeasurement(patient.getId(), 120.0, "BEFORE_MEAL", "2026-07-13", "08:00");
        glucoseDAO.save(glucose);

        PrescribedTherapy therapy = new PrescribedTherapy(patient.getId(), doctor.getId(), "Metformin", 2, "500mg", "after meals", "2026-07-10", null);
        therapyDAO.save(therapy);

        DrugIntake intake = new DrugIntake(patient.getId(), therapy.getId(), "2026-07-13", "13:00", "Metformin", "500mg");
        intakeDAO.save(intake);

        ConcomitantCondition condition = new ConcomitantCondition(patient.getId(), "SYMPTOM", "Headache", "2026-07-13", null);
        conditionDAO.save(condition);

        // Verify they are saved
        assertEquals(1, glucoseDAO.findByPatientId(patient.getId()).size());
        assertEquals(1, therapyDAO.findActiveByPatientId(patient.getId()).size());
        assertEquals(1, intakeDAO.findByPatientId(patient.getId()).size());
        assertEquals(1, conditionDAO.findByPatientId(patient.getId()).size());

        // Delete patient directly to verify ON DELETE CASCADE triggers
        try (java.sql.Connection conn = dbManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("DELETE FROM patient WHERE id = ?")) {
            pstmt.setInt(1, patient.getId());
            pstmt.executeUpdate();
        }

        assertNull(patientDAO.findById(patient.getId()));
        assertEquals(0, glucoseDAO.findByPatientId(patient.getId()).size(), "Glucose readings should be deleted");
        assertEquals(0, therapyDAO.findAllByPatientId(patient.getId()).size(), "Therapies should be deleted");
        assertEquals(0, intakeDAO.findByPatientId(patient.getId()).size(), "Intakes should be deleted");
        assertEquals(0, conditionDAO.findByPatientId(patient.getId()).size(), "Concomitant conditions should be deleted");
    }

    @Test
    public void testBloodGlucoseDateFilters() throws SQLException {
        Doctor doctor = new Doctor("CF_D", "D", "D", "duser", "pass");
        doctorDAO.save(doctor);
        Patient patient = new Patient("CF_P", "P", "P", "1990-01-01", "puser", "pass", doctor.getId());
        patientDAO.save(patient);

        BloodGlucoseMeasurement g1 = new BloodGlucoseMeasurement(patient.getId(), 110.0, "BEFORE_MEAL", "2026-07-01", "08:00");
        BloodGlucoseMeasurement g2 = new BloodGlucoseMeasurement(patient.getId(), 145.0, "AFTER_MEAL", "2026-07-05", "13:00");
        BloodGlucoseMeasurement g3 = new BloodGlucoseMeasurement(patient.getId(), 95.0, "BEFORE_MEAL", "2026-07-10", "20:00");
        glucoseDAO.save(g1);
        glucoseDAO.save(g2);
        glucoseDAO.save(g3);

        List<BloodGlucoseMeasurement> list = glucoseDAO.findByPatientIdAndPeriod(patient.getId(), "2026-07-03", "2026-07-08");
        assertEquals(1, list.size());
        assertEquals(145.0, list.get(0).getValue());
    }

    @Test
    public void testLogOperazioneAndAudit() throws SQLException {
        Doctor doctor = new Doctor("CF_DOC", "Doc", "Test", "docuser", "pass");
        doctorDAO.save(doctor);

        Patient patient = new Patient("CF_PAT", "Pat", "Test", "1995-05-05", "patuser", "pass", doctor.getId());
        patientDAO.save(patient);

        OperationLog log = new OperationLog(doctor.getId(), patient.getId(), "Prescribed Metformin", "2026-07-13 15:00:00");
        logDAO.save(log);
        assertNotNull(log.getId());

        List<OperationLog> logs = logDAO.findByDoctorId(doctor.getId());
        assertEquals(1, logs.size());
        assertEquals("Prescribed Metformin", logs.get(0).getOperation());
        assertEquals(patient.getId(), logs.get(0).getPatientId());

        // Verify SET NULL constraint works on deletion of patient
        try (java.sql.Connection conn = dbManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("DELETE FROM patient WHERE id = ?")) {
            pstmt.setInt(1, patient.getId());
            pstmt.executeUpdate();
        }

        List<OperationLog> postDeleteLogs = logDAO.findByDoctorId(doctor.getId());
        assertEquals(1, postDeleteLogs.size());
        assertNull(postDeleteLogs.get(0).getPatientId(), "Patient ID in log should be nullified (SET NULL)");
    }

    @Test
    public void testConcomitantConditionOperations() throws SQLException {
        Doctor doctor = new Doctor("CF_DOC", "Doc", "Test", "docuser", "pass");
        doctorDAO.save(doctor);

        Patient patient = new Patient("CF_PAT", "Pat", "Test", "1995-05-05", "patuser", "pass", doctor.getId());
        patientDAO.save(patient);

        // Create new condition
        ConcomitantCondition condition = new ConcomitantCondition(patient.getId(), "SYMPTOM", "Headache", "2026-07-13", null);
        conditionDAO.save(condition);
        assertNotNull(condition.getId(), "Condition ID should be generated");

        // Verify finding by patient ID
        List<ConcomitantCondition> list = conditionDAO.findByPatientId(patient.getId());
        assertEquals(1, list.size());
        assertEquals("Headache", list.get(0).getDescription());

        // Update condition
        condition.setDescription("Migraine");
        conditionDAO.save(condition);

        // Verify update
        List<ConcomitantCondition> updatedList = conditionDAO.findByPatientId(patient.getId());
        assertEquals(1, updatedList.size());
        assertEquals("Migraine", updatedList.get(0).getDescription());

        // Save a pathology as well
        ConcomitantCondition pathology = new ConcomitantCondition(patient.getId(), "PATHOLOGY", "Type 2 Diabetes", "2026-07-13", null);
        conditionDAO.save(pathology);

        // Verify find by patient ID and type
        List<ConcomitantCondition> symptoms = conditionDAO.findByPatientIdAndType(patient.getId(), "SYMPTOM");
        assertEquals(1, symptoms.size());
        assertEquals("Migraine", symptoms.get(0).getDescription());

        List<ConcomitantCondition> pathologies = conditionDAO.findByPatientIdAndType(patient.getId(), "PATHOLOGY");
        assertEquals(1, pathologies.size());
        assertEquals("Type 2 Diabetes", pathologies.get(0).getDescription());
    }
}
