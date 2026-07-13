package it.univr.telemedicina.persistence;

import it.univr.telemedicina.domain.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {
    private final DatabaseManager dbManager;

    public DoctorDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves or updates a doctor.
     */
    public void save(Doctor doctor) throws SQLException {
        if (doctor.getId() == null) {
            String sql = "INSERT INTO doctor (tax_code, first_name, last_name, username, password) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, doctor.getTaxCode());
                pstmt.setString(2, doctor.getFirstName());
                pstmt.setString(3, doctor.getLastName());
                pstmt.setString(4, doctor.getUsername());
                pstmt.setString(5, doctor.getPassword());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        doctor.setId(rs.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE doctor SET tax_code = ?, first_name = ?, last_name = ?, username = ?, password = ? WHERE id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, doctor.getTaxCode());
                pstmt.setString(2, doctor.getFirstName());
                pstmt.setString(3, doctor.getLastName());
                pstmt.setString(4, doctor.getUsername());
                pstmt.setString(5, doctor.getPassword());
                pstmt.setInt(6, doctor.getId());
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Finds a doctor by ID.
     */
    public Doctor findById(int id) throws SQLException {
        String sql = "SELECT * FROM doctor WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds a doctor by username.
     */
    public Doctor findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM doctor WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all doctors.
     */
    public List<Doctor> findAll() throws SQLException {
        String sql = "SELECT * FROM doctor";
        List<Doctor> doctors = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                doctors.add(mapResultSetToDoctor(rs));
            }
        }
        return doctors;
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("tax_code"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("username"),
                rs.getString("password")
        );
    }
}
