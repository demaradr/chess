package handlers;
import dataaccess.AuthDAO;
import io.javalin.http.Context;
import service.*;
import service.request.LogoutRequest;
import service.results.LogoutResult;

public class LogoutHandler {


    private final LogoutService logoutService;
    public LogoutHandler(AuthDAO authDAO) {
        this.logoutService = new LogoutService(authDAO);
    }

    public void logout(Context context) {
        try {
            String authToken = context.header("authorization");
            LogoutRequest request = new LogoutRequest(authToken);
            LogoutResult result = logoutService.logout(request);

            context.status(200);
            context.json(result);
        }
        catch (ServiceException error) {
            String message = error.getMessage();

            if (message.contains("unauthorized")) {
                context.status(401);
            } else {
                context.status(500);
            }
            context.json(new ErrorResponse(message));
        }
    }

    public record ErrorResponse(String message) {};

}
