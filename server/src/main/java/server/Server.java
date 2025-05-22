package server;

import static spark.Spark.*;
import com.google.gson.*;
import dataaccess.*;
import service.*;
import server.handlers.*;
import spark.Spark;

public class Server {

    private final Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        post("/user", new RegisterHandler(userService, gson));
        post("/session", new LoginHandler(userService, gson));
        delete("/session", new LogoutHandler(userService, gson));

        get("/game", new ListGamesHandler(gameService, gson));
        post("/game", new CreateGameHandler(gameService, gson));
        put("/game", new JoinGameHandler(gameService, gson, authDAO));

        delete("/db", new ClearHandler(userDAO, authDAO, gameDAO));



        // Start the server
        init();
        awaitInitialization();
        return port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
