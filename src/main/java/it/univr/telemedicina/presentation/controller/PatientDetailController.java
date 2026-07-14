package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.*;
import it.univr.telemedicina.persistence.*;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the patient details page viewed by a Doctor.
 * Shows patient card, measurements, therapies, conditions, trend analysis,
 * and email correspondence history in a single unified view.
 * "New Therapy" and "Edit Info" buttons launch modular modal dialogs.
 */
public class PatientDetailController {

    @FXML private Label titleLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label patientInfoLabel;
    @FXML private Label riskLabel;

    // Recent Glucose Measurements
    @FXML private TableView<BloodGlucoseMeasurement> glucoseTable;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gDateCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gTimeCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gValueCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gSlotCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gStatusCol;

    // Prescribed Therapies
    @FXML private TableView<PrescribedTherapy> therapyTable;
    @FXML private TableColumn<PrescribedTherapy, String> tDrugCol;
    @FXML private TableColumn<PrescribedTherapy, String> tDailyCol;
    @FXML private TableColumn<PrescribedTherapy, String> tQtyCol;
    @FXML private TableColumn<PrescribedTherapy, String> tDirCol;
    @FXML private TableColumn<PrescribedTherapy, String> tStartCol;
    @FXML private TableColumn<PrescribedTherapy, String> tStatusCol;

    // Reported Conditions
    @FXML private TableView<ConcomitantCondition> conditionTable;
    @FXML private TableColumn<ConcomitantCondition, String> cTypeCol;
    @FXML private TableColumn<ConcomitantCondition, String> cDescCol;
    @FXML private TableColumn<ConcomitantCondition, String> cStartCol;
    @FXML private TableColumn<ConcomitantCondition, String> cEndCol;

    // Trend Averages Table
    @FXML private ComboBox<String> periodCombo;
    @FXML private TableView<Map<String, String>> summaryTable;
    @FXML private TableColumn<Map<String, String>, String> periodCol;
    @FXML private TableColumn<Map<String, String>, String> avgBeforeCol;
    @FXML private TableColumn<Map<String, String>, String> avgAfterCol;
    @FXML private TableColumn<Map<String, String>, String> countCol;
    @FXML private TableColumn<Map<String, String>, String> alertsCol;

    private Patient patient;

    @FXML
    public void initialize() {
        periodCombo.setItems(FXCollections.observableArrayList("Weekly (last 12 weeks)", "Monthly (last 12 months)"));
        periodCombo.setValue("Weekly (last 12 weeks)");

        setupColumns();
    }

