package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO User (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.email());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error creating user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM User WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error getting user", e);
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData user = getUser(username);

        if (user == null) {
            return false;
        }

        return BCrypt.checkpw(password, user.password());
    }

    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE User")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
        }
    }



    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
        try {
            createUser(new UserData(username, password, email));
        } catch (DataAccessException e) {
            throw e;
        }
    }
}
