package server.handlers;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.RegisterService;
import com.google.gson.Gson;
import dataaccess.DataAccessException;

import model.UserData;
import request.RegisterRequest;
import spark.Request;
import spark.Response;

public class RegisterHandler {
    RegisterService service;

    public RegisterHandler(UserDAO userDAO, AuthDAO authDAO) {

        service = new RegisterService(userDAO, authDAO);
    }

    public String register(Request req, Response res, Gson gson) throws DataAccessException {
        var userData = gson.fromJson(req.body(), UserData.class);
        var registerRequest = new RegisterRequest(userData);

        var result = service.register(registerRequest);
        return gson.toJson(result);

    }
}