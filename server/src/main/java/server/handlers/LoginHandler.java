package server.handlers;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.LoginService;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.LoginRequest;
import spark.Request;
import spark.Response;

public class LoginHandler {
    LoginService service;

    public LoginHandler(UserDAO userDAO, AuthDAO authDAO) {

        service = new LoginService(userDAO, authDAO);
    }

    public String login(Request req, Response res, Gson gson) throws DataAccessException {
        var loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        var result = service.login(loginRequest);
        return gson.toJson(result);
    }
}