package it.univr.telemedicina;

import it.univr.telemedicina.persistence.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("Telemedicine System for Diabetic Patients - Database Layer Initialized");
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();
    }
}
