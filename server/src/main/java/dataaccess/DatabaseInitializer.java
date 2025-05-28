package dataaccess;

import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initializeDatabase() throws DataAccessException {
        createUsersTable();
        createAuthTable();
        createGamesTable();
    }

    private static void createUsersTable() throws DataAccessException {
        String sql = """
                CREATE TABLE IF NOT EXISTS User (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255)
                );
                """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            throw new DataAccessException("Error creating Users table", e);
        }
    }

    private static void createAuthTable() throws DataAccessException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Auth (
                    authToken VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    FOREIGN KEY (username) REFERENCES User(username) ON DELETE CASCADE
                );
                """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            throw new DataAccessException("Error creating Auth table", e);
        }
    }

    private static void createGamesTable() throws DataAccessException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Game (
                    gameID INT PRIMARY KEY AUTO_INCREMENT,
                    gameName VARCHAR(255) NOT NULL,
                    gameState TEXT NOT NULL,  -- Store JSON or FEN here
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    FOREIGN KEY (whiteUsername) REFERENCES User(username) ON DELETE SET NULL,
                    FOREIGN KEY (blackUsername) REFERENCES User(username) ON DELETE SET NULL
                );
                """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            throw new DataAccessException("Error creating Games table", e);
        }
    }
}
