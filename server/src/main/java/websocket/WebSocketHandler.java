package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import models.AuthData;
import models.GameData;
import websocket.commands.MakeMoveCommand;
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
    public void handleMessage(WsMessageContext context) throws IOException {
        Session session = context.session;
        String message = context.message();
        UserGameCommand command = gson.fromJson(context.message(), UserGameCommand.class);


        switch (command.getCommandType()) {
            case CONNECT -> connectUser(session, command);
            case MAKE_MOVE -> makeMove(session, message);
            case LEAVE -> leave(session, command);
            case RESIGN -> resign(session, command);
        }
    }

    @Override
    public void handleClose(WsCloseContext context) {
        System.out.println("WebSocket closed!");
    }


    private void connectUser(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            GameData game = gameDAO.getGame(command.getGameID());

            if (auth == null) {
                sendError(session, "Error: unauthorized");
                return;
            }

            if (game == null) {
                sendError(session, "Error: no game exists");
                return;
            }

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


            session.getRemote().sendString(gson.toJson(new LoadGameMessage(game.game())));
            String message;
            if (color == null) {
                message = auth.username() + " connected as observer!";
            } else {
                message = auth.username() + "connected as " + color.name().toLowerCase();
            }

            String notification = gson.toJson(new NotificationMessage(message));
            connections.broadcast(game.gameID(), session, notification);
        }
        catch (Exception ex) {
            sendError(session, "Error: " + ex.getMessage());
        }

    }

    private void makeMove(Session session, String json) throws IOException {
        try {
            MakeMoveCommand move = gson.fromJson(json, MakeMoveCommand.class);
            AuthData auth = authDAO.getAuth(move.getAuthToken());
            GameData game = gameDAO.getGame(move.getGameID());

            if (auth == null) {
                sendError(session, "Error: unauthorized");
                return;
            }

            if (game == null) {
                sendError(session, "Error: no game exists");
                return;
            }

            if (game.whiteUsername() == null && game.blackUsername() == null) {
                sendError(session, "Error: the game is over");
            }


            ChessGame.TeamColor color = null;
            if (auth.username().equals(game.whiteUsername())) {
                color = ChessGame.TeamColor.WHITE;

            }
            else if (auth.username().equals(game.blackUsername())) {
                color = ChessGame.TeamColor.BLACK;
            }

            if (color == null) {
                sendError(session, "Error: observers can't move");
                return;
            }

            ChessMove chessMove = move.getMove();
            if (chessMove == null) {
                sendError(session, "Error: bad move");
                return;
            }

            ChessGame chessGame = game.game();

            if (chessGame.getTeamTurn() != color) {
                sendError(session, "Error: its not your turn");
                return;
            }
            try {
                chessGame.makeMove(chessMove);
            }
            catch (InvalidMoveException ex){
                sendError(session, "Error: invalid move");
                return;
            }


            GameData update = new GameData(game.gameID(), game.whiteUsername(),
                    game.blackUsername(), game.gameName(), chessGame);
            gameDAO.updateGame(update);

            String moveDesc = move.getMoveDescription();
            String moveNoti = auth.username() + " made move: " + moveDesc;
            String loadMessage = gson.toJson(new LoadGameMessage(chessGame));
            connections.broadcast(game.gameID(), null, loadMessage);
            String notification = gson.toJson(new NotificationMessage(moveNoti));
            connections.broadcast(game.gameID(), session, notification);

            ChessGame.TeamColor turn = chessGame.getTeamTurn();
            String opponent = null;

            if (turn == ChessGame.TeamColor.WHITE) {
                opponent = game.whiteUsername();
            }
            else if (turn == ChessGame.TeamColor.BLACK) {
                opponent = game.blackUsername();
            }

            String statusNoti;

            if (chessGame.isInCheckmate(turn)) {
                statusNoti = gson.toJson(new NotificationMessage(opponent + "is in checkmate!"));

            }
            else if (chessGame.isInStalemate(turn)) {
                statusNoti = gson.toJson(new NotificationMessage(opponent + "is in stalemate!"));
            }
            else if (chessGame.isInCheck(turn)) {
                statusNoti = gson.toJson(new NotificationMessage(opponent + "is in check!"));
            }
            else statusNoti = null;

            if (statusNoti != null) {
                connections.broadcast(game.gameID(), null, statusNoti);
            }

        }
        catch (Exception ex) {
            sendError(session, ex.getMessage());
        }
    }

    private void leave(Session session, UserGameCommand command) throws IOException {
        try {


            AuthData auth = authDAO.getAuth(command.getAuthToken());
            GameData game = gameDAO.getGame(command.getGameID());

            if (auth == null) {
                sendError(session, "Error: unauthorized");
                return;
            }

            if (game == null) {
                sendError(session, "Error: no game exists");
                return;
            }


            ChessGame.TeamColor color = null;
            if (auth.username().equals(game.whiteUsername())) {
                color = ChessGame.TeamColor.WHITE;
            }
            else if (auth.username().equals(game.blackUsername())) {
                color = ChessGame.TeamColor.BLACK;
            }

            if (color != null) {
                String white = (color == ChessGame.TeamColor.WHITE) ? null : game.whiteUsername();
                String black = (color == ChessGame.TeamColor.BLACK) ? null: game.blackUsername();

                GameData update = new GameData(game.gameID(), white, black, game.gameName(), game.game());
                gameDAO.updateGame(update);
            }

            connections.remove(session);

            String username = auth.username();
            String message = username + "left the game!";
            String notification = gson.toJson(new NotificationMessage(message));
            connections.broadcast(game.gameID(), session, notification);


        }
        catch (Exception ex) {
            sendError(session, "Error: " + ex.getMessage());
        }
    }

    private void resign(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            GameData game = gameDAO.getGame(command.getGameID());

            if (auth == null) {
                sendError(session, "Error: unauthorized");
                return;
            }

            if (game == null) {
                sendError(session, "Error: no game exists");
                return;
            }

            ChessGame.TeamColor color = null;

            if (auth.username().equals(game.whiteUsername())) {
                color = ChessGame.TeamColor.WHITE;
            }
            else if (auth.username().equals(game.blackUsername())) {
                color = ChessGame.TeamColor.BLACK;
            }

            if (color == null) {
                sendError(session, "Error: observers can't resign");
                return;
            }

            GameData update = new GameData(game.gameID(), null, null, game.gameName(), game.game());
            gameDAO.updateGame(update);

            String message = auth.username() + "resigned!";
            String notification = gson.toJson(new NotificationMessage(message));
            connections.broadcast(game.gameID(), null, notification);

        }
        catch (Exception ex) {
            sendError(session, "Error: " + ex.getMessage());
        }
    }


    private void sendError(Session session, String error) throws IOException {
        session.getRemote().sendString(gson.toJson(new ErrorMessage(error)));
    }
}
