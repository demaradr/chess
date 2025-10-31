package dataaccess;

import models.AuthData;

import javax.xml.crypto.Data;
import java.sql.*;

public class MySqlAuthDAO implements AuthDAO{
    public MySqlAuthDAO () throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        DatabaseManager.updateData(sql, auth.authToken(), auth.username());

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT authToken, username FROM auth WHERE authToken=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        String username = rs.getString("username");
                        return new AuthData(authToken, username);
                    }
                }
            }

        }

        catch (SQLException error) {
            throw new DataAccessException(error.getMessage());
        }return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken=?";
        DatabaseManager.updateData(sql, authToken);

    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE auth";
        DatabaseManager.updateData(sql);

    }






    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL
                )
            """
    };




}
