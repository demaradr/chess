package dataaccess;

import model.UserData;
import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    private final String tableVal = DatabaseManager.TABLES[DatabaseManager.TableName.Users.ordinal()];

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM " + tableVal + " WHERE username = ?;";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet res = stmt.executeQuery()) {
                if (res.next()) {
                    return new UserData(username, res.getString(2), res.getString(3));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error" + e.getMessage());
        }
        return null;
    }

    @Override
    public void createUser(UserData data) throws DataAccessException {
        String sql = "INSERT INTO " + tableVal + " (username, password, email) VALUES (?, ?, ?);";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.username());
            stmt.setString(2, data.password());
            stmt.setString(3, data.email());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error" + ex.getMessage());
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
