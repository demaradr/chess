package dataaccess;

import model.AuthData;
import java.sql.*;

public class MySQLAuthDAO implements AuthDAO {

    Connection connection;
    private String tableVal;

    public MySQLAuthDAO(Connection connection) {
        this.connection = connection;
        tableVal = DatabaseManager.TABLES[DatabaseManager.TableName.Auth.ordinal()];
    }

    @Override
    public void createAuth(AuthData data) throws DataAccessException {
        String sql = "INSERT INTO " + tableVal + " (authToken, username) VALUES (?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.authToken());
            stmt.setString(2, data.username());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }

    }

    @Override
    public AuthData authenticate(String authToken) throws DataAccessException {
        AuthData returnVal = null;
        String sql = "select authToken, username from " + tableVal + " where authToken = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            var res = stmt.executeQuery();
            if (res.next()) {
                String auth = res.getString(1);
                String username = res.getString(2);
                returnVal = new AuthData(auth, username);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        if (returnVal == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return returnVal;

    }

    @Override
    public void deleteAuth(AuthData data) throws DataAccessException {
        String sql = "delete from " + tableVal + " where authToken = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.authToken());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        ClearHelper.clearDB(tableVal, connection);
    }
}