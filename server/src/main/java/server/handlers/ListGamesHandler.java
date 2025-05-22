package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.GameService;
import spark.*;
import response.ErrorResponse;

public class ListGamesHandler implements Route {
    private final GameService gameService;
    private final Gson gson;

    public ListGamesHandler(GameService gameService, Gson gson) {
        this.gameService = gameService;
        this.gson = gson;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error invalid Authorization header"));
            }

            var result = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error unauthorized " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
        }
    }
}
