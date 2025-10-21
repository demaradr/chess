package handlers;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.http.Context;
import service.*;
import service.results.ListGamesResult;

public class ListGamesHandler {


    private final ListGamesService listGamesService;
    public ListGamesHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.listGamesService = new ListGamesService(gameDAO, authDAO);
    }

    public void list(Context context) {
        try {
            String authToken = context.header("authorization");
            ListGamesResult result = listGamesService.listGames(authToken);
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
        catch (Exception error) {

            context.status(500);
            context.json(new ErrorResponse("Error: " + error.getMessage()));
        }
    }

    public record ErrorResponse(String message) {}
}
