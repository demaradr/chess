package server.handlers;

import com.google.gson.Gson;
import service.GameService;
import spark.*;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    public JoinGameHandler(GameService gameService, Gson gson) { this.gameService = gameService; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String authToken = req.headers("Authorization");
        var request = new Gson().fromJson(req.body(), request.JoinGameRequest.class);
        var result = gameService.joinGame(authToken, request.gameID(), request.playerColor());
        res.status(result != null ? 200 : 403);
        return new Gson().toJson(result);
    }
}
