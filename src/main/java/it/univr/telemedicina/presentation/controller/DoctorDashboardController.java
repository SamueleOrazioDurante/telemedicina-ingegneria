package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.*;
import it.univr.telemedicina.logic.MedicalRulesEngine;
import it.univr.telemedicina.persistence.*;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the doctor's main dashboard.
 * Shows patient list, medical alerts for abnormal glucose and missed therapies,
 * and action buttons to view/manage each patient.
 */
public class DoctorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label alertCountLabel;
    @FXML private Label activeTherapiesLabel;
    @FXML private VBox alertsBox;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> nameCol;
    @FXML private TableColumn<Patient, String> taxCodeCol;
    @FXML private TableColumn<Patient, String> dobCol;
    @FXML private TableColumn<Patient, String> riskCol;
    @FXML private TableColumn<Patient, String> actionCol;

    private int alertCount = 0;

    @FXML
    public void initialize() {
        Doctor doctor = SceneManager.getCurrentDoctor();
        if (doctor == null) { SceneManager.logout(); return; }

        welcomeLabel.setText("Welcome, Dr. " + doctor.getLastName());
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));

        setupColumns();
        loadPatients(doctor);
        loadAlerts(doctor);
    }

    private void setupColumns() {
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFirstName() + " " + cd.getValue().getLastName()));
        taxCodeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTaxCode()));
        dobCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDateOfBirth()));
        riskCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getRiskFactors() != null ? cd.getValue().getRiskFactors() : "-"));

        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    Button viewBtn = new Button("View");
                    viewBtn.getStyleClass().add("button-primary");
                    viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 16;");
                    viewBtn.setOnAction(e -> openPatientDetail(p));
                    setGraphic(viewBtn);
                }
            }
        });
    }

    private void loadPatients(Doctor doctor) {
        try {
            PatientDAO dao = new PatientDAO(SceneManager.getDbManager());
            List<Patient> patients = dao.findAll(); // Doctor can see all patients per spec
            patientsTable.setItems(FXCollections.observableArrayList(patients));
            totalPatientsLabel.setText(String.valueOf(patients.size()));

            // Count active therapies across all patients
            PrescribedTherapyDAO therapyDAO = new PrescribedTherapyDAO(SceneManager.getDbManager());
            int totalActive = 0;
            for (Patient p : patients) {
                totalActive += therapyDAO.findActiveByPatientId(p.getId()).size();
            }
            activeTherapiesLabel.setText(String.valueOf(totalActive));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAlerts(Doctor doctor) {
        DatabaseManager db = SceneManager.getDbManager();
        alertCount = 0;

        try {
            PatientDAO patientDAO = new PatientDAO(db);
            BloodGlucoseMeasurementDAO glucoseDAO = new BloodGlucoseMeasurementDAO(db);
            PrescribedTherapyDAO therapyDAO = new PrescribedTherapyDAO(db);
            DrugIntakeDAO intakeDAO = new DrugIntakeDAO(db);

            List<Patient> patients = patientDAO.findAll();
            MedicalRulesEngine engine = new MedicalRulesEngine();
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            for (Patient p : patients) {
                // Check abnormal glucose in the last 3 days
                String threeDaysAgo = LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);
                List<BloodGlucoseMeasurement> recentGlucose = glucoseDAO.findByPatientIdAndPeriod(p.getId(), threeDaysAgo, today);
                for (BloodGlucoseMeasurement m : recentGlucose) {
                    if (engine.checkGlucoseThreshold(m)) {
                        addAlert("danger", "🔴 " + p.getFirstName() + " " + p.getLastName() +
                                ": Abnormal glucose " + m.getValue() + " mg/dL (" + m.getTimeSlot().replace("_", " ").toLowerCase() + ") on " + m.getDate());
                        alertCount++;
                    }
                }

                // Check missed therapies (3 consecutive days)
                List<PrescribedTherapy> activeTherapies = therapyDAO.findActiveByPatientId(p.getId());
                List<DrugIntake> intakes = intakeDAO.findByPatientId(p.getId());
                for (PrescribedTherapy therapy : activeTherapies) {
                    if (engine.checkMissingTherapy(intakes, therapy, LocalDate.now())) {
                        addAlert("danger", "🔴 " + p.getFirstName() + " " + p.getLastName() +
                                ": Missed therapy '" + therapy.getDrugName() + "' for 3+ consecutive days!");
                        alertCount++;
                    }
                }
            }

            if (alertCount == 0) {
                addAlert("success", "✅ No active alerts. All patients are on track.");
            }

            alertCountLabel.setText(String.valueOf(alertCount));

        } catch (Exception e) {
            addAlert("danger", "Error loading alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addAlert(String type, String message) {
        HBox badge = new HBox();
        badge.getStyleClass().add("alert-badge-" + type);
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 12px;");
        badge.getChildren().add(label);
        alertsBox.getChildren().add(badge);
    }

    private void openPatientDetail(Patient patient) {
        PatientDetailController ctrl = SceneManager.switchSceneAndGetController("patient-detail.fxml");
        if (ctrl != null) ctrl.setPatient(patient);
    }

    // --- Navigation ---
    @FXML protected void onDashboard() { SceneManager.switchScene("doctor-dashboard.fxml"); }
    @FXML protected void onLogout() { SceneManager.logout(); }
}
