package com.todolist.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private static final String URL = "jdbc:h2:./data/todolist;AUTO_SERVER=TRUE";
    private static final String USER = "aboayed";
    private static final String PASSWORD = "";

    private static Connection sharedConnection;

    private Database() {
    }

 
    public static synchronized Connection getConnection() {
        try {
            if (sharedConnection == null || sharedConnection.isClosed()) {
                sharedConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return sharedConnection;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open database connection", e);
        }
    }

    //Read schema.sql and execute the SQL inside it.
    public static void initSchema() {
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql not found on classpath");
            }
            String ddl = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement stmt = getConnection().createStatement()) {
                for (String statement : ddl.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to initialize schema", e);
        }
    }
}
