package passoff.server;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.UserService;

public class UnitTests {

    static UserService userService;
    static UserDAO userDAO;
    static AuthDAO authDAO;

    static UserData defaultUser;


    @BeforeAll
    static void init() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @BeforeEach
    void setup() {
        userDAO.clear();
        authDAO.clear();

        defaultUser = new UserData("Username", "password", "email");
    }

    @Test
    @DisplayName("Create Valid User")
    void createUserTestPositive() throws DataAccessException {
        AuthData resultAuth = userService.register(defaultUser);
        Assertions.assertEquals(authDAO.getAuth(resultAuth.authToken()), resultAuth);
    }

    @Test
    @DisplayName("Create Invalid User")
    void createUserTestNegative() throws DataAccessException {
        userService.register(defaultUser);
        Assertions.assertThrows(DataAccessException.class, () -> userService.register(defaultUser));
    }

    @Test
    @DisplayName("Positive Login User")
    void loginUserTestPositive() throws DataAccessException {
        userService.register(defaultUser);
        AuthData authData = userService.login(defaultUser.username(), defaultUser.password());
        Assertions.assertEquals(authDAO.getAuth(authData.authToken()), authData);
    }

    @Test
    @DisplayName("Negative Login User")
    void loginUserTestNegative() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class, () -> userService.login(defaultUser.username(), defaultUser.password()));

        userService.register(defaultUser);
        UserData badPassUser = new UserData(defaultUser.username(), "wrongPass", defaultUser.email());
        Assertions.assertThrows(DataAccessException.class, () -> userService.login(badPassUser.username(), badPassUser.password()));
    }

    @Test
    @DisplayName("Positive Logout User")
    void logoutUserTestPositive() throws DataAccessException {
        AuthData auth = userService.register(defaultUser);
        userService.logout(auth.authToken());
        Assertions.assertNull(authDAO.getAuth(auth.authToken()), "Auth should be null after logout");
    }

    @Test
    @DisplayName("Negative Logout User")
    void logoutUserTestNegative() throws DataAccessException{
        AuthData auth = userService.register(defaultUser);
        Assertions.assertThrows(DataAccessException.class, () -> userService.logout("badAuthToken"));
    }

    @Test
    @DisplayName("Positive Clear DB")
    void clearTestPositive() throws DataAccessException {
        AuthData auth = userService.register(defaultUser);
        userService.clear();
        Assertions.assertNull(userDAO.getUser(defaultUser.username()), "User should be null after clear");
        Assertions.assertNull(authDAO.getAuth(auth.authToken()), "Auth should be null after clear");
    }

    @Test
    @DisplayName("Negative Clear DB")
    void clearTestNegative() throws DataAccessException {
        Assertions.assertDoesNotThrow(() -> userService.clear());
    }

}