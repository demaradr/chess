package server.handlers;

import com.google.gson.Gson;
import spark.*;
import service.*;

public class RegisterHandler implements Route {
    private final UserService userService;
    public RegisterHandler(UserService userService) { this.userService = userService; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        var request = new Gson().fromJson(req.body(), model.request.RegisterRequest.class);
        var result = userService.register(request);
        res.status(result.success() ? 200 : 400);
        return new Gson().toJson(result);
    }
}
