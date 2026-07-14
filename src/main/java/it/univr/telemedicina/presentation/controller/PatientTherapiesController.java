package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.domain.PrescribedTherapy;
import it.univr.telemedicina.persistence.PrescribedTherapyDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Controller for the patient's therapy list view.
 * Shows all therapies (active and historical) with status indication.
 */
public class PatientTherapiesController {

    @FXML private TableView<PrescribedTherapy> therapyTable;
    @FXML private TableColumn<PrescribedTherapy, String> drugCol;
    @FXML private TableColumn<PrescribedTherapy, String> dailyCol;
    @FXML private TableColumn<PrescribedTherapy, String> quantityCol;
    @FXML private TableColumn<PrescribedTherapy, String> directionsCol;
    @FXML private TableColumn<PrescribedTherapy, String> startCol;
    @FXML private TableColumn<PrescribedTherapy, String> endCol;
    @FXML private TableColumn<PrescribedTherapy, String> statusCol;

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        drugCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDrugName()));
        dailyCol.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getDailyIntakes())));
        quantityCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getQuantityPerIntake()));
        directionsCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDirections() != null ? cd.getValue().getDirections() : "-"));
        startCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStartDate()));
        endCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEndDate() != null ? cd.getValue().getEndDate() : "Ongoing"));
        statusCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isActive() ? "✅ Active" : "⛔ Stopped"));

        statusCol.setCellFactory(col -> new TableCell<>() {
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

    private void loadData() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        try {
            PrescribedTherapyDAO dao = new PrescribedTherapyDAO(SceneManager.getDbManager());
            List<PrescribedTherapy> therapies = dao.findAllByPatientId(patient.getId());
            therapyTable.setItems(FXCollections.observableArrayList(therapies));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
