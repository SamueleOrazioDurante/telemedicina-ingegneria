package it.univr.telemedicina.domain;

public class DrugIntake {
    private Integer id;
    private Integer patientId;
    private Integer therapyId;
    private String date; // YYYY-MM-DD
    private String time; // HH:MM
    private String quantityTaken;

    public DrugIntake() {}

    public DrugIntake(Integer id, Integer patientId, Integer therapyId, String date, String time, String quantityTaken) {
        this.id = id;
        this.patientId = patientId;
        this.therapyId = therapyId;
        this.date = date;
        this.time = time;
        this.quantityTaken = quantityTaken;
    }

    public DrugIntake(Integer patientId, Integer therapyId, String date, String time, String quantityTaken) {
        this(null, patientId, therapyId, date, time, quantityTaken);
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

    public Integer getTherapyId() {
        return therapyId;
    }

    public void setTherapyId(Integer therapyId) {
        this.therapyId = therapyId;
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

    public String getQuantityTaken() {
        return quantityTaken;
    }

    public void setQuantityTaken(String quantityTaken) {
        this.quantityTaken = quantityTaken;
    }

    @Override
    public String toString() {
        return "DrugIntake{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", therapyId=" + therapyId +
                ", date='" + date + '\'' +
                ", quantityTaken='" + quantityTaken + '\'' +
                '}';
    }
}
