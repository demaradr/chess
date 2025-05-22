package server.handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
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
    private final AuthDAO authDAO;

    public JoinGameHandler(GameService gameService, Gson gson, AuthDAO authDAO) {
        this.gameService = gameService;
        this.gson = gson;
        this.authDAO = authDAO;
    }

    @Override
    public Object handle(Request req, Response res) {
        String authToken = req.headers("Authorization");

        try {
            if (authToken == null || authToken.isBlank() || authDAO.getAuth(authToken) == null) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: Unauthorized"));
            }

            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

            if (request == null || request.gameID() == null || request.gameID() <= 0) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: invalid or missing game ID"));
            }

            if (request.playerColor() != null &&
                    !(request.playerColor().equalsIgnoreCase("WHITE") || request.playerColor().equalsIgnoreCase("BLACK"))) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: invalid player color"));
            }

            gameService.joinGame(authToken, request.gameID(), request.playerColor());

            res.status(200);
            return gson.toJson(new SuccessResponse("Successfully joined game"));

        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
