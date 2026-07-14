package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.Doctor;
import it.univr.telemedicina.domain.OperationLog;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.persistence.DatabaseManager;
import it.univr.telemedicina.persistence.OperationLogDAO;
import it.univr.telemedicina.persistence.PatientDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuditLogsController {

    @FXML private TableView<OperationLog> logsTable;
    @FXML private TableColumn<OperationLog, String> timestampCol;
    @FXML private TableColumn<OperationLog, String> patientCol;
    @FXML private TableColumn<OperationLog, String> operationCol;

    private final Map<Integer, String> patientNamesMap = new HashMap<>();

    @FXML
    public void initialize() {
        timestampCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTimestamp()));
        operationCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getOperation()));

        patientCol.setCellValueFactory(cd -> {
            Integer patId = cd.getValue().getPatientId();
            if (patId == null) {
                return new SimpleStringProperty("N/A");
            }
            String name = patientNamesMap.get(patId);
            if (name == null) {
                return new SimpleStringProperty("Patient #" + patId);
            }
            return new SimpleStringProperty(name);
        });

        loadLogs();
    }

    private void loadLogs() {
        Doctor doctor = SceneManager.getCurrentDoctor();
        if (doctor == null) return;

        DatabaseManager db = SceneManager.getDbManager();
        try {
            // First load all doctor's patients to map ID to Name
            PatientDAO patientDAO = new PatientDAO(db);
            List<Patient> patients = patientDAO.findByDoctorId(doctor.getId());
            for (Patient p : patients) {
                patientNamesMap.put(p.getId(), p.getFirstName() + " " + p.getLastName());
            }

            // Load logs
            OperationLogDAO logDAO = new OperationLogDAO(db);
            List<OperationLog> logs = logDAO.findByDoctorId(doctor.getId());
            logsTable.setItems(FXCollections.observableArrayList(logs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClose() {
        // Find stage and close
        Stage stage = (Stage) logsTable.getScene().getWindow();
        stage.close();
    }
}
