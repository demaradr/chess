package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import spark.*;
import service.*;
import request.LoginRequest;
import response.ErrorResponse;

public class LoginHandler implements Route {
    private final UserService userService;
    private final Gson gson;

    public LoginHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);
            if (request.username() == null || request.password() == null ||
                    request.username().isBlank() || request.password().isBlank()) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error missing username or password"));
            }
            var result = userService.login(request.username(), request.password());
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Internal server error"));
        }
    }

}

