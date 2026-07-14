package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.PatientMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientMessageDAO {
    private final DatabaseManager dbManager;

    public PatientMessageDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a patient message.
     */
    public void save(PatientMessage msg) throws SQLException {
        String sql = "INSERT INTO patient_message (patient_id, doctor_id, subject, message, date, time) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, msg.getPatientId());
            pstmt.setInt(2, msg.getDoctorId());
            pstmt.setString(3, msg.getSubject());
            pstmt.setString(4, msg.getMessage());
            pstmt.setString(5, msg.getDate());
            pstmt.setString(6, msg.getTime());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    msg.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Retrieves messages sent by a specific patient.
     */
    public List<PatientMessage> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM patient_message WHERE patient_id = ? ORDER BY date DESC, time DESC";
        List<PatientMessage> messages = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        }
        return messages;
    }

    /**
     * Retrieves messages received by a specific doctor.
     */
    public List<PatientMessage> findByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT * FROM patient_message WHERE doctor_id = ? ORDER BY date DESC, time DESC";
        List<PatientMessage> messages = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        }
        return messages;
    }

    /**
     * Retrieves messages sent by a patient to a doctor.
     */
    public List<PatientMessage> findByPatientIdAndDoctorId(int patientId, int doctorId) throws SQLException {
        String sql = "SELECT * FROM patient_message WHERE patient_id = ? AND doctor_id = ? ORDER BY date DESC, time DESC";
        List<PatientMessage> messages = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setInt(2, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        }
        return messages;
    }

    private PatientMessage mapResultSetToMessage(ResultSet rs) throws SQLException {
        return new PatientMessage(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("subject"),
                rs.getString("message"),
                rs.getString("date"),
                rs.getString("time")
        );
    }
}
