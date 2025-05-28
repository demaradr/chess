package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import request.ListGamesRequest;
import response.ErrorResponse;
import service.ListGamesService;
import spark.Request;
import spark.Response;

public class ListGamesHandler {
    private final ListGamesService listGamesService;
    private final Gson gson;

    public ListGamesHandler(AuthDAO authDAO, GameDAO gameDAO, Gson gson) {
        this.listGamesService = new ListGamesService(authDAO, gameDAO);
        this.gson = gson;
    }

    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            var request = new ListGamesRequest(authToken);
            var result = listGamesService.listGames(request);

            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
