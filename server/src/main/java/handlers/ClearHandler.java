package handlers;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.clearService = new ClearService(userDAO, gameDAO, authDAO);

    }


    public void clear(Context context) {
        try {
            clearService.clear();
            context.status(200);
            context.json(new ClearResponse());
        }
        catch (DataAccessException error) {
            context.status(500);
            context.json(new ErrorResponse(error.getMessage()));
        }
        catch (Exception excep) {
            context.status(500);
            context.json(new ErrorResponse(excep.getMessage()));
        }

    }

    public record ClearResponse() {}

    public record ErrorResponse(String message) {}

}
