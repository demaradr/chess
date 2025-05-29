package server.handlers;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import request.CreateGameRequest;
import service.CreateGameService;
import spark.Request;
import spark.Response;

public class CreateGameHandler {
    private CreateGameService service;

    public CreateGameHandler(AuthDAO authDAO, GameDAO gameDAO) {
        service = new CreateGameService(authDAO, gameDAO);
    }

    public String createGame(Request req, Response res, Gson gson) throws DataAccessException {
        var body = gson.fromJson(req.body(), CreateGameRequest.class);
        var createRequest = new CreateGameRequest(req.headers("authorization"), body.gameName());

        var result = service.createGame(createRequest);
        return gson.toJson(result);
    }
}