package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.RegisterRequest;
import spark.*;
import service.*;
import response.ErrorResponse;

public class RegisterHandler implements Route {
    private final UserService userService;
    private final Gson gson;

    public RegisterHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }


    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

            if (request.username() == null || request.password() == null || request.email() == null ||
                    request.username().isBlank() || request.password().isBlank() || request.email().isBlank()) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error required fields missing"));
            }
            var userData = new model.UserData(request.username(), request.password(), request.email());
            var result = userService.register(userData);

            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(new ErrorResponse("Error username already taken"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error internal server"));
        }
    }
}

