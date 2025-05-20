package server.handlers;

import com.google.gson.Gson;
import service.UserService;
import spark.*;

public class LogoutHandler implements Route {
    private final UserService userService;
    public LogoutHandler(UserService userService, Gson gson) { this.userService = userService; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String authToken = req.headers("Authorization");
        var result = userService.logout(authToken);
        res.status(result.success() ? 200 : 401);
        return new Gson().toJson(result);
    }
}
