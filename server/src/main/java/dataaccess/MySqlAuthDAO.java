package dataaccess;

import models.AuthData;

import java.sql.*;

public class MySqlAuthDAO implements AuthDAO{
    public MySqlAuthDAO () throws DataAccessException {
        configureDatabase();
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        updateData(sql, auth.authToken(), auth.username());

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
        updateData(sql, authToken);

    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE auth";
        updateData(sql);

    }


    private int updateData(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];

                    if (param instanceof String p) {
                        ps.setString(i + 1, p);

                    }
                    else if (param == null) {
                        ps.setNull(i + 1, Types.NULL);
                    }
                }
                ps.executeUpdate();
                return 0;
            }
        }
        catch (SQLException error) {
            throw new DataAccessException(error.getMessage());
        }
    }




    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL
                )
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (PreparedStatement ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        }
        catch (SQLException error) {
            throw new DataAccessException(error.getMessage());
        }
    }
}
