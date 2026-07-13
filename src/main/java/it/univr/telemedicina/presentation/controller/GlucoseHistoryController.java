package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.BloodGlucoseMeasurement;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.BloodGlucoseMeasurementDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the glucose measurement history view.
 * Displays a table of all past measurements with date range filtering and
 * a visual status indicator (normal/abnormal).
 */
public class GlucoseHistoryController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<BloodGlucoseMeasurement> glucoseTable;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> dateCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> timeCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> valueCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> timeSlotCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> statusCol;

    @FXML
    public void initialize() {
        setupColumns();
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        loadData(null, null);
    }

    private void setupColumns() {
        dateCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate()));
        timeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTime()));
        valueCol.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.1f", cd.getValue().getValue())));
        timeSlotCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getTimeSlot().replace("_", " ")));
        statusCol.setCellValueFactory(cd -> {
            BloodGlucoseMeasurement m = cd.getValue();
            boolean abnormal = false;
            if ("BEFORE_MEAL".equals(m.getTimeSlot())) {
                abnormal = m.getValue() < 80 || m.getValue() > 130;
            } else if ("AFTER_MEAL".equals(m.getTimeSlot())) {
                abnormal = m.getValue() > 180;
            }
            return new SimpleStringProperty(abnormal ? "⚠️ Abnormal" : "✅ Normal");
        });

        // Color the status column
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.contains("Abnormal") ? "-fx-text-fill: #f38ba8;" : "-fx-text-fill: #a6e3a1;");
                }
            }
        });
    }

    private void loadData(String startDate, String endDate) {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        try {
            BloodGlucoseMeasurementDAO dao = new BloodGlucoseMeasurementDAO(SceneManager.getDbManager());
            List<BloodGlucoseMeasurement> measurements;
            if (startDate != null && endDate != null) {
                measurements = dao.findByPatientIdAndPeriod(patient.getId(), startDate, endDate);
            } else {
                measurements = dao.findByPatientId(patient.getId());
            }
            glucoseTable.setItems(FXCollections.observableArrayList(measurements));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onFilter() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start != null && end != null) {
            loadData(start.format(DateTimeFormatter.ISO_LOCAL_DATE), end.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    @FXML
    protected void onShowAll() {
        loadData(null, null);
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
