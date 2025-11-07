package handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import io.javalin.http.Context;
import service.RegisterService;
import service.ServiceException;
import request.RegisterRequest;
import results.RegisterResult;

public class RegisterHandler {

    private final RegisterService registerService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.registerService = new RegisterService(userDAO, authDAO);
    }

    public void register(Context context) {
        try {
            RegisterRequest request = gson.fromJson(context.body(), RegisterRequest.class);
            RegisterResult result = registerService.register(request);
            context.status(200);
            context.result(gson.toJson(result));
            context.contentType("application/json");
        }
        catch (ServiceException e) {
            String message = e.getMessage();
            if (message.contains("bad request")) {
                context.status(400);
            }
            else if (message.contains("already exists")) {
                context.status(403);
            }
            else {
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
