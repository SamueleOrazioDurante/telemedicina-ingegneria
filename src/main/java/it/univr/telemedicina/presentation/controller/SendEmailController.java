package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.domain.PatientMessage;
import it.univr.telemedicina.persistence.DoctorDAO;
import it.univr.telemedicina.persistence.PatientMessageDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the "Send Email to Doctor" form.
 * Saves the messages/emails directly into the database.
 */
public class SendEmailController {

    @FXML private TextField recipientField;
    @FXML private TextField subjectField;
    @FXML private TextArea messageArea;
    @FXML private Label feedbackLabel;

    @FXML
    public void initialize() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        try {
            DoctorDAO dao = new DoctorDAO(SceneManager.getDbManager());
            Doctor doctor = dao.findById(patient.getReferenceDoctorId());
            if (doctor != null) {
                recipientField.setText("Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
            }
        } catch (Exception e) {
            recipientField.setText("Doctor not found");
        }
    }

    @FXML
    protected void onSend() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) { showFeedback("Please enter a subject.", true); return; }

        String message = messageArea.getText().trim();
        if (message.isEmpty()) { showFeedback("Please enter a message.", true); return; }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        try {
            PatientMessage msg = new PatientMessage(
                    patient.getId(),
                    patient.getReferenceDoctorId(),
                    subject,
                    message,
                    dateStr,
                    timeStr
            );
            PatientMessageDAO dao = new PatientMessageDAO(SceneManager.getDbManager());
            dao.save(msg);

            showFeedback("✅ Message sent successfully to " + recipientField.getText() + ".", false);
            subjectField.clear();
            messageArea.clear();
        } catch (Exception e) {
            showFeedback("Error sending message: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClear() {
        subjectField.clear();
        messageArea.clear();
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
