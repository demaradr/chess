package server.handlers;

import chess.ChessPosition;
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
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebsocketHandler {

    private static final ConcurrentHashMap<Session, String> SESSION_AUTH_TOKENS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> SESSION_GAME_ID = new ConcurrentHashMap<>();
    private final MySQLAuthDAO authDAO = new MySQLAuthDAO();
    private final MySQLGameDAO gameDAO = new MySQLGameDAO();
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        session.setIdleTimeout(30 * 60 * 1000);
        System.out.println("WebSocket Connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Received: " + message);

        try {
            if (message == null || message.trim().isEmpty() || !message.trim().startsWith("{")) {
                session.getRemote().sendString(errorMessage("Message must be a JSON object."));
                return;
            }

            ClientMessage clientMessage = gson.fromJson(message, ClientMessage.class);

            if (clientMessage == null || clientMessage.getCommandType() == null) {
                session.getRemote().sendString(errorMessage("Invalid message format."));
                return;
            }

            AuthData authData = authDAO.authenticate(clientMessage.getAuthToken());
            SESSION_AUTH_TOKENS.put(session, clientMessage.getAuthToken());

            String response = switch (clientMessage.getCommandType()) {
                case CONNECT -> handleConnect(session, authData, clientMessage);
                case MAKE_MOVE -> handleMakeMove(authData, clientMessage);
                case RESIGN -> handleResign(authData, clientMessage);
                case LEAVE -> handleLeave(session, authData, clientMessage);
                default -> errorMessage("Invalid command type.");
            };

            if (response != null) {
                session.getRemote().sendString(response);
            } else {
                System.out.println("No direct response needed, message already handled.");
            }

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
        SESSION_AUTH_TOKENS.remove(session);
        SESSION_GAME_ID.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error on session " + session + ": " + error);
        error.printStackTrace();
    }

    private String handleConnect(Session session, AuthData authData, ClientMessage msg) throws DataAccessException, IOException {
        GameData gameData = gameDAO.getGame(authData, msg.getGameID());
        String username = authData.username();

        boolean isWhitePlayer = username.equals(gameData.whiteUsername());
        boolean isBlackPlayer = username.equals(gameData.blackUsername());

        if (isWhitePlayer || isBlackPlayer) {
            SESSION_AUTH_TOKENS.put(session, authData.authToken());
            SESSION_GAME_ID.put(session, msg.getGameID());

            ServerMessage loadGameMessage = new ServerMessage(ServerMessageType.LOAD_GAME, null, gameData.game());
            session.getRemote().sendString(gson.toJson(loadGameMessage));
            String color = isWhitePlayer ? "white" : "black";
            broadcastToOtherPlayers(session, msg.getGameID(), new ServerMessage(
                    ServerMessageType.NOTIFICATION,
                    username + " has joined as " + color + ".",
                    null
            ));



            return null;
        }

        if (msg.getPlayerColor() == null) {
            SESSION_AUTH_TOKENS.put(session, authData.authToken());
            SESSION_GAME_ID.put(session, msg.getGameID());

            ServerMessage loadGameMessage = new ServerMessage(ServerMessageType.LOAD_GAME, null, gameData.game());
            session.getRemote().sendString(gson.toJson(loadGameMessage));

            broadcastToOtherPlayers(session, msg.getGameID(), new ServerMessage(
                    ServerMessageType.NOTIFICATION,
                    username + " is observing the game.",
                    null
            ));

            return null;
        }

        ChessGame.TeamColor requestedColor;
        try {
            requestedColor = ChessGame.TeamColor.valueOf(msg.getPlayerColor().toUpperCase());
        } catch (IllegalArgumentException e) {
            return errorMessage("Invalid player color requested.");
        }

        if (requestedColor == ChessGame.TeamColor.WHITE) {
            if (gameData.whiteUsername() != null) {
                return errorMessage("White side is already taken.");
            }
            gameData = new GameData(
                    gameData.gameID(),
                    username,
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            );
        } else if (requestedColor == ChessGame.TeamColor.BLACK) {
            if (gameData.blackUsername() != null) {
                return errorMessage("Black side is already taken.");
            }
            gameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    username,
                    gameData.gameName(),
                    gameData.game()
            );
        }

        gameDAO.updateGame(gameData);

        SESSION_AUTH_TOKENS.put(session, authData.authToken());
        SESSION_GAME_ID.put(session, msg.getGameID());

        ServerMessage loadGameMessage = new ServerMessage(ServerMessageType.LOAD_GAME, null, gameData.game());
        session.getRemote().sendString(gson.toJson(loadGameMessage));

        broadcastToOtherPlayers(session, msg.getGameID(), new ServerMessage(
                ServerMessageType.NOTIFICATION,
                username + " has joined the game as " + requestedColor.toString().toLowerCase() + ".",
                null
        ));

        return null;
    }

    private void broadcastToOtherPlayers(Session senderSession, int gameID, ServerMessage message) throws IOException {
        String messageJson = gson.toJson(message);

        for (Map.Entry<Session, Integer> entry : SESSION_GAME_ID.entrySet()) {
            Session session = entry.getKey();
            Integer sessionGameID = entry.getValue();

            if (session.isOpen() && !session.equals(senderSession) && Objects.equals(sessionGameID, gameID)) {
                session.getRemote().sendString(messageJson);
            }
        }
    }

    private String handleMakeMove(AuthData authData, ClientMessage msg) throws DataAccessException, InvalidMoveException, IOException {
        GameData gameData = gameDAO.getGame(authData, msg.getGameID());
        ChessGame game = gameData.game();
        ChessMove move = msg.getMove();

        if (move == null) {
            return errorMessage("Missing move data.");
        }

        boolean isWhitePlayer = authData.username().equals(gameData.whiteUsername());
        boolean isBlackPlayer = authData.username().equals(gameData.blackUsername());

        if (!isWhitePlayer && !isBlackPlayer) {
            return errorMessage("You are not a player in this game.");
        }

        if (game.getWinner() != null) {
            return errorMessage("Game is over. No moves allowed.");
        }

        ChessGame.TeamColor playerColor = isWhitePlayer ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        if (game.getTeamTurn() != playerColor) {
            return errorMessage("It is not your turn.");
        }

        game.makeMove(move);

        GameData updatedGameData = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        gameDAO.updateGame(updatedGameData);

        broadcastToAllPlayers(msg.getGameID(), new ServerMessage(
                ServerMessageType.LOAD_GAME,
                null,
                game
        ));

        String moveMessage = authData.username() + " made a move " + formatPosition(move.getStartPosition()) +
                " to " + formatPosition(move.getEndPosition()) + ".";

        broadcastToOtherPlayers(findSessionByAuthToken(authData.authToken()), msg.getGameID(), new ServerMessage(
                ServerMessageType.NOTIFICATION,
                moveMessage,
                null
        ));

        ChessGame.TeamColor opponentColor = authData.username().equals(gameData.whiteUsername())
                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (game.getWinner() != null) {
            broadcastToAllPlayers(msg.getGameID(), new ServerMessage(
                    ServerMessageType.NOTIFICATION,
                    authData.username() + " won the game by checkmate!",
                    null
            ));
        }
        else if (game.isInCheck(opponentColor)) {
            String playerInCheckUsername = opponentColor == ChessGame.TeamColor.WHITE
                    ? gameData.whiteUsername()
                    : gameData.blackUsername();

            String checkMessage = playerInCheckUsername + " is in check!";
            broadcastToAllPlayers(msg.getGameID(), new ServerMessage(
                    ServerMessageType.NOTIFICATION,
                    checkMessage,
                    null
            ));
        }


        return null;
    }


    private String formatPosition(ChessPosition pos) {
        char file = (char) ('a' + (pos.getColumn() - 1));
        int rank = pos.getRow();
        return "" + file + rank;
    }


    private Session findSessionByAuthToken(String authToken) {
        for (Map.Entry<Session, String> entry : SESSION_AUTH_TOKENS.entrySet()) {
            if (entry.getValue().equals(authToken)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void broadcastToAllPlayers(int gameID, ServerMessage message) throws IOException {
        String messageJson = gson.toJson(message);

        System.out.println("Broadcasting message to all players in game: " + gameID);

        for (Map.Entry<Session, Integer> entry : SESSION_GAME_ID.entrySet()) {
            Session session = entry.getKey();
            Integer sessionGameID = entry.getValue();

            if (session.isOpen() && Objects.equals(sessionGameID, gameID)) {
                System.out.println("Sending message to session: " + session);
                session.getRemote().sendString(messageJson);
            }
        }
    }

    private String handleResign(AuthData authData, ClientMessage msg) throws DataAccessException, IOException {
        GameData gameData = gameDAO.getGame(authData, msg.getGameID());
        ChessGame game = gameData.game();

        if (game.getWinner() != null) {
            return errorMessage("Game is already over. Cannot resign.");
        }

        ChessGame.TeamColor resigningPlayerColor = null;
        if (authData.username().equals(gameData.whiteUsername())) {
            resigningPlayerColor = ChessGame.TeamColor.WHITE;
        } else if (authData.username().equals(gameData.blackUsername())) {
            resigningPlayerColor = ChessGame.TeamColor.BLACK;
        } else {
            return errorMessage("You are not a player in this game.");
        }

        ChessGame.TeamColor winnerColor = (resigningPlayerColor == ChessGame.TeamColor.WHITE)
                ? ChessGame.TeamColor.BLACK
                : ChessGame.TeamColor.WHITE;

        game.setWinner(winnerColor);

        GameData updatedGameData = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        gameDAO.updateGame(updatedGameData);

        broadcastToAllPlayers(msg.getGameID(), new ServerMessage(
                ServerMessageType.NOTIFICATION,
                authData.username() + " resigned.",
                null
        ));

        return null;
    }

    private String handleLeave(Session session, AuthData authData, ClientMessage msg) throws IOException, DataAccessException {
        SESSION_AUTH_TOKENS.remove(session);
        SESSION_GAME_ID.remove(session);
        session.close();

        GameData gameData = gameDAO.getGame(authData, msg.getGameID());

        boolean wasWhite = authData.username().equals(gameData.whiteUsername());
        boolean wasBlack = authData.username().equals(gameData.blackUsername());

        if (wasWhite) {
            gameData = new GameData(
                    gameData.gameID(),
                    null,
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            );
        } else if (wasBlack) {
            gameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()
            );
        }

        gameDAO.updateGame(gameData);

        broadcastToOtherPlayers(session, msg.getGameID(), new ServerMessage(
                ServerMessageType.NOTIFICATION,
                authData.username() + " left the game.",
                null
        ));

        return null;
    }

    private String errorMessage(String message) {
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.ERROR, message, null);
        return gson.toJson(serverMessage);
    }
}
