package handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.http.Context;
import service.JoinGameService;
import service.request.JoinGameRequest;
import service.results.JoinGameResult;
import service.ServiceException;

public class JoinGameHandler {

    private final JoinGameService joinGameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.joinGameService = new JoinGameService(gameDAO, authDAO);
    }


    public void joinGame(Context context) {
        try {
            String authToken = context.header("authorization");
            JoinGameRequest request = gson.fromJson(context.body(), JoinGameRequest.class);
            JoinGameResult result = joinGameService.joinGame(request, authToken);
            context.status(200);
            context.json(result);
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
            context.json(new ClearHandler.ErrorResponse(message));
        }
        catch (Exception e) {
            context.status(500);
            context.json(new ClearHandler.ErrorResponse("Error: " + e.getMessage()));
        }
    }

}
