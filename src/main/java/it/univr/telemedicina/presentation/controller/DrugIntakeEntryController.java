package it.univr.telemedicina.presentation.controller;

import it.univr.telemedicina.domain.DrugIntake;
import it.univr.telemedicina.domain.Patient;
import it.univr.telemedicina.domain.PrescribedTherapy;
import it.univr.telemedicina.persistence.DrugIntakeDAO;
import it.univr.telemedicina.persistence.PrescribedTherapyDAO;
import it.univr.telemedicina.presentation.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the drug intake entry form.
 * Allows patients to log medication consumption linked to an active therapy.
 */
public class DrugIntakeEntryController {

    @FXML private ComboBox<PrescribedTherapy> therapyCombo;
    @FXML private TextField drugNameField;
    @FXML private TextField quantityField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private Label feedbackLabel;

    @FXML
    public void initialize() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        datePicker.setValue(LocalDate.now());
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        loadActiveTherapies(patient);

        // When therapy is selected, auto-fill drug name and quantity
        therapyCombo.setOnAction(e -> {
            PrescribedTherapy selected = therapyCombo.getValue();
            if (selected != null) {
                drugNameField.setText(selected.getDrugName());
                quantityField.setText(selected.getQuantityPerIntake());
            }
        });
    }

    private void loadActiveTherapies(Patient patient) {
        try {
            PrescribedTherapyDAO dao = new PrescribedTherapyDAO(SceneManager.getDbManager());
            List<PrescribedTherapy> therapies = dao.findActiveByPatientId(patient.getId());
            therapyCombo.setItems(FXCollections.observableArrayList(therapies));

            therapyCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(PrescribedTherapy t) {
                    return t == null ? "" : t.getDrugName() + " (" + t.getQuantityPerIntake() + " x" + t.getDailyIntakes() + "/day)";
                }
                @Override
                public PrescribedTherapy fromString(String s) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSave() {
        Patient patient = SceneManager.getCurrentPatient();
        if (patient == null) { SceneManager.logout(); return; }

        PrescribedTherapy therapy = therapyCombo.getValue();
        if (therapy == null) { showFeedback("Please select a therapy.", true); return; }

        String quantity = quantityField.getText().trim();
        if (quantity.isEmpty()) { showFeedback("Please enter the quantity taken.", true); return; }

        LocalDate date = datePicker.getValue();
        if (date == null) { showFeedback("Please select a date.", true); return; }

        String time = timeField.getText().trim();
        try {
            java.time.LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            showFeedback("Time must be a valid time in HH:MM format (e.g. 08:30, 14:15).", true);
            return;
        }

        try {
            DrugIntake intake = new DrugIntake(patient.getId(), therapy.getId(),
                    date.format(DateTimeFormatter.ISO_LOCAL_DATE), time, therapy.getDrugName(), quantity);

            DrugIntakeDAO dao = new DrugIntakeDAO(SceneManager.getDbManager());
            dao.save(intake);

            showFeedback("Drug intake recorded: " + therapy.getDrugName() + " - " + quantity, false);
            quantityField.setText(therapy.getQuantityPerIntake());
        } catch (Exception e) {
            showFeedback("Error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClear() {
        therapyCombo.setValue(null);
        drugNameField.clear();
        quantityField.clear();
        datePicker.setValue(LocalDate.now());
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(isError ? "-fx-text-fill: #f38ba8; -fx-font-size: 13px;" : "-fx-text-fill: -color-accent; -fx-font-size: 13px;");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    @FXML
    protected void onClose() {
        if (feedbackLabel.getScene() != null && feedbackLabel.getScene().getWindow() != null) {
            ((javafx.stage.Stage) feedbackLabel.getScene().getWindow()).close();
        }
    }
}
