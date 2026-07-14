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
import java.util.stream.Collectors;

/**
 * Main Controller for the Patient Dashboard.
 * Integrates dashboard stats, notifications, therapies list, glucose history,
 * and reported conditions into a single scrollable view.
 * All action forms are loaded as modal popup stages.
 */
public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label todayMeasurementsLabel;
    @FXML private Label activeTherapiesLabel;
    @FXML private Label todayIntakesLabel;
    @FXML private VBox alertsBox;

    // Prescribed Therapies Table
    @FXML private TableView<PrescribedTherapy> therapyTable;
    @FXML private TableColumn<PrescribedTherapy, String> tDrugCol;
    @FXML private TableColumn<PrescribedTherapy, String> tDailyCol;
    @FXML private TableColumn<PrescribedTherapy, String> tQtyCol;
    @FXML private TableColumn<PrescribedTherapy, String> tDirCol;
    @FXML private TableColumn<PrescribedTherapy, String> tStartCol;
    @FXML private TableColumn<PrescribedTherapy, String> tEndCol;
    @FXML private TableColumn<PrescribedTherapy, String> tStatusCol;

    // Glucose History Table
    @FXML private TableView<BloodGlucoseMeasurement> glucoseTable;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gDateCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gTimeCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gValueCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gSlotCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gStatusCol;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // Reported Conditions Table
    @FXML private TableView<ConcomitantCondition> conditionTable;
    @FXML private TableColumn<ConcomitantCondition, String> cTypeCol;
    @FXML private TableColumn<ConcomitantCondition, String> cDescCol;
    @FXML private TableColumn<ConcomitantCondition, String> cStartCol;
    @FXML private TableColumn<ConcomitantCondition, String> cEndCol;

    private List<BloodGlucoseMeasurement> allGlucoseMeasurements;

    @FXML
    public void initialize() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) {
            SceneManager.logout();
            return;
        }

        welcomeLabel.setText("Welcome, " + patient.getFirstName() + " " + patient.getLastName());
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));

        setupTherapyColumns();
        setupGlucoseColumns();
        setupConditionColumns();

        loadAllData(patient);
    }

    private void loadAllData(Patient patient) {
        loadStats(patient);
        loadAlerts(patient);
        loadTherapies(patient);
        loadGlucose(patient);
        loadConditions(patient);
    }

    private void loadStats(Patient patient) {
        DatabaseManager db = SceneManager.getDbManager();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            BloodGlucoseMeasurementDAO glucoseDAO = new BloodGlucoseMeasurementDAO(db);
            List<BloodGlucoseMeasurement> todayMeasurements = glucoseDAO.findByPatientIdAndPeriod(patient.getId(), today, today);
            todayMeasurementsLabel.setText(String.valueOf(todayMeasurements.size()));

            PrescribedTherapyDAO therapyDAO = new PrescribedTherapyDAO(db);
            List<PrescribedTherapy> activeTherapies = therapyDAO.findActiveByPatientId(patient.getId());
            activeTherapiesLabel.setText(String.valueOf(activeTherapies.size()));

            DrugIntakeDAO intakeDAO = new DrugIntakeDAO(db);
            List<DrugIntake> todayIntakes = intakeDAO.findByPatientIdAndDate(patient.getId(), today);
            todayIntakesLabel.setText(String.valueOf(todayIntakes.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAlerts(Patient patient) {
        DatabaseManager db = SceneManager.getDbManager();
        alertsBox.getChildren().clear();
        
        // Add Section Header
        Label header = new Label("Notifications & Alerts");
        header.getStyleClass().add("section-title");
        alertsBox.getChildren().add(header);

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
                            therapy.getDrugName() + ": " + remaining + " intake(s) remaining today (" +
                            intakesForTherapy + "/" + therapy.getDailyIntakes() + " completed)");
                    hasAlert = true;
                }
            }

            BloodGlucoseMeasurementDAO glucoseDAO = new BloodGlucoseMeasurementDAO(db);
            List<BloodGlucoseMeasurement> todayGlucose = glucoseDAO.findByPatientIdAndPeriod(patient.getId(), today, today);
            if (todayGlucose.isEmpty()) {
                addAlertBadge("info", "You haven't recorded any glucose measurements today.");
                hasAlert = true;
            }

            MedicalRulesEngine engine = new MedicalRulesEngine();
            for (BloodGlucoseMeasurement m : todayGlucose) {
                if (engine.checkGlucoseThreshold(m)) {
                    addAlertBadge("danger",
                            "Abnormal glucose: " + m.getValue() + " mg/dL (" + m.getTimeSlot().replace("_", " ").toLowerCase() + " at " + m.getTime() + ")");
                    hasAlert = true;
                }
            }

            if (!hasAlert) {
                addAlertBadge("success", "All good! No pending alerts.");
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

    private void setupTherapyColumns() {
        tDrugCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDrugName()));
        tDailyCol.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getDailyIntakes())));
        tQtyCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getQuantityPerIntake()));
        tDirCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDirections() != null ? cd.getValue().getDirections() : "-"));
        tStartCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStartDate()));
        tEndCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEndDate() != null ? cd.getValue().getEndDate() : "Ongoing"));
        tStatusCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isActive() ? "Active" : "Stopped"));

        tStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle(item.contains("Active") ? "-fx-text-fill: -color-accent;" : "-fx-text-fill: -color-text-muted;");
                }
            }
        });
    }

    private void loadTherapies(Patient patient) {
        try {
            PrescribedTherapyDAO dao = new PrescribedTherapyDAO(SceneManager.getDbManager());
            List<PrescribedTherapy> list = dao.findAllByPatientId(patient.getId());
            therapyTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupGlucoseColumns() {
        gDateCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate()));
        gTimeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTime()));
        gValueCol.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.1f", cd.getValue().getValue())));
        gSlotCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTimeSlot().replace("_", " ")));
        gStatusCol.setCellValueFactory(cd -> {
            BloodGlucoseMeasurement m = cd.getValue();
            boolean abnormal = ("BEFORE_MEAL".equals(m.getTimeSlot()) && (m.getValue() < 80 || m.getValue() > 130))
                    || ("AFTER_MEAL".equals(m.getTimeSlot()) && m.getValue() > 180);
            return new SimpleStringProperty(abnormal ? "Abnormal" : "Normal");
        });

        gStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle(item.contains("Abnormal") ? "-fx-text-fill: #f38ba8; -fx-font-weight: bold;" : "-fx-text-fill: -color-accent;");
                }
            }
        });
    }

    private void loadGlucose(Patient patient) {
        try {
            BloodGlucoseMeasurementDAO dao = new BloodGlucoseMeasurementDAO(SceneManager.getDbManager());
            allGlucoseMeasurements = dao.findByPatientId(patient.getId());
            glucoseTable.setItems(FXCollections.observableArrayList(allGlucoseMeasurements));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupConditionColumns() {
        cTypeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getType()));
        cDescCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));
        cStartCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStartDate()));
        cEndCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEndDate() != null ? cd.getValue().getEndDate() : "Ongoing"));
    }

    private void loadConditions(Patient patient) {
        try {
            ConcomitantConditionDAO dao = new ConcomitantConditionDAO(SceneManager.getDbManager());
            List<ConcomitantCondition> list = dao.findByPatientId(patient.getId());
            conditionTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onFilter() {
        if (allGlucoseMeasurements == null) return;

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start != null && end != null && end.isBefore(start)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Date Range");
            alert.setHeaderText(null);
            alert.setContentText("The start date must be before or equal to the end date.");
            alert.showAndWait();
            return;
        }

        List<BloodGlucoseMeasurement> filtered = allGlucoseMeasurements.stream().filter(m -> {
            LocalDate mDate = LocalDate.parse(m.getDate());
            if (start != null && mDate.isBefore(start)) return false;
            if (end != null && mDate.isAfter(end)) return false;
            return true;
        }).collect(Collectors.toList());

        glucoseTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    protected void onShowAll() {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        if (allGlucoseMeasurements != null) {
            glucoseTable.setItems(FXCollections.observableArrayList(allGlucoseMeasurements));
        }
    }

    // Modal Triggers
    @FXML
    protected void onRecordGlucose() {
        SceneManager.ModalResult<GlucoseEntryController> res = SceneManager.createModal("glucose-entry.fxml", "Record Glucose");
        if (res != null) {
            res.stage.showAndWait();
            loadAllData(SceneManager.getCurrentPatient());
        }
    }

    @FXML
    protected void onRecordDrugIntake() {
        SceneManager.ModalResult<DrugIntakeEntryController> res = SceneManager.createModal("drug-intake-entry.fxml", "Record Drug Intake");
        if (res != null) {
            res.stage.showAndWait();
            loadAllData(SceneManager.getCurrentPatient());
        }
    }

    @FXML
    protected void onReportCondition() {
        SceneManager.ModalResult<ConditionEntryController> res = SceneManager.createModal("condition-entry.fxml", "Report Condition");
        if (res != null) {
            res.stage.showAndWait();
            loadAllData(SceneManager.getCurrentPatient());
        }
    }

    @FXML
    protected void onEmailDoctor() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) return;
        try {
            DoctorDAO doctorDAO = new DoctorDAO(SceneManager.getDbManager());
            Doctor doctor = doctorDAO.findById(patient.getReferenceDoctorId());
            if (doctor != null && doctor.getEmail() != null && !doctor.getEmail().trim().isEmpty()) {
                String email = doctor.getEmail().trim();
                String subject = "Richiesta paziente: " + patient.getFirstName() + " " + patient.getLastName();
                String mailtoUri = "mailto:" + email + "?subject=" + java.net.URLEncoder.encode(subject, "UTF-8").replace("+", "%20");
                SceneManager.showDocument(mailtoUri);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Reference doctor email is not configured.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading reference doctor details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void onLogout() {
        SceneManager.logout();
    }
}
