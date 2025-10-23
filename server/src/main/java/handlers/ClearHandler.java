package handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.clearService = new ClearService(userDAO, gameDAO, authDAO);

    }


    public void clear(Context context) {
        try {
            clearService.clear();
            context.status(200);
            context.result(gson.toJson(new ClearResponse()));
            context.contentType("application/json");
        }
        catch (Exception excep) {
            context.status(500);
            context.result(gson.toJson(new ErrorResponse(excep.getMessage())));
            context.contentType("application/json");
        }

    }

    public record ClearResponse() {}

    public record ErrorResponse(String message) {}

}
