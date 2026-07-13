package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.*;
import it.univr.telemedicina.logic.MedicalRulesEngine;
import it.univr.telemedicina.persistence.*;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the patient's main dashboard.
 * Shows summary statistics, alerts for pending drug intakes, and navigation actions.
 */
public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label todayMeasurementsLabel;
    @FXML private Label activeTherapiesLabel;
    @FXML private Label todayIntakesLabel;
    @FXML private VBox alertsBox;

    @FXML
    public void initialize() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) {
            SceneManager.logout();
            return;
        }

        welcomeLabel.setText("Welcome, " + patient.getFirstName() + " " + patient.getLastName());
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));

        loadStats(patient);
        loadAlerts(patient);
    }

    private void loadStats(Patient patient) {
        DatabaseManager db = SceneManager.getDbManager();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            // Today's measurements
            BloodGlucoseMeasurementDAO glucoseDAO = new BloodGlucoseMeasurementDAO(db);
            List<BloodGlucoseMeasurement> todayMeasurements = glucoseDAO.findByPatientIdAndPeriod(patient.getId(), today, today);
            todayMeasurementsLabel.setText(String.valueOf(todayMeasurements.size()));

            // Active therapies
            PrescribedTherapyDAO therapyDAO = new PrescribedTherapyDAO(db);
            List<PrescribedTherapy> activeTherapies = therapyDAO.findActiveByPatientId(patient.getId());
            activeTherapiesLabel.setText(String.valueOf(activeTherapies.size()));

            // Today's intakes
            DrugIntakeDAO intakeDAO = new DrugIntakeDAO(db);
            List<DrugIntake> todayIntakes = intakeDAO.findByPatientIdAndDate(patient.getId(), today);
            todayIntakesLabel.setText(String.valueOf(todayIntakes.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAlerts(Patient patient) {
        DatabaseManager db = SceneManager.getDbManager();

        try {
            PrescribedTherapyDAO therapyDAO = new PrescribedTherapyDAO(db);
            DrugIntakeDAO intakeDAO = new DrugIntakeDAO(db);
            List<PrescribedTherapy> activeTherapies = therapyDAO.findActiveByPatientId(patient.getId());
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<DrugIntake> todayIntakes = intakeDAO.findByPatientIdAndDate(patient.getId(), today);

            boolean hasAlert = false;

            for (PrescribedTherapy therapy : activeTherapies) {
                long intakesForTherapy = todayIntakes.stream()
                        .filter(i -> i.getTherapyId().equals(therapy.getId()))
                        .count();

                if (intakesForTherapy < therapy.getDailyIntakes()) {
                    long remaining = therapy.getDailyIntakes() - intakesForTherapy;
                    addAlertBadge("warning",
                            "⚠️ " + therapy.getDrugName() + ": " + remaining + " intake(s) remaining today (" +
                            intakesForTherapy + "/" + therapy.getDailyIntakes() + " completed)");
                    hasAlert = true;
                }
            }

            // Check glucose measurements today
            BloodGlucoseMeasurementDAO glucoseDAO = new BloodGlucoseMeasurementDAO(db);
            List<BloodGlucoseMeasurement> todayGlucose = glucoseDAO.findByPatientIdAndPeriod(patient.getId(), today, today);
            if (todayGlucose.isEmpty()) {
                addAlertBadge("info", "ℹ️ You haven't recorded any glucose measurements today.");
                hasAlert = true;
            }

            // Check for abnormal glucose in recent measurements
            MedicalRulesEngine engine = new MedicalRulesEngine();
            for (BloodGlucoseMeasurement m : todayGlucose) {
                if (engine.checkGlucoseThreshold(m)) {
                    addAlertBadge("danger",
                            "🔴 Abnormal glucose: " + m.getValue() + " mg/dL (" + m.getTimeSlot().replace("_", " ").toLowerCase() + " at " + m.getTime() + ")");
                    hasAlert = true;
                }
            }

            if (!hasAlert) {
                addAlertBadge("success", "✅ All good! No pending alerts.");
            }

        } catch (Exception e) {
            addAlertBadge("danger", "Error loading alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addAlertBadge(String type, String message) {
        HBox badge = new HBox();
        badge.getStyleClass().add("alert-badge-" + type);
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 13px;");
        badge.getChildren().add(label);
        alertsBox.getChildren().add(badge);
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
