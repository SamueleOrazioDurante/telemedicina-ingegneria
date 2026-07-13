package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.DrugIntake;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DrugIntakeDAO {
    private final DatabaseManager dbManager;

    public DrugIntakeDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a drug intake.
     */
    public void save(DrugIntake intake) throws SQLException {
        if (intake.getId() == null) {
            String sql = "INSERT INTO drug_intake (patient_id, therapy_id, date, time, drug_name, quantity_taken) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, intake.getPatientId());
                pstmt.setInt(2, intake.getTherapyId());
                pstmt.setString(3, intake.getDate());
                pstmt.setString(4, intake.getTime());
                pstmt.setString(5, intake.getDrugName());
                pstmt.setString(6, intake.getQuantityTaken());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        intake.setId(rs.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE drug_intake SET patient_id = ?, therapy_id = ?, date = ?, time = ?, drug_name = ?, quantity_taken = ? WHERE id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, intake.getPatientId());
                pstmt.setInt(2, intake.getTherapyId());
                pstmt.setString(3, intake.getDate());
                pstmt.setString(4, intake.getTime());
                pstmt.setString(5, intake.getDrugName());
                pstmt.setString(6, intake.getQuantityTaken());
                pstmt.setInt(7, intake.getId());
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Finds drug intakes for a patient.
     */
    public List<DrugIntake> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM drug_intake WHERE patient_id = ? ORDER BY date ASC, time ASC";
        List<DrugIntake> intakes = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    intakes.add(mapResultSetToIntake(rs));
                }
            }
        }
        return intakes;
    }

    /**
     * Finds drug intakes for a patient on a specific date.
     */
    public List<DrugIntake> findByPatientIdAndDate(int patientId, String date) throws SQLException {
        String sql = "SELECT * FROM drug_intake WHERE patient_id = ? AND date = ? ORDER BY time ASC";
        List<DrugIntake> intakes = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    intakes.add(mapResultSetToIntake(rs));
                }
            }
        }
        return intakes;
    }

    /**
     * Finds drug intakes for a patient in a date range.
     */
    public List<DrugIntake> findByPatientIdAndPeriod(int patientId, String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM drug_intake WHERE patient_id = ? AND date >= ? AND date <= ? ORDER BY date ASC, time ASC";
        List<DrugIntake> intakes = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    intakes.add(mapResultSetToIntake(rs));
                }
            }
        }
        return intakes;
    }

    private DrugIntake mapResultSetToIntake(ResultSet rs) throws SQLException {
        return new DrugIntake(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("therapy_id"),
                rs.getString("date"),
                rs.getString("time"),
                rs.getString("drug_name"),
                rs.getString("quantity_taken")
        );
    }
}
