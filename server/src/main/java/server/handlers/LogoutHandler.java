package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.UserService;
import spark.*;
import response.ErrorResponse;
import response.SuccessResponse;

public class LogoutHandler implements Route {
    private final UserService userService;
    private final Gson gson;

    public LogoutHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    @Override
    public Object handle(Request req, Response res) {
        String authToken = req.headers("Authorization");

        if (authToken == null || authToken.isBlank()) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }

        try {
            userService.logout(authToken);
            res.status(200);
            return gson.toJson(new SuccessResponse("Logged out successfully."));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: server error."));
        }
    }
}
