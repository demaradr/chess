package handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import io.javalin.http.Context;
import service.LoginService;
import service.ServiceException;
import request.LoginRequest;
import results.LoginResult;

public class LoginHandler {


    private final LoginService loginService;
    private final Gson gson = new Gson();

    public LoginHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.loginService = new LoginService(userDAO, authDAO);
    }



    public void login(Context context) {
        try {
            LoginRequest request = gson.fromJson(context.body(), LoginRequest.class);
            LoginResult result = loginService.login(request);
            context.status(200);
            context.result(gson.toJson(result));
            context.contentType("application/json");

        }
        catch (ServiceException e) {
            String message = e.getMessage();
            if (message.contains("bad request")) {
                context.status(400);
            } else if (message.contains("unauthorized")) {
                context.status(401);
            } else {
                context.status(500);
            }
            context.result(gson.toJson(new ClearHandler.ErrorResponse(message)));
            context.contentType("application/json");


        }
        catch (Exception e) {

            context.status(500);
            context.result(gson.toJson(new ClearHandler.ErrorResponse("Error: " + e.getMessage())));
            context.contentType("application/json");
        }
    }


}
