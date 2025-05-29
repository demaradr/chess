package server.handlers;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import service.ListGamesService;
import com.google.gson.Gson;
import dataaccess.DataAccessException;

import request.ListGamesRequest;
import spark.Request;
import spark.Response;

public class ListGamesHandler {
    ListGamesService service;

    public ListGamesHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.service = new ListGamesService(authDAO, gameDAO);
    }

    public String listGames(Request req, Response res, Gson gson) throws DataAccessException {
        var listRequest = new ListGamesRequest(req.headers("authorization"));
        var result = service.listGames(listRequest);
        return gson.toJson(result);
    }

}