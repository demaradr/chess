package client;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerFacadeTests {

    private Server server;
    private ServerFacade facade;

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeAll
    void startServer() throws DataAccessException {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
        System.out.println("Started test HTTP server on port " + port);
    }

    @AfterAll
    void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        authDAO = new MySQLAuthDAO();
        gameDAO = new MySQLGameDAO();
        userDAO = new MySQLUserDAO();
        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }

    @Test
    void register_validUser_succeeds() {
        UserData user = new UserData("test_user", "pass", "test.com");
        assertDoesNotThrow(() -> facade.register(user));
    }

    @Test
    void register_duplicateUser_fails() {
        UserData user = new UserData("test_user", "pass", "test.com");
        assertDoesNotThrow(() -> facade.register(user));
        ResultException e = assertThrows(ResultException.class, () -> facade.register(user));
        assertEquals(403, e.statusCode());
    }
}