package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import request.RegisterRequest;
import results.RegisterResult;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        var url = "http://localhost/" + port;
        facade = new ServerFacade(url);
        System.out.println("Started test HTTP server on " + port);
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

}
