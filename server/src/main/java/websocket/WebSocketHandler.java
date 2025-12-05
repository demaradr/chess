package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import models.AuthData;
import models.GameData;
import org.jetbrains.annotations.Nullable;
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
                message = auth.username() + " connected as observer!\n";
            } else {
                message = auth.username() + " connected as " + color.name().toLowerCase() + "\n";
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
            Result result = getResult(session, move);
            if (result == null) {
                return;
            }
            ChessGame chessGame = result.game().game();
            if (chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) || chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                    chessGame.isInStalemate(ChessGame.TeamColor.BLACK) || chessGame.isInStalemate(ChessGame.TeamColor.WHITE)) {
                sendError(session, "Error: the game is over");
                return;

            }
            ChessGame.TeamColor color = null;
            if (result.auth().username().equals(result.game().whiteUsername())) {
                color = ChessGame.TeamColor.WHITE;
            }
            else if (result.auth().username().equals(result.game().blackUsername())) {
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
            if (chessGame.getTeamTurn() != color) {
                sendError(session, "Error: its not your turn");
                return;
            }
            boolean inCheck = chessGame.isInCheck(color);
            try {
                chessGame.makeMove(chessMove);
            }
            catch (InvalidMoveException ex){
                if (inCheck) {
                    sendError(session, "Error: You're in check - invalid move");
                }
                else {
                    sendError(session, "Error: invalid move");
                }
                return;
            }
            GameData update = new GameData(result.game().gameID(), result.game().whiteUsername(),
                    result.game().blackUsername(), result.game().gameName(), chessGame);
            gameDAO.updateGame(update);
            String moveDesc = move.getMoveDescription();
            String moveNoti = result.auth().username() + " made move: " + moveDesc + "\n";
            String loadMessage = gson.toJson(new LoadGameMessage(chessGame));
            connections.broadcast(result.game().gameID(), null, loadMessage);
            String notification = gson.toJson(new NotificationMessage(moveNoti));
            connections.broadcast(result.game().gameID(), session, notification);
            ChessGame.TeamColor turn = chessGame.getTeamTurn();
            String opponent = null;
            String player = null;

            if (turn == ChessGame.TeamColor.WHITE) {
                player = result.game().whiteUsername();
                opponent = result.game().blackUsername();
            }
            else if (turn == ChessGame.TeamColor.BLACK) {
                player = result.game().blackUsername();
                opponent = result.game().whiteUsername();
            }

            String statusNoti;
            if (chessGame.isInCheckmate(turn)) {
                if (opponent != null) {
                    String winMessage = opponent + " wins by checkmating " + player + "!\n";
                    String winNoti = gson.toJson(new NotificationMessage(winMessage));
                    connections.broadcast(result.game().gameID(), null, winNoti);
                }
            }
            else if (chessGame.isInStalemate(turn)) {
                statusNoti = gson.toJson(new NotificationMessage("It's a draw! Due to stalemate\n"));
                connections.broadcast(result.game().gameID(), null, statusNoti);
                if (player != null) {
                    statusNoti = gson.toJson(new NotificationMessage(player + " is in stalemate \n"));
                    connections.broadcast(result.game().gameID(), null, statusNoti);

                }            }
            else if (chessGame.isInCheck(turn)) {
                if (player != null) {
                    statusNoti = gson.toJson(new NotificationMessage(player + " is in check\n"));
                    connections.broadcast(result.game().gameID(), null, statusNoti);
                }
            }
        }
        catch (Exception ex) {
            sendError(session, ex.getMessage());
        }}

    @Nullable
    private Result getResult(Session session, MakeMoveCommand move) throws DataAccessException, IOException {
        AuthData auth = authDAO.getAuth(move.getAuthToken());
        GameData game = gameDAO.getGame(move.getGameID());

        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return null;
        }

        if (game == null) {
            sendError(session, "Error: no game exists");
            return null;
        }
        Result result = new Result(auth, game);
        return result;
    }

    private record Result(AuthData auth, GameData game) {
    }

    private void leave(Session session, UserGameCommand command) throws IOException {
        try {


            AuthData auth = authDAO.getAuth(command.getAuthToken());
            GameData game = gameDAO.getGame(command.getGameID());

            if (auth == null) {
                sendError(session, "Leave error: unauthorized");
                return;
            }

            if (game == null) {
                sendError(session, "Leave error: no game exists");
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
            String message = username + " left the game!\n";
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
                sendError(session, "Resign error: unauthorized");
                return;
            }

            if (game == null) {
                sendError(session, "Resign error: no game exists");
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

            String message = auth.username() + " resigned! \n";
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
