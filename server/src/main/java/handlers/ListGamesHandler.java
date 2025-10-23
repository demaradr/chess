package handlers;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.http.Context;
import service.*;
import service.results.ListGamesResult;

public class ListGamesHandler {


    private final ListGamesService listGamesService;
    private final Gson gson = new Gson();
    public ListGamesHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.listGamesService = new ListGamesService(gameDAO, authDAO);
    }

    public void list(Context context) {
        try {
            String authToken = context.header("authorization");
            ListGamesResult result = listGamesService.listGames(authToken);
            context.status(200);
            context.result(gson.toJson(result));
            context.contentType("application/json");
        }
        catch (ServiceException error) {
            String message = error.getMessage();


            if (message.contains("unauthorized")) {
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
