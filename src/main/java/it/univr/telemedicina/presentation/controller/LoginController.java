package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.DatabaseManager;
import it.univr.telemedicina.persistence.DoctorDAO;
import it.univr.telemedicina.persistence.PatientDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the login screen.
 * Authenticates users (doctors or patients) and redirects to the appropriate dashboard.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    protected void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        DatabaseManager dbManager = SceneManager.getDbManager();

        try {
            // Try doctor login first
            DoctorDAO doctorDAO = new DoctorDAO(dbManager);
            Doctor doctor = doctorDAO.findByUsername(username);
            if (doctor != null && doctor.getPassword().equals(password)) {
                SceneManager.setCurrentUser(doctor);
                SceneManager.switchScene("doctor-dashboard.fxml");
                return;
            }

            // Try patient login
            PatientDAO patientDAO = new PatientDAO(dbManager);
            Patient patient = patientDAO.findByUsername(username);
            if (patient != null && patient.getPassword().equals(password)) {
                SceneManager.setCurrentUser(patient);
                SceneManager.switchScene("patient-dashboard.fxml");
                return;
            }

            showError("Invalid username or password.");
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
