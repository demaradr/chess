package server.handlers;

import com.google.gson.Gson;
import response.ErrorResponse;
import service.GameService;
import spark.*;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    public JoinGameHandler(GameService gameService, Gson gson) { this.gameService = gameService; }

    public Object handle(Request req, Response res) throws Exception {
        String authToken = req.headers("Authorization");
        var request = new Gson().fromJson(req.body(), request.JoinGameRequest.class);

        try {
            gameService.joinGame(authToken, request.gameID(), request.playerColor());
            res.status(200);
            return "Successfully joined game.";
        } catch (Exception e) {
            res.status(403);
            return new Gson().toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
