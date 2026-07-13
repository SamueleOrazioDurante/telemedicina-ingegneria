package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.*;
import it.univr.telemedicina.persistence.*;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Controller for the patient detail view (doctor side).
 * Shows comprehensive patient data: personal info, glucose history,
 * therapies, and reported conditions.
 */
public class PatientDetailController {

    @FXML private Label titleLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label patientInfoLabel;
    @FXML private Label riskLabel;

    @FXML private TableView<BloodGlucoseMeasurement> glucoseTable;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gDateCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gTimeCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gValueCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gSlotCol;
    @FXML private TableColumn<BloodGlucoseMeasurement, String> gStatusCol;

    @FXML private TableView<PrescribedTherapy> therapyTable;
    @FXML private TableColumn<PrescribedTherapy, String> tDrugCol;
    @FXML private TableColumn<PrescribedTherapy, String> tDailyCol;
    @FXML private TableColumn<PrescribedTherapy, String> tQtyCol;
    @FXML private TableColumn<PrescribedTherapy, String> tDirCol;
    @FXML private TableColumn<PrescribedTherapy, String> tStartCol;
    @FXML private TableColumn<PrescribedTherapy, String> tStatusCol;

    @FXML private TableView<ConcomitantCondition> conditionTable;
    @FXML private TableColumn<ConcomitantCondition, String> cTypeCol;
    @FXML private TableColumn<ConcomitantCondition, String> cDescCol;
    @FXML private TableColumn<ConcomitantCondition, String> cStartCol;
    @FXML private TableColumn<ConcomitantCondition, String> cEndCol;

    private Patient patient;

    public void setPatient(Patient patient) {
        this.patient = patient;
        loadAll();
    }

    private void loadAll() {
        if (patient == null) return;

        titleLabel.setText("Patient: " + patient.getFirstName() + " " + patient.getLastName());
        patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName() +
                " — Tax Code: " + patient.getTaxCode());
        patientInfoLabel.setText("Date of Birth: " + patient.getDateOfBirth() +
                "\nPast Pathologies: " + (patient.getPastPathologies() != null ? patient.getPastPathologies() : "—") +
                "\nComorbidities: " + (patient.getComorbidities() != null ? patient.getComorbidities() : "—"));
        riskLabel.setText("Risk Factors: " + (patient.getRiskFactors() != null ? patient.getRiskFactors() : "None reported"));

        setupGlucoseColumns();
        setupTherapyColumns();
        setupConditionColumns();
        loadGlucose();
        loadTherapies();
        loadConditions();
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
            return new SimpleStringProperty(abnormal ? "⚠️ Abnormal" : "✅ Normal");
        });
    }

    private void setupTherapyColumns() {
        tDrugCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDrugName()));
        tDailyCol.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getDailyIntakes())));
        tQtyCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getQuantityPerIntake()));
        tDirCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDirections() != null ? cd.getValue().getDirections() : "—"));
        tStartCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStartDate()));
        tStatusCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().isActive() ? "Active" : "Stopped"));
    }

    private void setupConditionColumns() {
        cTypeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getType()));
        cDescCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));
        cStartCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStartDate()));
        cEndCol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEndDate() != null ? cd.getValue().getEndDate() : "Ongoing"));
    }

    private void loadGlucose() {
        try {
            BloodGlucoseMeasurementDAO dao = new BloodGlucoseMeasurementDAO(SceneManager.getDbManager());
            List<BloodGlucoseMeasurement> list = dao.findByPatientId(patient.getId());
            glucoseTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTherapies() {
        try {
            PrescribedTherapyDAO dao = new PrescribedTherapyDAO(SceneManager.getDbManager());
            List<PrescribedTherapy> list = dao.findAllByPatientId(patient.getId());
            therapyTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadConditions() {
        try {
            ConcomitantConditionDAO dao = new ConcomitantConditionDAO(SceneManager.getDbManager());
            List<ConcomitantCondition> list = dao.findByPatientId(patient.getId());
            conditionTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    protected void onGlucoseChart() {
        GlucoseChartController ctrl = SceneManager.switchSceneAndGetController("glucose-chart.fxml");
        if (ctrl != null) ctrl.setPatient(patient);
    }

    @FXML
    protected void onNewTherapy() {
        TherapyFormController ctrl = SceneManager.switchSceneAndGetController("therapy-form.fxml");
        if (ctrl != null) ctrl.setPatient(patient);
    }

    @FXML
    protected void onEditInfo() {
        PatientInfoEditController ctrl = SceneManager.switchSceneAndGetController("patient-info-edit.fxml");
        if (ctrl != null) ctrl.setPatient(patient);
    }

    @FXML protected void onBack() { SceneManager.switchScene("doctor-dashboard.fxml"); }
    @FXML protected void onLogout() { SceneManager.logout(); }
}
