package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.OperationLog;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.OperationLogDAO;
import it.univr.telemedicina.persistence.PatientDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for editing patient medical information (doctor side).
 * Allows updating risk factors, past pathologies, and comorbidities.
 * Each update is logged in the operation_log for audit tracking.
 */
public class PatientInfoEditController {

    @FXML private Label titleLabel;
    @FXML private Label patientLabel;
    @FXML private TextArea riskFactorsArea;
    @FXML private TextArea pastPathologiesArea;
    @FXML private TextArea comorbiditiesArea;
    @FXML private Label feedbackLabel;

    private Patient patient;

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            patientLabel.setText("Patient: " + patient.getFirstName() + " " + patient.getLastName() +
                    " (" + patient.getTaxCode() + ")");
            riskFactorsArea.setText(patient.getRiskFactors() != null ? patient.getRiskFactors() : "");
            pastPathologiesArea.setText(patient.getPastPathologies() != null ? patient.getPastPathologies() : "");
            comorbiditiesArea.setText(patient.getComorbidities() != null ? patient.getComorbidities() : "");
        }
    }

    @FXML
    protected void onSave() {
        Doctor doctor = SceneManager.getCurrentDoctor();
        if (doctor == null || patient == null) { SceneManager.logout(); return; }

        String riskFactors = riskFactorsArea.getText().trim();
        String pastPathologies = pastPathologiesArea.getText().trim();
        String comorbidities = comorbiditiesArea.getText().trim();

        try {
            PatientDAO patientDAO = new PatientDAO(SceneManager.getDbManager());
            patientDAO.updateMedicalInfo(patient.getId(),
                    riskFactors.isEmpty() ? null : riskFactors,
                    pastPathologies.isEmpty() ? null : pastPathologies,
                    comorbidities.isEmpty() ? null : comorbidities);

            // Update local object
            patient.setRiskFactors(riskFactors.isEmpty() ? null : riskFactors);
            patient.setPastPathologies(pastPathologies.isEmpty() ? null : pastPathologies);
            patient.setComorbidities(comorbidities.isEmpty() ? null : comorbidities);

            // Log the operation
            OperationLogDAO logDAO = new OperationLogDAO(SceneManager.getDbManager());
            logDAO.save(new OperationLog(doctor.getId(), patient.getId(),
                    "Updated patient medical info (risk factors, pathologies, comorbidities)",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            showFeedback("✅ Patient information updated successfully.", false);
        } catch (Exception e) {
            showFeedback("Error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onReset() {
        if (patient != null) {
            riskFactorsArea.setText(patient.getRiskFactors() != null ? patient.getRiskFactors() : "");
            pastPathologiesArea.setText(patient.getPastPathologies() != null ? patient.getPastPathologies() : "");
            comorbiditiesArea.setText(patient.getComorbidities() != null ? patient.getComorbidities() : "");
        }
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
