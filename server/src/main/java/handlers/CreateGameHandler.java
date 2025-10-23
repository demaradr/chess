package handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.http.Context;
import service.CreateGameService;
import service.ServiceException;
import service.request.CreateGameRequest;
import service.results.CreateGameResult;

public class CreateGameHandler {

    private final CreateGameService createGameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.createGameService = new CreateGameService(gameDAO, authDAO);
    }


    public void createGame(Context context) {
        try {
            String authToken = context.header("authorization");
            CreateGameRequest request = gson.fromJson(context.body(), CreateGameRequest.class);
            CreateGameResult result = createGameService.createGame(request, authToken);
            context.status(200);
            context.result(gson.toJson(result));
            context.contentType("application/json");
        }
        catch (ServiceException error) {
            String message = error.getMessage();
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
        catch (Exception error) {
            context.status(500);
            context.result(gson.toJson(new ClearHandler.ErrorResponse("Error: " + error.getMessage())));
            context.contentType("application/json");
        }
        }
    }
