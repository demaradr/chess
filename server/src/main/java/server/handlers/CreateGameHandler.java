package server.handlers;

import com.google.gson.Gson;
import service.GameService;
import spark.*;

public class CreateGameHandler implements Route {
    private final GameService gameService;
    public CreateGameHandler(GameService gameService, Gson gson) { this.gameService = gameService; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String authToken = req.headers("Authorization");
        var request = new Gson().fromJson(req.body(), request.CreateGameRequest.class);
        var result = gameService.createGame(authToken, request.gameName());
        res.status(result != null ? 200 : 401);
        return new Gson().toJson(result);
    }
}
