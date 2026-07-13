package it.univr.telemedicina;

import it.univr.telemedicina.persistence.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("Database Layer Initialized");
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();
        
        System.out.println("Application Started");
        App.main(args);
    }
}
