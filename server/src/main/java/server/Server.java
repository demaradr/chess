package server;

import dataaccess.*;
import handlers.*;
import io.javalin.*;
import websocket.WebSocketHandler;

public class Server {

    private final Javalin javalin;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final ClearHandler clearHandler;
    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final ListGamesHandler listGamesHandler;
    private final CreateGameHandler createGameHandler;
    private final JoinGameHandler joinGameHandler;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        try {
            userDAO = new MySqlUserDAO();
            gameDAO = new MySqlGameDAO();
            authDAO = new MySqlAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        clearHandler = new ClearHandler(userDAO, gameDAO, authDAO);
        registerHandler = new RegisterHandler(userDAO, authDAO);
        loginHandler = new LoginHandler(userDAO, authDAO);
        logoutHandler = new LogoutHandler(authDAO);
        listGamesHandler = new ListGamesHandler(gameDAO, authDAO);
        createGameHandler = new CreateGameHandler(gameDAO, authDAO);
        joinGameHandler = new JoinGameHandler(gameDAO, authDAO);
        webSocketHandler = new WebSocketHandler(authDAO, gameDAO);



        // Register your endpoints and exception handlers here.

        registerEndpoints();

    }

    private void registerEndpoints() {
        javalin.delete("/db", clearHandler::clear);
        javalin.post("/user", registerHandler::register);
        javalin.post("/session", loginHandler::login);
        javalin.delete("/session", logoutHandler::logout);
        javalin.get("/game", listGamesHandler::list);
        javalin.post("/game", createGameHandler::createGame);
        javalin.put("/game", joinGameHandler::joinGame);
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
