package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.BloodGlucoseMeasurement;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.BloodGlucoseMeasurementDAO;
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
 * Controller for the glucose chart/trend view (doctor side).
 * Shows synthetic data: weekly or monthly average glucose values,
 * measurement count, and abnormal reading count for a given patient.
 */
public class GlucoseChartController {

    @FXML private Label titleLabel;
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

        periodCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("period")));
        avgBeforeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("avgBefore")));
        avgAfterCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("avgAfter")));
        countCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("count")));
        alertsCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("alerts")));
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            titleLabel.setText("Glucose Trend — " + patient.getFirstName() + " " + patient.getLastName());
            loadData();
        }
    }

    @FXML
    protected void onRefresh() {
        loadData();
    }

    private void loadData() {
        if (patient == null) return;

        try {
            BloodGlucoseMeasurementDAO dao = new BloodGlucoseMeasurementDAO(SceneManager.getDbManager());
            List<BloodGlucoseMeasurement> all = dao.findByPatientId(patient.getId());

            boolean weekly = periodCombo.getValue().startsWith("Weekly");
            List<Map<String, String>> rows = weekly ? computeWeekly(all) : computeMonthly(all);
            summaryTable.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            row.put("avgBefore", "—");
            row.put("avgAfter", "—");
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

        row.put("avgBefore", avgBefore > 0 ? String.format("%.1f mg/dL", avgBefore) : "—");
        row.put("avgAfter", avgAfter > 0 ? String.format("%.1f mg/dL", avgAfter) : "—");
        row.put("alerts", String.valueOf(abnormal));
        return row;
    }

    @FXML protected void onBack() { SceneManager.switchScene("doctor-dashboard.fxml"); }
    @FXML protected void onLogout() { SceneManager.logout(); }
}
