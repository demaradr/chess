package dataaccess;

import model.AuthData;
import java.sql.*;

public class MySQLAuthDAO implements AuthDAO {

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO Auth (authToken, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth token", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT * FROM Auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    return new AuthData(authToken, username);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving auth token", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM Auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token", e);
        }
    }

    @Override
    public void createAuth(String username, String token) throws DataAccessException {
        createAuth(new AuthData(token, username));
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM Auth";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing Auth table", e);
        }
    }
}

