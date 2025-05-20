package server.handlers;

import com.google.gson.Gson;
import spark.*;
import service.*;

public class RegisterHandler implements Route {
    private final UserService userService;
    public RegisterHandler(UserService userService, Gson gson) { this.userService = userService; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        var request = new Gson().fromJson(req.body(), request.RegisterRequest.class);
        var userData = new model.UserData(request.username(), request.password(), request.email());
        var result = userService.register(userData);
        res.status(result != null ? 200 : 400);
        return new Gson().toJson(result);
    }
}
