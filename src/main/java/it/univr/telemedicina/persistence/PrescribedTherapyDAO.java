package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.PrescribedTherapy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescribedTherapyDAO {
    private final DatabaseManager dbManager;

    public PrescribedTherapyDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves or updates a therapy.
     */
    public void save(PrescribedTherapy therapy) throws SQLException {
        if (therapy.getId() == null) {
            String sql = "INSERT INTO prescribed_therapy (patient_id, doctor_id, drug_name, daily_intakes, " +
                    "quantity_per_intake, directions, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, therapy.getPatientId());
                pstmt.setInt(2, therapy.getDoctorId());
                pstmt.setString(3, therapy.getDrugName());
                pstmt.setInt(4, therapy.getDailyIntakes());
                pstmt.setString(5, therapy.getQuantityPerIntake());
                pstmt.setString(6, therapy.getDirections());
                pstmt.setString(7, therapy.getStartDate());
                pstmt.setString(8, therapy.getEndDate());
                pstmt.setInt(9, therapy.isActive() ? 1 : 0);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        therapy.setId(rs.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE prescribed_therapy SET patient_id = ?, doctor_id = ?, drug_name = ?, " +
                    "daily_intakes = ?, quantity_per_intake = ?, directions = ?, " +
                    "start_date = ?, end_date = ?, is_active = ? WHERE id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, therapy.getPatientId());
                pstmt.setInt(2, therapy.getDoctorId());
                pstmt.setString(3, therapy.getDrugName());
                pstmt.setInt(4, therapy.getDailyIntakes());
                pstmt.setString(5, therapy.getQuantityPerIntake());
                pstmt.setString(6, therapy.getDirections());
                pstmt.setString(7, therapy.getStartDate());
                pstmt.setString(8, therapy.getEndDate());
                pstmt.setInt(9, therapy.isActive() ? 1 : 0);
                pstmt.setInt(10, therapy.getId());
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Finds a therapy by ID.
     */
    public PrescribedTherapy findById(int id) throws SQLException {
        String sql = "SELECT * FROM prescribed_therapy WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTherapy(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds active therapies for a patient.
     */
    public List<PrescribedTherapy> findActiveByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM prescribed_therapy WHERE patient_id = ? AND is_active = 1 ORDER BY start_date DESC";
        List<PrescribedTherapy> therapies = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    therapies.add(mapResultSetToTherapy(rs));
                }
            }
        }
        return therapies;
    }

    /**
     * Finds all therapies for a patient.
     */
    public List<PrescribedTherapy> findAllByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM prescribed_therapy WHERE patient_id = ? ORDER BY start_date DESC";
        List<PrescribedTherapy> therapies = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    therapies.add(mapResultSetToTherapy(rs));
                }
            }
        }
        return therapies;
    }

    /**
     * Updates active status and end date of a therapy.
     */
    public void updateTherapyStatus(int id, boolean active, String endDate) throws SQLException {
        String sql = "UPDATE prescribed_therapy SET is_active = ?, end_date = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, active ? 1 : 0);
            pstmt.setString(2, endDate);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        }
    }

    private PrescribedTherapy mapResultSetToTherapy(ResultSet rs) throws SQLException {
        return new PrescribedTherapy(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("drug_name"),
                rs.getInt("daily_intakes"),
                rs.getString("quantity_per_intake"),
                rs.getString("directions"),
                rs.getString("start_date"),
                rs.getString("end_date"),
                rs.getInt("is_active") == 1
        );
    }
}
