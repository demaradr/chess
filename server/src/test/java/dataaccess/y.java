///*
//package dataaccess;
//
//import model.UserData;
//import org.junit.jupiter.api.AfterEach;
//import org.mindrot.jbcrypt.BCrypt;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.sql.SQLException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MySQLUserDAOTest {
//
//    UserDAO dao;
//    UserData defaultUser;
//
//    @BeforeEach
//    void setUp() throws DataAccessException, SQLException {
//        DatabaseManager.createDatabase();
//        dao = new MySQLUserDAO();
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("TRUNCATE user")) {
//                statement.executeUpdate();
//            }
//        }
//
//        defaultUser = new UserData("username", "password", "email");
//    }
//
//    @AfterEach
//    void tearDown() throws SQLException, DataAccessException {
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("TRUNCATE user")) {
//                statement.executeUpdate();
//            }
//        }
//    }
//
//    @Test
//    void createUserPositive() throws DataAccessException, SQLException {
//        dao.createUser(defaultUser);
//
//        String resultUsername;
//        String resultPassword;
//        String resultEmail;
//
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
//                statement.setString(1, defaultUser.username());
//                try (var results = statement.executeQuery()) {
//                    results.next();
//                    resultUsername = results.getString("username");
//                    resultPassword = results.getString("password");
//                    resultEmail = results.getString("email");
//                }
//            }
//        }
//
//        assertEquals(defaultUser.username(), resultUsername);
//        assertTrue(passwordMatches(defaultUser.password(), resultPassword));
//        assertEquals(defaultUser.email(), resultEmail);
//    }
//
//    @Test
//    void getUserPositive() throws DataAccessException {
//        dao.createUser(defaultUser);
//
//        UserData resultUser = dao.getUser(defaultUser.username());
//
//        assertEquals(defaultUser.username(), resultUser.username());
//        assertTrue(passwordMatches(defaultUser.password(), resultUser.password()));
//        assertEquals(defaultUser.email(), resultUser.email());
//    }
//
//    @Test
//    void getUserNegative() throws DataAccessException {
//        UserData result = dao.getUser(defaultUser.username());
//        assertNull(result, "Expected null for non-existent user.");
//    }
//
//
//    @Test
//    void authenticateUserPositive() throws DataAccessException {
//        dao.createUser(defaultUser);
//
//        assertTrue(dao.authenticateUser(defaultUser.username(), defaultUser.password()));
//    }
//
//    @Test
//    void authenticateUserNegative() throws DataAccessException {
//        dao.createUser(defaultUser);
//
//        assertFalse(dao.authenticateUser(defaultUser.username(), "badPass"));
//    }
//
//    @Test
//    void clear() throws DataAccessException, SQLException {
//        dao.createUser(defaultUser);
//        dao.clear();
//
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
//                statement.setString(1, defaultUser.username());
//                try (var results = statement.executeQuery()) {
//                    assertFalse(results.next()); //There should be no elements
//                }
//            }
//        }
//    }
//
//    private boolean passwordMatches(String rawPassword, String hashedPassword) {
//        return BCrypt.checkpw(rawPassword, hashedPassword);
//    }
//}*/
