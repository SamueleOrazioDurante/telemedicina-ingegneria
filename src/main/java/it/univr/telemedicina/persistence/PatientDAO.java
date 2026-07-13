package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private final DatabaseManager dbManager;

    public PatientDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves or updates a patient.
     */
    public void save(Patient patient) throws SQLException {
        if (patient.getId() == null) {
            String sql = "INSERT INTO patient (tax_code, first_name, last_name, date_of_birth, username, password, " +
                    "reference_doctor_id, risk_factors, past_pathologies, comorbidities) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, patient.getTaxCode());
                pstmt.setString(2, patient.getFirstName());
                pstmt.setString(3, patient.getLastName());
                pstmt.setString(4, patient.getDateOfBirth());
                pstmt.setString(5, patient.getUsername());
                pstmt.setString(6, patient.getPassword());
                pstmt.setInt(7, patient.getReferenceDoctorId());
                pstmt.setString(8, patient.getRiskFactors());
                pstmt.setString(9, patient.getPastPathologies());
                pstmt.setString(10, patient.getComorbidities());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        patient.setId(rs.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE patient SET tax_code = ?, first_name = ?, last_name = ?, date_of_birth = ?, username = ?, " +
                    "password = ?, reference_doctor_id = ?, risk_factors = ?, past_pathologies = ?, comorbidities = ? WHERE id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, patient.getTaxCode());
                pstmt.setString(2, patient.getFirstName());
                pstmt.setString(3, patient.getLastName());
                pstmt.setString(4, patient.getDateOfBirth());
                pstmt.setString(5, patient.getUsername());
                pstmt.setString(6, patient.getPassword());
                pstmt.setInt(7, patient.getReferenceDoctorId());
                pstmt.setString(8, patient.getRiskFactors());
                pstmt.setString(9, patient.getPastPathologies());
                pstmt.setString(10, patient.getComorbidities());
                pstmt.setInt(11, patient.getId());
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Finds a patient by ID.
     */
    public Patient findById(int id) throws SQLException {
        String sql = "SELECT * FROM patient WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds a patient by username.
     */
    public Patient findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM patient WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds patients assigned to a specific reference doctor.
     */
    public List<Patient> findByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT * FROM patient WHERE reference_doctor_id = ?";
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        }
        return patients;
    }

    /**
     * Retrieves all patients.
     */
    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT * FROM patient";
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        }
        return patients;
    }

    /**
     * Updates only a patient's medical information.
     */
    public void updateMedicalInfo(int id, String riskFactors, String pastPathologies, String comorbidities) throws SQLException {
        String sql = "UPDATE patient SET risk_factors = ?, past_pathologies = ?, comorbidities = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, riskFactors);
            pstmt.setString(2, pastPathologies);
            pstmt.setString(3, comorbidities);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
        }
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("tax_code"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("date_of_birth"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getInt("reference_doctor_id"),
                rs.getString("risk_factors"),
                rs.getString("past_pathologies"),
                rs.getString("comorbidities")
        );
    }
}
