package dataaccess;

import model.AuthData;
import java.sql.*;

public class MySQLAuthDAO implements AuthDAO {

    private final String tableVal = DatabaseManager.TABLES[DatabaseManager.TableName.Auth.ordinal()];

    @Override
    public void createAuth(AuthData data) throws DataAccessException {
        String sql = "INSERT INTO " + tableVal + " (authToken, username) VALUES (?, ?);";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.authToken());
            stmt.setString(2, data.username());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error" + ex.getMessage());
        }
    }

    @Override
    public AuthData authenticate(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM " + tableVal + " WHERE authToken = ?;";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (ResultSet res = stmt.executeQuery()) {
                if (res.next()) {
                    return new AuthData(res.getString(1), res.getString(2));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error" + e.getMessage());
        }
        throw new UnauthorizedException("Error: unauthorized");
    }

    @Override
    public void deleteAuth(AuthData data) throws DataAccessException {
        String sql = "DELETE FROM " + tableVal + " WHERE authToken = ?;";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.authToken());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error" + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            ClearHelper.clearDB(tableVal, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error " + e);
        }
    }
}
