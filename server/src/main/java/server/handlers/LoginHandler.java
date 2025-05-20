package server.handlers;

import com.google.gson.Gson;
import service.*;
import spark.*;

public class LoginHandler implements Route {
    private final UserService userService;
    public LoginHandler(UserService userService, Gson gson) { this.userService = userService; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        var request = new Gson().fromJson(req.body(), request.LoginRequest.class);
        var result = userService.login(request.username(), request.password());
        res.status(result != null ? 200 : 401);
        return new Gson().toJson(result);
    }
}
