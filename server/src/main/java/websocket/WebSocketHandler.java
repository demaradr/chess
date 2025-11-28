package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import models.AuthData;
import models.GameData;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }


    @Override
    public void handleConnect(WsConnectContext context) {
        System.out.println("Websocket connected!");
        context.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext context) {
        Session session = context.session;
        UserGameCommand command = gson.fromJson(context.message(), UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connectUser(session, command);
        }
    }

    @Override
    public void handleClose(WsCloseContext context) {
        System.out.println("WebSocket closed!");
    }


    private void connectUser(Session session, UserGameCommand command) {
        try {
            if (command.getAuthToken() == null) {
                sendError(session, "Error: unauthorized");
            }

            AuthData auth = authDAO.getAuth(command.getAuthToken());

            if (command.getGameID() == null) {
                sendError(session, "Error: invalid game");
            }

            GameData game = gameDAO.getGame(command.getGameID());

            ChessGame.TeamColor color = null;

            if (auth.username().equals(game.whiteUsername())) {
                color = ChessGame.TeamColor.WHITE;
            } else if (auth.username().equals(game.blackUsername())) {
                color = ChessGame.TeamColor.BLACK;
            }

            connections.add(new ConnectionManager.Connection(session,
                    game.gameID(),
                    auth.username(),
                    color,
                    color == null));
            session.getRemote().sendString(gson.toJson(new LoadGameMessage(game)));
            String message = "";
            if (color == null) {
                message = auth.username() + " connected as observer!";
            } else {
                message = auth.username() + "connected as " + color.name().toLowerCase();
            }

            String notification = gson.toJson(new NotificationMessage(message));
            for (ConnectionManager.Connection conn : connections.allConnections()) {
                if (conn.gameID() == game.gameID() && conn.session().isOpen()) {
                    conn.session().getRemote().sendString(notification);
                }
            }
        }
        catch (Exception ex) {
            sendError(session, "Error: " + ex.getMessage());
        }

    }


    private void sendError(Session session, String error) {
        session.getRemote().sendString(gson.toJson(new ErrorMessage(error)));
    }
}
