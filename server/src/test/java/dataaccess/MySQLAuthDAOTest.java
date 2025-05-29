package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.MySQLAuthDAO;
import dataaccess.UnauthorizedException;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class MySQLAuthDAOTest {

    Connection connection;
    MySQLAuthDAO authDAO;
    String authToken = "abc123";
    final String username = "nightblood";
    AuthData saveData;

    @BeforeEach
    void setup() throws DataAccessException, SQLException {

        DatabaseManager.createDatabase();
        connection = DatabaseManager.getConnection();
        saveData = new AuthData(authToken, username);

        authDAO = new MySQLAuthDAO();
    }

    private void addAuth() throws DataAccessException {
        clearTestAuth();
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    private void clearTestAuth() throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private boolean attemptFindAuth(String authToken) throws DataAccessException {

        boolean returnVal;
        String sql = "select authToken, username from auth where authToken = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            returnVal = stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return returnVal;
    }

    private int getCount() throws DataAccessException {
        int count = 0;
        String sql = "select authToken, username from auth;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            var res = stmt.executeQuery();

            while (res.next()) {
                count++;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return count;
    }

    @Test
    void createAuthValid() throws DataAccessException {
        clearTestAuth();
        authDAO.createAuth(new AuthData(authToken, username));
        boolean valid = attemptFindAuth(authToken);
        Assertions.assertTrue(valid, "Expected authToken to be created");
    }

    @Test
    void createAuthInvalid() throws DataAccessException {
        clearTestAuth();
        boolean valid = attemptFindAuth(authToken);
        Assertions.assertFalse(valid, "Expected authToken to not be created");
    }

    @Test
    void authenticateValid() throws DataAccessException {
        addAuth();
        Assertions.assertDoesNotThrow(() -> authDAO.authenticate(authToken));
    }

    @Test
    void authenticateInvalid() throws DataAccessException {
        clearTestAuth();
        Assertions.assertThrows(UnauthorizedException.class, () -> authDAO.authenticate(authToken));
    }

    @Test
    void deleteAuthValid() throws DataAccessException {
        addAuth();
        int desiredCount = getCount() - 1;
        authDAO.deleteAuth(saveData);
        Assertions.assertFalse(attemptFindAuth(authToken), "Expected token to be deleted");

        int actualCount = getCount();
        Assertions.assertEquals(desiredCount, actualCount);
    }

    @Test
    void deleteAuthInvalid() throws DataAccessException {
        clearTestAuth();
        int desiredCount = getCount();
        authDAO.deleteAuth(saveData);
        int actualCount = getCount();
        Assertions.assertEquals(desiredCount, actualCount);
    }

    @Test
    void clearValid() throws DataAccessException {

        addAuth();

        authDAO.clear();
        int count = getCount();
        Assertions.assertEquals(0, count);
    }

}