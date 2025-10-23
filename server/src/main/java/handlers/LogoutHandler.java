package handlers;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import io.javalin.http.Context;
import service.*;
import service.request.LogoutRequest;
import service.results.LogoutResult;

public class LogoutHandler {


    private final LogoutService logoutService;
    private final Gson gson = new Gson();
    public LogoutHandler(AuthDAO authDAO) {
        this.logoutService = new LogoutService(authDAO);
    }

    public void logout(Context context) {
        try {
            String authToken = context.header("authorization");
            LogoutRequest request = new LogoutRequest(authToken);
            LogoutResult result = logoutService.logout(request);

            context.status(200);
            context.result(gson.toJson(result));
            context.contentType("application/json");
        }
        catch (ServiceException error) {
            String message = error.getMessage();

            if (message.contains("unauthorized") && !message.isEmpty()) {
                context.status(401);
            } else {
                context.status(500);
            }
            context.result(gson.toJson(new ErrorResponse(message)));
            context.contentType("application/json");
        }
        catch (Exception error) {
            context.status(500);
            context.result(gson.toJson(new ErrorResponse("Error: " + error.getMessage())));
            context.contentType("application/json");
        }
    }

    public record ErrorResponse(String message) {}

}
