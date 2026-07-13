package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.ConcomitantCondition;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.ConcomitantConditionDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the condition/symptom reporting form.
 * Patients can report symptoms (e.g. headache, nausea), concomitant pathologies,
 * or concurrent therapies with start/end dates.
 */
public class ConditionEntryController {

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label feedbackLabel;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("SYMPTOM", "PATHOLOGY", "CONCOMITANT_THERAPY"));
        startDatePicker.setValue(LocalDate.now());
    }

    @FXML
    protected void onSave() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        String type = typeCombo.getValue();
        if (type == null) { showFeedback("Please select a type.", true); return; }

        String desc = descriptionArea.getText().trim();
        if (desc.isEmpty()) { showFeedback("Please enter a description.", true); return; }

        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) { showFeedback("Please select a start date.", true); return; }

        LocalDate endDate = endDatePicker.getValue();
        String endDateStr = endDate != null ? endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;

        try {
            ConcomitantCondition condition = new ConcomitantCondition(
                    patient.getId(), type, desc,
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE), endDateStr);

            ConcomitantConditionDAO dao = new ConcomitantConditionDAO(SceneManager.getDbManager());
            dao.save(condition);

            showFeedback("✅ Condition reported successfully: " + desc, false);
            descriptionArea.clear();
        } catch (Exception e) {
            showFeedback("Error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClear() {
        typeCombo.setValue(null);
        descriptionArea.clear();
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

    // --- Navigation ---
    @FXML protected void onDashboard() { SceneManager.switchScene("patient-dashboard.fxml"); }
    @FXML protected void onRecordGlucose() { SceneManager.switchScene("glucose-entry.fxml"); }
    @FXML protected void onGlucoseHistory() { SceneManager.switchScene("glucose-history.fxml"); }
    @FXML protected void onRecordDrugIntake() { SceneManager.switchScene("drug-intake-entry.fxml"); }
    @FXML protected void onReportCondition() { SceneManager.switchScene("condition-entry.fxml"); }
    @FXML protected void onMyTherapies() { SceneManager.switchScene("patient-therapies.fxml"); }
    @FXML protected void onEmailDoctor() { SceneManager.switchScene("send-email.fxml"); }
    @FXML protected void onLogout() { SceneManager.logout(); }
}
