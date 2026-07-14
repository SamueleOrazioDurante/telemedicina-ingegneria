package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.BloodGlucoseMeasurement;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.logic.MedicalRulesEngine;
import it.univr.telemedicina.persistence.BloodGlucoseMeasurementDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the glucose measurement entry form.
 * Validates input, saves measurement, and triggers threshold checks via MedicalRulesEngine.
 */
public class GlucoseEntryController {

    @FXML private TextField glucoseValueField;
    @FXML private ComboBox<String> timeSlotCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private Label feedbackLabel;

    @FXML
    public void initialize() {
        timeSlotCombo.setItems(FXCollections.observableArrayList("BEFORE_MEAL", "AFTER_MEAL"));
        datePicker.setValue(LocalDate.now());
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    protected void onSave() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        // Validate
        String valueStr = glucoseValueField.getText().trim();
        if (valueStr.isEmpty()) { showFeedback("Please enter a glucose value.", true); return; }

        double value;
        try {
            value = Double.parseDouble(valueStr);
            if (value <= 0 || value > 600) { showFeedback("Glucose value must be between 1 and 600 mg/dL.", true); return; }
        } catch (NumberFormatException e) {
            showFeedback("Invalid numeric value.", true);
            return;
        }

        String timeSlot = timeSlotCombo.getValue();
        if (timeSlot == null) { showFeedback("Please select a time slot.", true); return; }

        LocalDate date = datePicker.getValue();
        if (date == null) { showFeedback("Please select a date.", true); return; }

        String time = timeField.getText().trim();
        if (!time.matches("\\d{2}:\\d{2}")) { showFeedback("Time must be in HH:MM format.", true); return; }

        try {
            BloodGlucoseMeasurement measurement = new BloodGlucoseMeasurement(
                    patient.getId(), value, timeSlot, date.format(DateTimeFormatter.ISO_LOCAL_DATE), time);

            BloodGlucoseMeasurementDAO dao = new BloodGlucoseMeasurementDAO(SceneManager.getDbManager());
            dao.save(measurement);

            // Check thresholds
            MedicalRulesEngine engine = new MedicalRulesEngine();
            boolean alert = engine.checkGlucoseThreshold(measurement);

            if (alert) {
                showFeedback("Measurement saved. WARNING: Abnormal glucose level detected (" + value + " mg/dL, " +
                        timeSlot.replace("_", " ").toLowerCase() + "). Your doctor will be notified.", true);
                feedbackLabel.setStyle("-fx-text-fill: #f9e2af; -fx-font-size: 13px; -fx-font-weight: bold;");
            } else {
                showFeedback("Measurement saved successfully (" + value + " mg/dL).", false);
            }

            glucoseValueField.clear();

        } catch (Exception e) {
            showFeedback("Error saving measurement: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClear() {
        glucoseValueField.clear();
        timeSlotCombo.setValue(null);
        datePicker.setValue(LocalDate.now());
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(isError ? "-fx-text-fill: #f38ba8; -fx-font-size: 13px;" : "-fx-text-fill: -color-accent; -fx-font-size: 13px;");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    @FXML
    protected void onClose() {
        if (feedbackLabel.getScene() != null && feedbackLabel.getScene().getWindow() != null) {
            ((javafx.stage.Stage) feedbackLabel.getScene().getWindow()).close();
        }
    }
}
