package server.handlers;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import chess.ChessMove;
import websocket.messages.ClientMessage;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;
import websocket.messages.ClientMessage.ClientMessageType;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebsocketHandler {

    private static final ConcurrentHashMap<Session, String> sessionAuthTokens = new ConcurrentHashMap<>();
    private final MySQLAuthDAO authDAO = new MySQLAuthDAO();
    private final MySQLGameDAO gameDAO = new MySQLGameDAO();
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket Connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Received: " + message);

        try {
            ClientMessage clientMessage = gson.fromJson(message, ClientMessage.class);
            AuthData authData = authDAO.authenticate(clientMessage.getAuthToken());
            sessionAuthTokens.put(session, clientMessage.getAuthToken());

            String response = switch (clientMessage.getCommandType()) {
                case CONNECT -> handleConnect(authData, clientMessage);
                case MAKE_MOVE -> handleMakeMove(authData, clientMessage);
                case RESIGN -> handleResign(authData, clientMessage);
                case LEAVE -> handleLeave(authData, clientMessage);
                default -> errorMessage("Invalid command type.");
            };

            session.getRemote().sendString(response);

        } catch (UnauthorizedException e) {
            session.getRemote().sendString(errorMessage("Unauthorized: " + e.getMessage()));
        } catch (DataAccessException e) {
            session.getRemote().sendString(errorMessage("Data access error: " + e.getMessage()));
        } catch (IllegalArgumentException | InvalidMoveException e) {
            session.getRemote().sendString(errorMessage("Invalid move: " + e.getMessage()));
        } catch (Exception e) {
            session.getRemote().sendString(errorMessage("Server error: " + e.getMessage()));
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket Closed: " + session);
        sessionAuthTokens.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private String handleConnect(AuthData authData, ClientMessage msg) throws DataAccessException {
        GameData gameData = gameDAO.getGame(authData, msg.getGameID());
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.LOAD_GAME, "Game loaded", gameData.game());
        return gson.toJson(serverMessage);
    }

    private String handleMakeMove(AuthData authData, ClientMessage msg) throws DataAccessException, InvalidMoveException {
        GameData gameData = gameDAO.getGame(authData, msg.getGameID());
        ChessMove move = msg.getMove();

        if (move == null) {
            return errorMessage("Missing move data.");
        }

        ChessGame game = gameData.game();
        boolean moveResult = game.makeMove(move);

        if (!moveResult) {
            return errorMessage("Illegal move.");
        }

        GameData updatedGameData = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        gameDAO.updateGame(updatedGameData);
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION, "Move accepted", game);
        return gson.toJson(serverMessage);
    }

    private String handleResign(AuthData authData, ClientMessage msg) throws DataAccessException {
        GameData gameData = gameDAO.getGame(authData, msg.getGameID());
        ChessGame game = gameData.game();

        // Resign logic (for now just notify)
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION, authData.username() + " resigned", game);
        return gson.toJson(serverMessage);
    }

    private String handleLeave(AuthData authData, ClientMessage msg) {
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION,
                authData.username() + " left the game.", null);
        return gson.toJson(serverMessage);
    }

    private String errorMessage(String message) {
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.ERROR, message, null);
        return gson.toJson(serverMessage);
    }
}
