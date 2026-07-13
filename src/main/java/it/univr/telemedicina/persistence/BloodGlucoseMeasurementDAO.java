package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.BloodGlucoseMeasurement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BloodGlucoseMeasurementDAO {
    private final DatabaseManager dbManager;

    public BloodGlucoseMeasurementDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a blood glucose measurement.
     */
    public void save(BloodGlucoseMeasurement measurement) throws SQLException {
        if (measurement.getId() == null) {
            String sql = "INSERT INTO blood_glucose_measurement (patient_id, value, time_slot, date, time) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, measurement.getPatientId());
                pstmt.setDouble(2, measurement.getValue());
                pstmt.setString(3, measurement.getTimeSlot());
                pstmt.setString(4, measurement.getDate());
                pstmt.setString(5, measurement.getTime());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        measurement.setId(rs.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE blood_glucose_measurement SET patient_id = ?, value = ?, time_slot = ?, date = ?, time = ? WHERE id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, measurement.getPatientId());
                pstmt.setDouble(2, measurement.getValue());
                pstmt.setString(3, measurement.getTimeSlot());
                pstmt.setString(4, measurement.getDate());
                pstmt.setString(5, measurement.getTime());
                pstmt.setInt(6, measurement.getId());
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Retrieves all blood glucose measurements for a specific patient.
     */
    public List<BloodGlucoseMeasurement> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM blood_glucose_measurement WHERE patient_id = ? ORDER BY date ASC, time ASC";
        List<BloodGlucoseMeasurement> measurements = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(mapResultSetToMeasurement(rs));
                }
            }
        }
        return measurements;
    }

    /**
     * Retrieves measurements for a specific patient within a date range (YYYY-MM-DD).
     */
    public List<BloodGlucoseMeasurement> findByPatientIdAndPeriod(int patientId, String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM blood_glucose_measurement WHERE patient_id = ? AND date >= ? AND date <= ? ORDER BY date ASC, time ASC";
        List<BloodGlucoseMeasurement> measurements = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(mapResultSetToMeasurement(rs));
                }
            }
        }
        return measurements;
    }

    private BloodGlucoseMeasurement mapResultSetToMeasurement(ResultSet rs) throws SQLException {
        return new BloodGlucoseMeasurement(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getDouble("value"),
                rs.getString("time_slot"),
                rs.getString("date"),
                rs.getString("time")
        );
    }
}
