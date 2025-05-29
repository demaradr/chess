package dataaccess;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String user;
    private static String password;
    private static String url;

    private static final String DEFAULT_PROPERTIES_FILE = "/db.properties";
    public static final String[] TABLES = {"auth", "games", "users"};

    static {
        loadPropertiesFromResources();
    }

    /**
     * Load database properties from the default db.properties file in the classpath.
     */
    private static void loadPropertiesFromResources() {
        loadProperties(DEFAULT_PROPERTIES_FILE);
    }

    /**
     * Allows overriding the properties programmatically (e.g., for testing).
     */
    public static void loadProperties(Properties props) {
        applyProperties(props);
    }

    /**
     * Load properties from a specific file path in the classpath.
     * For example: loadProperties("/test-db.properties")
     */
    public static void loadProperties(String filePath) {
        Properties props = new Properties();
        try (InputStream input = DatabaseManager.class.getResourceAsStream(filePath)) {
            if (input == null) {
                throw new RuntimeException("Error: Properties file not found at " + filePath);
            }
            props.load(input);
            applyProperties(props);
        } catch (IOException e) {
            throw new RuntimeException("Error: Failed to load properties file at " + filePath, e);
        }
    }

    /**
     * Helper method to apply properties to static fields.
     */
    private static void applyProperties(Properties props) {
        try {
            databaseName = props.getProperty("db.name");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            var host = props.getProperty("db.host");
            var port = Integer.parseInt(props.getProperty("db.port"));
            url = String.format("jdbc:mysql://%s:%d", host, port);

            if (databaseName == null || user == null || password == null || host == null) {
                throw new RuntimeException("Error: Missing required database properties.");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error processing properties: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get a connection to the database.
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(url, user, password);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    /**
     * Create the database and tables if they don't exist.
     */
    public static void createDatabase() throws DataAccessException {
        try (var conn = DriverManager.getConnection(url, user, password)) {
            var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            statement = "USE " + databaseName;
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            statement = """
                    CREATE TABLE IF NOT EXISTS games (
                       id INT NOT NULL AUTO_INCREMENT,
                       whiteUsername VARCHAR(255),
                       blackUsername VARCHAR(255),
                       gameName VARCHAR(255) NOT NULL,
                       chessGame LONGTEXT NOT NULL,
                       PRIMARY KEY (id)
                    );""";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            statement = """
                    CREATE TABLE IF NOT EXISTS auth (
                       authToken VARCHAR(255) NOT NULL,
                       username VARCHAR(255) NOT NULL,
                       PRIMARY KEY (authToken)
                    );""";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            statement = """
                    CREATE TABLE IF NOT EXISTS users (
                       username VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       PRIMARY KEY (username)
                    );""";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    /**
     * Reset the database by truncating all tables.
     */
    public static void reset() throws DataAccessException {
        try (var connection = getConnection()) {
            for (var table : TABLES) {
                String sql = "TRUNCATE TABLE " + table + ";";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    public enum TableName {
        Auth,
        Games,
        Users
    }
}
