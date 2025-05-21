package server.handlers;

import com.google.gson.Gson;
import service.UserService;
import spark.*;

public class LogoutHandler implements Route {
    private final UserService userService;
    private final Gson gson;

    public LogoutHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String authToken = req.headers("Authorization");

        try {
            userService.logout(authToken);
            res.status(200);
            return gson.toJson(new SuccessResponse("Logged out successfully."));
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error unauthorized"));
        }
    }

    private record SuccessResponse(String message) {}
    private record ErrorResponse(String message) {}
}
