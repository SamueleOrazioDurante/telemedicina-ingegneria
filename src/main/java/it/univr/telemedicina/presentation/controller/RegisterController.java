package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.DatabaseManager;
import it.univr.telemedicina.persistence.DoctorDAO;
import it.univr.telemedicina.persistence.PatientDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RegisterController {

    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField taxCodeField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    // Patient specific fields
    @FXML private VBox patientFieldsBox;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private TextField riskFactorsField;
    @FXML private TextField pastPathologiesField;
    @FXML private TextField comorbiditiesField;

    @FXML private Label feedbackLabel;

    private List<Doctor> doctorList;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("Doctor", "Patient"));
        roleCombo.setValue("Patient");

        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isPatient = "Patient".equals(newVal);
            patientFieldsBox.setVisible(isPatient);
            patientFieldsBox.setManaged(isPatient);
        });

        // Set listener for patient fields box initial state
        patientFieldsBox.setVisible(true);
        patientFieldsBox.setManaged(true);

        setupDoctorCombo();
    }

    private void setupDoctorCombo() {
        DatabaseManager dbManager = SceneManager.getDbManager();
        try {
            DoctorDAO doctorDAO = new DoctorDAO(dbManager);
            doctorList = doctorDAO.findAll();
            doctorCombo.setItems(FXCollections.observableArrayList(doctorList));

            doctorCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Doctor doc) {
                    if (doc == null) return "";
                    return "Dr. " + doc.getFirstName() + " " + doc.getLastName();
                }

                @Override
                public Doctor fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showFeedback("Error loading doctors: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRegister() {
        String role = roleCombo.getValue();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String taxCode = taxCodeField.getText().trim().toUpperCase();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || taxCode.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showFeedback("Please fill in all required fields marked with *.", true);
            return;
        }

        DatabaseManager dbManager = SceneManager.getDbManager();

        try {
            if ("Doctor".equals(role)) {
                DoctorDAO doctorDAO = new DoctorDAO(dbManager);
                // Check if username/taxcode exists
                if (doctorDAO.findByUsername(username) != null) {
                    showFeedback("Username is already taken.", true);
                    return;
                }
                
                Doctor doctor = new Doctor(null, taxCode, firstName, lastName, username, password);
                doctorDAO.save(doctor);
                showFeedback("Registration successful! Please login.", false);
                clearFields();
            } else {
                // Patient
                LocalDate dob = dobPicker.getValue();
                Doctor refDoc = doctorCombo.getValue();

                if (dob == null) {
                    showFeedback("Please select a date of birth.", true);
                    return;
                }
                if (refDoc == null) {
                    showFeedback("Please select a reference doctor.", true);
                    return;
                }

                PatientDAO patientDAO = new PatientDAO(dbManager);
                if (patientDAO.findByUsername(username) != null) {
                    showFeedback("Username is already taken.", true);
                    return;
                }

                String dobStr = dob.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String riskFactors = riskFactorsField.getText().trim();
                String pastPathologies = pastPathologiesField.getText().trim();
                String comorbidities = comorbiditiesField.getText().trim();

                Patient patient = new Patient(
                        null,
                        taxCode,
                        firstName,
                        lastName,
                        dobStr,
                        username,
                        password,
                        refDoc.getId(),
                        riskFactors.isEmpty() ? null : riskFactors,
                        pastPathologies.isEmpty() ? null : pastPathologies,
                        comorbidities.isEmpty() ? null : comorbidities
                );

                patientDAO.save(patient);
                showFeedback("Registration successful! Please login.", false);
                clearFields();
            }
        } catch (SQLException e) {
            showFeedback("Database error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onBackToLogin() {
        SceneManager.switchScene("login-view.fxml");
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(isError ? "-fx-text-fill: #f38ba8; -fx-font-size: 13px;" : "-fx-text-fill: -color-accent; -fx-font-size: 13px;");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        taxCodeField.clear();
        usernameField.clear();
        passwordField.clear();
        dobPicker.setValue(null);
        doctorCombo.setValue(null);
        riskFactorsField.clear();
        pastPathologiesField.clear();
        comorbiditiesField.clear();
    }
}
