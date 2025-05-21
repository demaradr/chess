package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.JoinGameRequest;
import response.ErrorResponse;
import response.SuccessResponse;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson;

    public JoinGameHandler(GameService gameService, Gson gson) {
        this.gameService = gameService;
        this.gson = gson;
    }

    @Override
    public Object handle(Request req, Response res) {
        String authToken = req.headers("Authorization");

        try {
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Missing or invalid auth token"));
            }

            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);
            if (request == null || request.gameID() == 0) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Missing or invalid game ID"));
            }

            gameService.joinGame(authToken, request.gameID(), request.playerColor());

            res.status(200);
            return gson.toJson(new SuccessResponse("Successfully joined game"));

        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