    private void setupColumns() {
        // Glucose history
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

        // Prescribed Therapies
        tDrugCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDrugName()));
        tDailyCol.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getDailyIntakes())));
        tQtyCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getQuantityPerIntake()));
        tDirCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDirections() != null ? cd.getValue().getDirections() : "-"));
        tStartCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStartDate()));
        tStatusCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isActive() ? "Active" : "Stopped"));

        tStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle("Active".equals(item) ? "-fx-text-fill: -color-accent;" : "-fx-text-fill: -color-text-muted;");
                }
            }
        });

        // Reported Conditions
        cTypeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getType()));
        cDescCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));
        cStartCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStartDate()));
        cEndCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEndDate() != null ? cd.getValue().getEndDate() : "Ongoing"));

        // Trend Averages
        periodCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("period")));
        avgBeforeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("avgBefore")));
        avgAfterCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("avgAfter")));
        countCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("count")));
        alertsCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("alerts")));
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        loadAllData();
    }

    private void loadAllData() {
        if (patient == null) return;

        titleLabel.setText("Patient Detail - " + patient.getFirstName() + " " + patient.getLastName());
        patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName() + " (DOB: " + patient.getDateOfBirth() + ")");
        
        StringBuilder info = new StringBuilder();
        info.append("Tax Code: ").append(patient.getTaxCode()).append("\n");
        info.append("Past Pathologies: ").append(patient.getPastPathologies() != null ? patient.getPastPathologies() : "None").append("\n");
        info.append("Comorbidities: ").append(patient.getComorbidities() != null ? patient.getComorbidities() : "None");
        patientInfoLabel.setText(info.toString());

        riskLabel.setText("Risk Factors: " + (patient.getRiskFactors() != null ? patient.getRiskFactors() : "None"));

        DatabaseManager db = SceneManager.getDbManager();
        Doctor doctor = SceneManager.getCurrentDoctor();

        try {
            // Load glucose measurements
            BloodGlucoseMeasurementDAO glucoseDAO = new BloodGlucoseMeasurementDAO(db);
            List<BloodGlucoseMeasurement> measurements = glucoseDAO.findByPatientId(patient.getId());
            glucoseTable.setItems(FXCollections.observableArrayList(measurements));

            // Load trends/averages
            loadTrends(measurements);

            // Load therapies
            PrescribedTherapyDAO therapyDAO = new PrescribedTherapyDAO(db);
            List<PrescribedTherapy> therapies = therapyDAO.findAllByPatientId(patient.getId());
            therapyTable.setItems(FXCollections.observableArrayList(therapies));

            // Load conditions
            ConcomitantConditionDAO conditionDAO = new ConcomitantConditionDAO(db);
            List<ConcomitantCondition> conditions = conditionDAO.findByPatientId(patient.getId());
            conditionTable.setItems(FXCollections.observableArrayList(conditions));



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTrends(List<BloodGlucoseMeasurement> measurements) {
        boolean weekly = periodCombo.getValue().startsWith("Weekly");
        List<Map<String, String>> rows = weekly ? computeWeekly(measurements) : computeMonthly(measurements);
        summaryTable.setItems(FXCollections.observableArrayList(rows));
    }

    private List<Map<String, String>> computeWeekly(List<BloodGlucoseMeasurement> all) {
        List<Map<String, String>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int w = 0; w < 12; w++) {
            LocalDate weekEnd = today.minusWeeks(w).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            LocalDate weekStart = weekEnd.minusDays(6);
            if (weekEnd.isAfter(today)) weekEnd = today;

            String startStr = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String endStr = weekEnd.format(DateTimeFormatter.ISO_LOCAL_DATE);

            List<BloodGlucoseMeasurement> weekData = all.stream()
                    .filter(m -> m.getDate().compareTo(startStr) >= 0 && m.getDate().compareTo(endStr) <= 0)
                    .collect(Collectors.toList());

            rows.add(buildRow(
                    weekStart.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + weekEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    weekData));
        }
        return rows;
    }

    private List<Map<String, String>> computeMonthly(List<BloodGlucoseMeasurement> all) {
        List<Map<String, String>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int m = 0; m < 12; m++) {
            LocalDate month = today.minusMonths(m);
            LocalDate monthStart = month.withDayOfMonth(1);
            LocalDate monthEnd = month.with(TemporalAdjusters.lastDayOfMonth());
            if (monthEnd.isAfter(today)) monthEnd = today;

            String startStr = monthStart.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String endStr = monthEnd.format(DateTimeFormatter.ISO_LOCAL_DATE);

            List<BloodGlucoseMeasurement> monthData = all.stream()
                    .filter(mg -> mg.getDate().compareTo(startStr) >= 0 && mg.getDate().compareTo(endStr) <= 0)
                    .collect(Collectors.toList());

            rows.add(buildRow(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), monthData));
        }
        return rows;
    }

    private Map<String, String> buildRow(String periodLabel, List<BloodGlucoseMeasurement> data) {
        Map<String, String> row = new HashMap<>();
        row.put("period", periodLabel);
        row.put("count", String.valueOf(data.size()));

        if (data.isEmpty()) {
            row.put("avgBefore", "-");
            row.put("avgAfter", "-");
            row.put("alerts", "0");
            return row;
        }

        double avgBefore = data.stream().filter(m -> "BEFORE_MEAL".equals(m.getTimeSlot()))
                .mapToDouble(BloodGlucoseMeasurement::getValue).average().orElse(0);
        double avgAfter = data.stream().filter(m -> "AFTER_MEAL".equals(m.getTimeSlot()))
                .mapToDouble(BloodGlucoseMeasurement::getValue).average().orElse(0);

        long abnormal = data.stream().filter(m -> {
            if ("BEFORE_MEAL".equals(m.getTimeSlot())) return m.getValue() < 80 || m.getValue() > 130;
            if ("AFTER_MEAL".equals(m.getTimeSlot())) return m.getValue() > 180;
            return false;
        }).count();

        row.put("avgBefore", avgBefore > 0 ? String.format("%.1f mg/dL", avgBefore) : "-");
        row.put("avgAfter", avgAfter > 0 ? String.format("%.1f mg/dL", avgAfter) : "-");
        row.put("alerts", String.valueOf(abnormal));
        return row;
    }

    @FXML
    protected void onRefreshTrends() {
        if (patient == null) return;
        try {
            BloodGlucoseMeasurementDAO glucoseDAO = new BloodGlucoseMeasurementDAO(SceneManager.getDbManager());
            List<BloodGlucoseMeasurement> measurements = glucoseDAO.findByPatientId(patient.getId());
            loadTrends(measurements);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Modal Triggers
    @FXML
    protected void onNewTherapy() {
        SceneManager.ModalResult<TherapyFormController> res = SceneManager.createModal("therapy-form.fxml", "Prescribe Therapy");
        if (res != null) {
            res.controller.setPatient(patient);
            res.stage.showAndWait();
            loadAllData();
        }
    }

    @FXML
    protected void onEditInfo() {
        SceneManager.ModalResult<PatientInfoEditController> res = SceneManager.createModal("patient-info-edit.fxml", "Edit Patient Info");
        if (res != null) {
            res.controller.setPatient(patient);
            res.stage.showAndWait();
            
            // Reload the patient details from database to reflect any changes in name/taxcode if edited
            // Note: PatientInfoEditController edits riskFactors, comorbidities, pastPathologies
            try {
                PatientDAO patientDAO = new PatientDAO(SceneManager.getDbManager());
                Patient updatedPatient = patientDAO.findByUsername(patient.getUsername());
                if (updatedPatient != null) {
                    this.patient = updatedPatient;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            loadAllData();
        }
    }

    @FXML
    protected void onBack() {
        SceneManager.switchScene("doctor-dashboard.fxml");
    }

    @FXML
    protected void onLogout() {
        SceneManager.logout();
    }
}
