package it.univr.telemedicina.persistence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:telemedicina.db";
    private final String dbUrl;

    public DatabaseManager() {
        this(DEFAULT_DB_URL);
    }

    public DatabaseManager(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found.", e);
        }
    }

    /**
     * Gets a connection to the database and configures it (e.g. enabling foreign keys).
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Reads database.sql DDL script and initializes the SQLite tables.
     */
    public void initializeDatabase() {
        // If the database file already exists and is non-empty, skip initialization
        if (dbUrl.startsWith("jdbc:sqlite:")) {
            String dbPath = dbUrl.substring("jdbc:sqlite:".length());
            if (!dbPath.equals(":memory:") && !dbPath.trim().isEmpty()) {
                java.io.File file = new java.io.File(dbPath);
                if (file.exists() && file.length() > 0) {
                    System.out.println("Database already exists. Skipping initialization.");
                    return;
                }
            }
        }

        try (Connection conn = getConnection();
             InputStream is = DatabaseManager.class.getResourceAsStream("/database.sql")) {
            
            if (is == null) {
                throw new RuntimeException("database.sql not found in resources.");
            }

            String sql;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                sql = reader.lines()
                        .map(line -> {
                            int commentIdx = line.indexOf("--");
                            if (commentIdx != -1) {
                                return line.substring(0, commentIdx);
                            }
                            return line;
                        })
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.joining(" "));
            }

            String[] statements = sql.split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sqlStmt : statements) {
                    String trimmed = sqlStmt.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            System.out.println("Database successfully initialized or verified.");
        } catch (Exception e) {
            throw new RuntimeException("Error initializing database: " + e.getMessage(), e);
        }
    }
}
