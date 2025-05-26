package dataaccess;

import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initializeDatabase() throws DataAccessException {
        createUsersTable();
        // You’ll add other tables here (Auth, Games)
    }

    private static void createUsersTable() throws DataAccessException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Users (
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

    // We’ll add createAuthTable() and createGamesTable() later!
}
