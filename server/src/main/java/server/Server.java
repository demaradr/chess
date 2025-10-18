package server;

import dataaccess.*;
import handlers.ClearHandler;
import io.javalin.*;

public class Server {

    private final Javalin javalin;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final ClearHandler clearHandler;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        clearHandler = new ClearHandler(userDAO, gameDAO, authDAO);

        // Register your endpoints and exception handlers here.

        registerEnpoints();

    }

    private void registerEnpoints() {
        javalin.delete("/db", clearHandler::clear);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
