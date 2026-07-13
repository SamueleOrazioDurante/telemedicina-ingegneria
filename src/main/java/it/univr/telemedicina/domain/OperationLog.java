package it.univr.telemedicina.domain;

public class OperationLog {
    private Integer id;
    private Integer doctorId;
    private Integer patientId; // Can be null
    private String operation;
    private String timestamp; // YYYY-MM-DD HH:MM:SS

    public OperationLog() {}

    public OperationLog(Integer id, Integer doctorId, Integer patientId, String operation, String timestamp) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.operation = operation;
        this.timestamp = timestamp;
    }

    public OperationLog(Integer doctorId, Integer patientId, String operation, String timestamp) {
        this(null, doctorId, patientId, operation, timestamp);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", doctorId=" + doctorId +
                ", patientId=" + patientId +
                ", operation='" + operation + '\'' +
                '}';
    }
}
