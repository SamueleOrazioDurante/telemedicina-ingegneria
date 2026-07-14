package it.univr.telemedicina.domain;

public class PatientMessage {
    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private String subject;
    private String message;
    private String date; // YYYY-MM-DD
    private String time; // HH:MM

    public PatientMessage() {}

    public PatientMessage(Integer id, Integer patientId, Integer doctorId, String subject, String message, String date, String time) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.subject = subject;
        this.message = message;
        this.date = date;
        this.time = time;
    }

    public PatientMessage(Integer patientId, Integer doctorId, String subject, String message, String date, String time) {
        this(null, patientId, doctorId, subject, message, date, time);
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

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        return "PatientMessage{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", subject='" + subject + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
