package it.univr.telemedicina.logic;

import it.univr.telemedicina.domain.BloodGlucoseMeasurement;
import it.univr.telemedicina.domain.DrugIntake;
import it.univr.telemedicina.domain.PrescribedTherapy;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MedicalRulesEngine implements AlertSubject {
    private List<AlertObserver> observers = new ArrayList<>();

    @Override
    public void registerObserver(AlertObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(AlertObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String alertMessage) {
        for (AlertObserver observer : observers) {
            observer.onAlert(alertMessage);
        }
    }

    public enum GlucoseSeverity {
        NONE,
        MODERATE_HYPOGLYCEMIA,
        SEVERE_HYPOGLYCEMIA,
        MODERATE_HYPERGLYCEMIA,
        SEVERE_HYPERGLYCEMIA
    }

    public GlucoseSeverity getGlucoseSeverity(BloodGlucoseMeasurement measurement) {
        if (measurement == null) return GlucoseSeverity.NONE;
        double val = measurement.getValue();

        if (val < 70.0) {
            return GlucoseSeverity.SEVERE_HYPOGLYCEMIA;
        }

        if ("BEFORE_MEAL".equals(measurement.getTimeSlot())) {
            if (val >= 70.0 && val < 80.0) {
                return GlucoseSeverity.MODERATE_HYPOGLYCEMIA;
            } else if (val > 130.0 && val <= 250.0) {
                return GlucoseSeverity.MODERATE_HYPERGLYCEMIA;
            } else if (val > 250.0) {
                return GlucoseSeverity.SEVERE_HYPERGLYCEMIA;
            }
        } else if ("AFTER_MEAL".equals(measurement.getTimeSlot())) {
            if (val > 180.0 && val <= 250.0) {
                return GlucoseSeverity.MODERATE_HYPERGLYCEMIA;
            } else if (val > 250.0) {
                return GlucoseSeverity.SEVERE_HYPERGLYCEMIA;
            }
        }
        return GlucoseSeverity.NONE;
    }

    public boolean checkGlucoseThreshold(BloodGlucoseMeasurement measurement) {
        GlucoseSeverity severity = getGlucoseSeverity(measurement);
        if (severity != GlucoseSeverity.NONE) {
            notifyObservers("ALERT [" + severity + "]: Abnormal glucose level detected for patient ID " 
                    + measurement.getPatientId() + ": " + measurement.getValue() + " mg/dL (" + measurement.getTimeSlot() + ")");
            return true;
        }
        return false;
    }

    public boolean checkMissingTherapy(List<DrugIntake> intakes, PrescribedTherapy therapy, LocalDate today) {
        int consecutiveMissingDays = 0;
        
        for (int i = 1; i <= 3; i++) {
            LocalDate checkDate = today.minusDays(i);
            String dateStr = checkDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long intakesForDay = intakes.stream()
                .filter(intake -> intake.getTherapyId().equals(therapy.getId()) && dateStr.equals(intake.getDate()))
                .count();
            
            if (intakesForDay < therapy.getDailyIntakes()) {
                consecutiveMissingDays++;
            } else {
                break;
            }
        }
        
        if (consecutiveMissingDays >= 3) {
            notifyObservers("ALERT: Patient ID " + therapy.getPatientId() + " missed therapy '" + therapy.getDrugName() + "' for " + consecutiveMissingDays + " consecutive days.");
            return true;
        }
        return false;
    }
}
