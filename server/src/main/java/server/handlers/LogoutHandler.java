package server.handlers;

import dataaccess.AuthDAO;
import service.LogoutService;
import dataaccess.DataAccessException;

import request.LogoutRequest;
import spark.Request;
import spark.Response;

public class LogoutHandler {
    LogoutService service;

    public LogoutHandler(AuthDAO authDAO) {
        service = new LogoutService(authDAO);
    }

    public String logout(Request req, Response res) throws DataAccessException {
        var logoutRequest = new LogoutRequest(req.headers("authorization"));
        service.logout(logoutRequest);
        return "{}";
    }
}