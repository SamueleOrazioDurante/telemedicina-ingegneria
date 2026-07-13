package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.OperationLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperationLogDAO {
    private final DatabaseManager dbManager;

    public OperationLogDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a log entry.
     */
    public void save(OperationLog log) throws SQLException {
        String sql = "INSERT INTO operation_log (doctor_id, patient_id, operation, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, log.getDoctorId());
            if (log.getPatientId() != null) {
                pstmt.setInt(2, log.getPatientId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, log.getOperation());
            pstmt.setString(4, log.getTimestamp());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Retrieves logs for a specific doctor.
     */
    public List<OperationLog> findByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT * FROM operation_log WHERE doctor_id = ? ORDER BY timestamp DESC";
        List<OperationLog> logs = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Retrieves logs related to a specific patient.
     */
    public List<OperationLog> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM operation_log WHERE patient_id = ? ORDER BY timestamp DESC";
        List<OperationLog> logs = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Retrieves all log entries.
     */
    public List<OperationLog> findAll() throws SQLException {
        String sql = "SELECT * FROM operation_log ORDER BY timestamp DESC";
        List<OperationLog> logs = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        }
        return logs;
    }

    private OperationLog mapResultSetToLog(ResultSet rs) throws SQLException {
        int patientId = rs.getInt("patient_id");
        Integer patientIdObj = rs.wasNull() ? null : patientId;
        return new OperationLog(
                rs.getInt("id"),
                rs.getInt("doctor_id"),
                patientIdObj,
                rs.getString("operation"),
                rs.getString("timestamp")
        );
    }
}
