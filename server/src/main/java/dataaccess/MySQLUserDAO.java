package dataaccess;

import dataaccess.UserDAO;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {

    Connection connection;
    private String tableVal;

    public MySQLUserDAO(Connection connection) {
        this.connection = connection;
        tableVal = DatabaseManager.TABLES[DatabaseManager.TableName.Users.ordinal()];
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData returnVal = null;
        String sql = "select username, password, email from " + tableVal + " where username = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            var res = stmt.executeQuery();
            if (res.next()) {
                String password = res.getString(2);
                String email = res.getString(3);
                returnVal = new UserData(username, password, email);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return returnVal;
    }

    @Override
    public void createUser(UserData data) throws DataAccessException {
        String sql = "INSERT INTO " + tableVal + " (username, password, email) VALUES (?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.username());
            stmt.setString(2, data.password());
            stmt.setString(3, data.email());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        ClearHelper.clearDB(tableVal, connection);
    }
}