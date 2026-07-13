package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.OperationLog;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.domain.PrescribedTherapy;
import it.univr.telemedicina.persistence.OperationLogDAO;
import it.univr.telemedicina.persistence.PrescribedTherapyDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the therapy prescription form (doctor side).
 * Allows a doctor to prescribe a new therapy for a patient,
 * specifying drug, dosage, frequency, directions, and dates.
 * Saves an operation log for audit tracking.
 */
public class TherapyFormController {

    @FXML private Label titleLabel;
    @FXML private Label patientLabel;
    @FXML private TextField drugNameField;
    @FXML private Spinner<Integer> dailyIntakesSpinner;
    @FXML private TextField quantityField;
    @FXML private TextArea directionsArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label feedbackLabel;

    private Patient patient;

    @FXML
    public void initialize() {
        dailyIntakesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        startDatePicker.setValue(LocalDate.now());
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            patientLabel.setText("Patient: " + patient.getFirstName() + " " + patient.getLastName() +
                    " (" + patient.getTaxCode() + ")");
        }
    }

    @FXML
    protected void onSave() {
        Doctor doctor = SceneManager.getCurrentDoctor();
        if (doctor == null || patient == null) { SceneManager.logout(); return; }

        String drugName = drugNameField.getText().trim();
        if (drugName.isEmpty()) { showFeedback("Please enter a drug name.", true); return; }

        String quantity = quantityField.getText().trim();
        if (quantity.isEmpty()) { showFeedback("Please enter quantity per intake.", true); return; }

        int dailyIntakes = dailyIntakesSpinner.getValue();
        String directions = directionsArea.getText().trim();
        if (directions.isEmpty()) directions = null;

        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) { showFeedback("Please select a start date.", true); return; }

        LocalDate endDate = endDatePicker.getValue();
        String endDateStr = endDate != null ? endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;

        try {
            PrescribedTherapy therapy = new PrescribedTherapy(
                    patient.getId(), doctor.getId(), drugName, dailyIntakes,
                    quantity, directions, startDate.format(DateTimeFormatter.ISO_LOCAL_DATE), endDateStr);

            PrescribedTherapyDAO therapyDAO = new PrescribedTherapyDAO(SceneManager.getDbManager());
            therapyDAO.save(therapy);

            // Log the operation
            OperationLogDAO logDAO = new OperationLogDAO(SceneManager.getDbManager());
            logDAO.save(new OperationLog(doctor.getId(), patient.getId(),
                    "Prescribed therapy: " + drugName + " (" + quantity + " x" + dailyIntakes + "/day)",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            showFeedback("✅ Therapy prescribed successfully: " + drugName, false);
            drugNameField.clear();
            quantityField.clear();
            directionsArea.clear();

        } catch (Exception e) {
            showFeedback("Error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClear() {
        drugNameField.clear();
        dailyIntakesSpinner.getValueFactory().setValue(1);
        quantityField.clear();
        directionsArea.clear();
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(null);
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(isError ? "-fx-text-fill: #f38ba8; -fx-font-size: 13px;" : "-fx-text-fill: #a6e3a1; -fx-font-size: 13px;");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    @FXML protected void onBack() { SceneManager.switchScene("doctor-dashboard.fxml"); }
    @FXML protected void onLogout() { SceneManager.logout(); }
}
