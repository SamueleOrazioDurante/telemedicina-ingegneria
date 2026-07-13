package it.univr.telemedicina.domain;

public class PrescribedTherapy {
    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private String drugName;
    private int dailyIntakes;
    private String quantityPerIntake;
    private String directions;
    private String startDate; // YYYY-MM-DD
    private String endDate;   // YYYY-MM-DD (nullable)
    private boolean active;

    public PrescribedTherapy() {}

    public PrescribedTherapy(Integer id, Integer patientId, Integer doctorId, String drugName, 
                             int dailyIntakes, String quantityPerIntake, String directions, 
                             String startDate, String endDate, boolean active) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.drugName = drugName;
        this.dailyIntakes = dailyIntakes;
        this.quantityPerIntake = quantityPerIntake;
        this.directions = directions;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public PrescribedTherapy(Integer patientId, Integer doctorId, String drugName, 
                             int dailyIntakes, String quantityPerIntake, String directions, 
                             String startDate, String endDate) {
        this(null, patientId, doctorId, drugName, dailyIntakes, quantityPerIntake, directions, startDate, endDate, true);
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

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public int getDailyIntakes() {
        return dailyIntakes;
    }

    public void setDailyIntakes(int dailyIntakes) {
        this.dailyIntakes = dailyIntakes;
    }

    public String getQuantityPerIntake() {
        return quantityPerIntake;
    }

    public void setQuantityPerIntake(String quantityPerIntake) {
        this.quantityPerIntake = quantityPerIntake;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "PrescribedTherapy{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", drugName='" + drugName + '\'' +
                ", dailyIntakes=" + dailyIntakes +
                ", active=" + active +
                '}';
    }
}
