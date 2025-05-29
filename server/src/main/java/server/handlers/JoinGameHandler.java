package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import request.JoinGameRequest;
import service.JoinGameService;
import spark.Request;
import spark.Response;

public class JoinGameHandler {
    JoinGameService service;

    public JoinGameHandler(AuthDAO authDAO, GameDAO gameDAO) {

        service = new JoinGameService(authDAO, gameDAO);
    }

    public String joinGame(Request req, Response res, Gson gson) throws DataAccessException {
        var body = gson.fromJson(req.body(), JoinGameRequest.class);
        var joinRequest = new JoinGameRequest(req.headers("authorization"), body.playerColor(), body.gameID());
        service.joinGame(joinRequest);

        return "{}";
    }
}