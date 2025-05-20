package server.handlers;

import com.google.gson.Gson;
import service.GameService;
import spark.*;

public class ListGamesHandler implements Route {
    private final GameService gameService;
    public ListGamesHandler(GameService gameService, Gson gson) { this.gameService = gameService; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String authToken = req.headers("Authorization");
        var result = gameService.listGames(authToken);
        res.status(result != null ? 200 : 401);
        return new Gson().toJson(result);
    }
}
