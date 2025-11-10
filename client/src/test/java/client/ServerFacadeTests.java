package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import request.RegisterRequest;
import results.RegisterResult;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        var url = "http://localhost:" + port;
        facade = new ServerFacade(url);
        System.out.println("Started test HTTP server on " + port);
    }
    @BeforeEach
    void clear() throws ResponseException {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerPositive() throws Exception {
        RegisterResult user = facade.register("jon", "pass", "snow@gmail.com");
        assertEquals("jon", user.username());


    }


    @Test
    public void registerNegative() throws Exception {
        RegisterResult user = facade.register("jon", "pass", "snow@gmail.com");
        assertThrows(ResponseException.class, () -> facade.register("jon", "pass2", "snow@gmail.com"));


    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("mortadelo", "pass", "test@email.com");
        var loggedIn = facade.login("mortadelo", "pass");
        assertEquals("mortadelo", loggedIn.username());


    }

    @Test
    public void loginNegative() throws Exception {
        facade.register("filemon", "pass", "test@email.com");
        assertThrows(ResponseException.class, () -> facade.login("filemon", "wrong"));


    }


    @Test
    public void logoutPositive() throws Exception {
        facade.register("mortadelo", "pass", "test@email.com");
        var loggedIn = facade.login("mortadelo", "pass");
        assertDoesNotThrow(() -> facade.logout());


    }

    @Test
    public void logoutNegative() {
        assertThrows(ResponseException.class, () -> facade.logout());

    }


    @Test
    void createGamePositive() throws Exception {
        facade.register("test", "pass","email");
        var game = facade.createGame("Test");
        assertNotNull(game);
    }


    @Test
    void createGameNegative() {
        assertThrows(ResponseException.class, () -> facade.createGame("Test"));
    }



    @Test
    void listGamesPositive() throws ResponseException {

        facade.register("test","pass","mail");
        facade.createGame("Test");
        facade.createGame("Test2");
        facade.createGame("Test3");
        facade.createGame("Test4");

        var listGames = facade.listGames();
        assertEquals(4, listGames.games().size());
    }


    @Test
    void listGamesNegative() {
        assertThrows(ResponseException.class, () -> facade.listGames());
    }
}
