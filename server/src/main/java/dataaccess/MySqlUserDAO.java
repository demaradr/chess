package dataaccess;

import models.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class MySqlUserDAO implements UserDAO{

    public MySqlUserDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(u.password(), BCrypt.gensalt());
        DatabaseManager.updateData(sql, u.username(), hashedPassword, u.email());


    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try(Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT username, password, email FROM users WHERE username = ?";

            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try(ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        String password = rs.getString("password");
                        String email = rs.getString("email");
                        return new UserData(username, password, email);
                    }
                }
            }
        }
        catch (SQLException error) {
            throw new DataAccessException(error.getMessage());
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE users";
        DatabaseManager.updateData(sql);

    }






    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
                )
            """
    };




    }

