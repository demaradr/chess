package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.CreateGameRequest;
import response.CreateGameResponse;
import response.ErrorResponse;
import service.GameService;
import spark.*;

public class CreateGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson;

    public CreateGameHandler(GameService gameService, Gson gson) {
        this.gameService = gameService;
        this.gson = gson;
    }

    @Override
    public Object handle(Request req, Response res) {
        String authToken = req.headers("Authorization");

        try {
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error invalid auth token"));
            }

            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);
            if (request == null || request.gameName() == null || request.gameName().isBlank()) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error invalid game name"));
            }

            int gameID = gameService.createGame(authToken, request.gameName());

            res.status(200);
            return gson.toJson(new CreateGameResponse(gameID));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
        }
    }
}
