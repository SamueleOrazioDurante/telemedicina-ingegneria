package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.ConcomitantCondition;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcomitantConditionDAO {
    private final DatabaseManager dbManager;

    public ConcomitantConditionDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a symptom or concomitant condition.
     */
    public void save(ConcomitantCondition condition) throws SQLException {
        if (condition.getId() == null) {
            String sql = "INSERT INTO concomitant_condition (patient_id, type, description, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, condition.getPatientId());
                pstmt.setString(2, condition.getType());
                pstmt.setString(3, condition.getDescription());
                pstmt.setString(4, condition.getStartDate());
                pstmt.setString(5, condition.getEndDate());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        condition.setId(rs.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE concomitant_condition SET patient_id = ?, type = ?, description = ?, " +
                    "start_date = ?, end_date = ? WHERE id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, condition.getPatientId());
                pstmt.setString(2, condition.getType());
                pstmt.setString(3, condition.getDescription());
                pstmt.setString(4, condition.getStartDate());
                pstmt.setString(5, condition.getEndDate());
                pstmt.setInt(6, condition.getId());
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Finds all conditions for a specific patient.
     */
    public List<ConcomitantCondition> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM concomitant_condition WHERE patient_id = ? ORDER BY start_date DESC";
        List<ConcomitantCondition> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCondition(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds conditions of a specific type (e.g. SYMPTOM, PATHOLOGY, CONCOMITANT_THERAPY) for a patient.
     */
    public List<ConcomitantCondition> findByPatientIdAndType(int patientId, String type) throws SQLException {
        String sql = "SELECT * FROM concomitant_condition WHERE patient_id = ? AND type = ? ORDER BY start_date DESC";
        List<ConcomitantCondition> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCondition(rs));
                }
            }
        }
        return list;
    }

    private ConcomitantCondition mapResultSetToCondition(ResultSet rs) throws SQLException {
        return new ConcomitantCondition(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("type"),
                rs.getString("description"),
                rs.getString("start_date"),
                rs.getString("end_date")
        );
    }
}
