package it.univr.telemedicina.domain;

public class BloodGlucoseMeasurement {
    private Integer id;
    private Integer patientId;
    private double value;
    private String timeSlot; // BEFORE_MEAL or AFTER_MEAL
    private String date;     // YYYY-MM-DD
    private String time;     // HH:MM

    public BloodGlucoseMeasurement() {}

    public BloodGlucoseMeasurement(Integer id, Integer patientId, double value, String timeSlot, String date, String time) {
        this.id = id;
        this.patientId = patientId;
        this.value = value;
        this.timeSlot = timeSlot;
        this.date = date;
        this.time = time;
    }

    public BloodGlucoseMeasurement(Integer patientId, double value, String timeSlot, String date, String time) {
        this(null, patientId, value, timeSlot, date, time);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "BloodGlucoseMeasurement{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", value=" + value +
                ", timeSlot='" + timeSlot + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
