package server.handlers;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import service.ClearService;
import dataaccess.DataAccessException;

import spark.Request;
import spark.Response;

public class ClearHandler {
    ClearService service;

    public ClearHandler(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO) {

        service = new ClearService(authDAO, gameDAO, userDAO);
    }

    public String clear(Request req, Response res) throws DataAccessException {
        service.clear();
        return "{}";
    }
}