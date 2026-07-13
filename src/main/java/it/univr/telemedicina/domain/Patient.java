package it.univr.telemedicina.domain;

public class Patient {
    private Integer id;
    private String taxCode;
    private String firstName;
    private String lastName;
    private String dateOfBirth; // YYYY-MM-DD
    private String username;
    private String password;
    private Integer referenceDoctorId;
    private String riskFactors;
    private String pastPathologies;
    private String comorbidities;

    public Patient() {}

    public Patient(Integer id, String taxCode, String firstName, String lastName, String dateOfBirth, 
                   String username, String password, Integer referenceDoctorId, 
                   String riskFactors, String pastPathologies, String comorbidities) {
        this.id = id;
        this.taxCode = taxCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.username = username;
        this.password = password;
        this.referenceDoctorId = referenceDoctorId;
        this.riskFactors = riskFactors;
        this.pastPathologies = pastPathologies;
        this.comorbidities = comorbidities;
    }

    public Patient(String taxCode, String firstName, String lastName, String dateOfBirth, 
                   String username, String password, Integer referenceDoctorId) {
        this(null, taxCode, firstName, lastName, dateOfBirth, username, password, referenceDoctorId, null, null, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getReferenceDoctorId() {
        return referenceDoctorId;
    }

    public void setReferenceDoctorId(Integer referenceDoctorId) {
        this.referenceDoctorId = referenceDoctorId;
    }

    public String getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getPastPathologies() {
        return pastPathologies;
    }

    public void setPastPathologies(String pastPathologies) {
        this.pastPathologies = pastPathologies;
    }

    public String getComorbidities() {
        return comorbidities;
    }

    public void setComorbidities(String comorbidities) {
        this.comorbidities = comorbidities;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", taxCode='" + taxCode + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", referenceDoctorId=" + referenceDoctorId +
                '}';
    }
}
