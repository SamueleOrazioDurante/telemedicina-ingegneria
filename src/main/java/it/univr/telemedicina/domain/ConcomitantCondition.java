package it.univr.telemedicina.domain;

public class ConcomitantCondition {
    private Integer id;
    private Integer patientId;
    private String type; // SYMPTOM, PATHOLOGY, CONCOMITANT_THERAPY
    private String description;
    private String startDate; // YYYY-MM-DD
    private String endDate;   // YYYY-MM-DD (nullable)

    public ConcomitantCondition() {}

    public ConcomitantCondition(Integer id, Integer patientId, String type, String description, 
                                String startDate, String endDate) {
        this.id = id;
        this.patientId = patientId;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ConcomitantCondition(Integer patientId, String type, String description, 
                                String startDate, String endDate) {
        this(null, patientId, type, description, startDate, endDate);
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "ConcomitantCondition{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
