package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.DoctorDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the "Send Email to Doctor" form.
 * Since the spec requires email functionality, this simulates it by printing
 * the message to console and showing a success confirmation.
 * In a real system, this would integrate with an email/SMTP service.
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

        // Simulate email sending
        System.out.println("=== EMAIL SENT ===");
        System.out.println("From: " + patient.getFirstName() + " " + patient.getLastName() + " (Patient ID: " + patient.getId() + ")");
        System.out.println("To: " + recipientField.getText());
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("==================");

        showFeedback("✅ Message sent successfully to " + recipientField.getText() + ".", false);
        subjectField.clear();
        messageArea.clear();
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
