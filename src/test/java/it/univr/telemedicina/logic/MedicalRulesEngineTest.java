package it.univr.telemedicina.logic;

import it.univr.telemedicina.domain.BloodGlucoseMeasurement;
import it.univr.telemedicina.domain.DrugIntake;
import it.univr.telemedicina.domain.PrescribedTherapy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedicalRulesEngineTest {

    private MedicalRulesEngine engine;
    private List<String> receivedAlerts;

    @BeforeEach
    void setUp() {
        engine = new MedicalRulesEngine();
        receivedAlerts = new ArrayList<>();
        engine.registerObserver(alertMessage -> receivedAlerts.add(alertMessage));
    }

    @Test
    void testCheckGlucoseThreshold_NormalBeforeMeal() {
        BloodGlucoseMeasurement measurement = new BloodGlucoseMeasurement(1, 100.0, "BEFORE_MEAL", "2024-10-10", "08:00");
        assertFalse(engine.checkGlucoseThreshold(measurement));
        assertTrue(receivedAlerts.isEmpty());
    }

    @Test
    void testCheckGlucoseThreshold_HighBeforeMeal() {
        BloodGlucoseMeasurement measurement = new BloodGlucoseMeasurement(1, 140.0, "BEFORE_MEAL", "2024-10-10", "08:00");
        assertTrue(engine.checkGlucoseThreshold(measurement));
        assertEquals(1, receivedAlerts.size());
    }

    @Test
    void testCheckGlucoseThreshold_NormalAfterMeal() {
        BloodGlucoseMeasurement measurement = new BloodGlucoseMeasurement(1, 150.0, "AFTER_MEAL", "2024-10-10", "14:00");
        assertFalse(engine.checkGlucoseThreshold(measurement));
        assertTrue(receivedAlerts.isEmpty());
    }

    @Test
    void testCheckGlucoseThreshold_HighAfterMeal() {
        BloodGlucoseMeasurement measurement = new BloodGlucoseMeasurement(1, 190.0, "AFTER_MEAL", "2024-10-10", "14:00");
        assertTrue(engine.checkGlucoseThreshold(measurement));
        assertEquals(1, receivedAlerts.size());
    }

    @Test
    void testPostPrandialHypoglycemia() {
        BloodGlucoseMeasurement measurement = new BloodGlucoseMeasurement(1, 65.0, "AFTER_MEAL", "2024-10-10", "14:00");
        assertTrue(engine.checkGlucoseThreshold(measurement));
        assertEquals(MedicalRulesEngine.GlucoseSeverity.SEVERE_HYPOGLYCEMIA, engine.getGlucoseSeverity(measurement));
    }

    @Test
    void testGlucoseSeverityLevels() {
        BloodGlucoseMeasurement severeHypo = new BloodGlucoseMeasurement(1, 60.0, "BEFORE_MEAL", "2024-10-10", "08:00");
        assertEquals(MedicalRulesEngine.GlucoseSeverity.SEVERE_HYPOGLYCEMIA, engine.getGlucoseSeverity(severeHypo));

        BloodGlucoseMeasurement modHypo = new BloodGlucoseMeasurement(1, 75.0, "BEFORE_MEAL", "2024-10-10", "08:00");
        assertEquals(MedicalRulesEngine.GlucoseSeverity.MODERATE_HYPOGLYCEMIA, engine.getGlucoseSeverity(modHypo));

        BloodGlucoseMeasurement modHyper = new BloodGlucoseMeasurement(1, 200.0, "AFTER_MEAL", "2024-10-10", "14:00");
        assertEquals(MedicalRulesEngine.GlucoseSeverity.MODERATE_HYPERGLYCEMIA, engine.getGlucoseSeverity(modHyper));

        BloodGlucoseMeasurement severeHyper = new BloodGlucoseMeasurement(1, 280.0, "AFTER_MEAL", "2024-10-10", "14:00");
        assertEquals(MedicalRulesEngine.GlucoseSeverity.SEVERE_HYPERGLYCEMIA, engine.getGlucoseSeverity(severeHyper));
    }

    @Test
    void testCheckMissingTherapy_NotMissing() {
        PrescribedTherapy therapy = new PrescribedTherapy(1, 2, "Insulin", 1, "10mg", "After meal", "2024-10-01", null);
        therapy.setId(10);
        
        List<DrugIntake> intakes = new ArrayList<>();
        intakes.add(new DrugIntake(1, 10, "2024-10-09", "08:00", "10mg"));
        intakes.add(new DrugIntake(1, 10, "2024-10-08", "08:00", "10mg"));
        intakes.add(new DrugIntake(1, 10, "2024-10-07", "08:00", "10mg"));
        
        assertFalse(engine.checkMissingTherapy(intakes, therapy, LocalDate.of(2024, 10, 10)));
        assertTrue(receivedAlerts.isEmpty());
    }

    @Test
    void testCheckMissingTherapy_MissingThreeDays() {
        PrescribedTherapy therapy = new PrescribedTherapy(1, 2, "Insulin", 1, "10mg", "After meal", "2024-10-01", null);
        therapy.setId(10);
        
        List<DrugIntake> intakes = new ArrayList<>();
        // No intakes for the last 3 days
        
        assertTrue(engine.checkMissingTherapy(intakes, therapy, LocalDate.of(2024, 10, 10)));
        assertEquals(1, receivedAlerts.size());
    }

    @Test
    void testCheckMissingTherapy_NonConsecutiveMissing() {
        PrescribedTherapy therapy = new PrescribedTherapy(1, 2, "Insulin", 1, "10mg", "After meal", "2024-10-01", null);
        therapy.setId(10);

        List<DrugIntake> intakes = new ArrayList<>();
        // Compliant on day -2, but missing on day -1 and day -3
        intakes.add(new DrugIntake(1, 10, "2024-10-08", "08:00", "10mg"));

        assertFalse(engine.checkMissingTherapy(intakes, therapy, LocalDate.of(2024, 10, 10)),
                "Non-consecutive missed days should NOT trigger alert");
        assertTrue(receivedAlerts.isEmpty());
    }
}
