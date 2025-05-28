package server;

import static spark.Spark.*;
import com.google.gson.*;
import dataaccess.*;
import service.*;
import server.handlers.*;
import spark.Spark;

public class Server {

    private final Gson gson = new Gson();

    public Server() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            System.err.println("Failed to create database: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        UserDAO userDAO = new MySQLUserDAO();
        AuthDAO authDAO = new MySQLAuthDAO();
        GameDAO gameDAO = new MySQLGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        post("/user", new RegisterHandler(userService, gson));
        post("/session", new LoginHandler(userService, gson));
        delete("/session", new LogoutHandler(userService, gson));

        get("/game", new ListGamesHandler(authDAO, gameDAO, gson)::handle);
        post("/game", new CreateGameHandler(gameService, gson));
        put("/game", new JoinGameHandler(gameService, gson, authDAO));

        delete("/db", new ClearHandler(userDAO, authDAO, gameDAO));

        init();
        awaitInitialization();
        return port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
