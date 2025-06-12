package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import ui.CommandInterpreter;
import ui.ConsolePrinter;
import websocket.messages.ClientMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public class ChessWebSocketClient extends Endpoint {

    private Session session;
    private final ConsolePrinter printer;
    private final CommandInterpreter interpreter;
    private final Gson gson;
    private final CompletableFuture<ChessGame> gameLoaded = new CompletableFuture<>();
    private final String authToken;
    private final int gameID;
    private final String username;
    private final ChessGame.TeamColor teamColor;

    private ChessGame game;

    private final Object sessionLock = new Object();

    public ChessWebSocketClient(String url, String authToken, int gameID, String username, ChessGame.TeamColor teamColor, CommandInterpreter interpreter) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.username = username;
        this.teamColor = teamColor;
        this.interpreter = interpreter;

        this.gson = new Gson();
        this.printer = new ConsolePrinter();

        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            container.connectToServer(this, socketURI);

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            printer.printError(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                try {
                    String jsonString;

                    if (message instanceof String s) {
                        jsonString = s;
                    } else {
                        printer.printError("Received unexpected message type: " + message.getClass().getName() +
                                " with value: " + message.toString());
                        new Exception("Debug: unexpected message type received").printStackTrace();
                        System.err.println("Session: " + session);
                        return;
                    }

                    ServerMessage notification = gson.fromJson(jsonString, ServerMessage.class);

                    switch (notification.getServerMessageType()) {
                        case NOTIFICATION -> printer.printNotification(notification.getMessage());

                        case LOAD_GAME -> {
                            game = notification.getGame();

                            if (!gameLoaded.isDone()) {
                                gameLoaded.complete(game);
                            } else {
                                System.out.println("\n" + interpreter.drawBoard(game, teamColor));
                            }
                        }

                        case ERROR -> printer.printError(notification.getErrorMessage());

                        default -> printer.printError("Unknown server message type.");
                    }
                } catch (Exception e) {
                    printer.printError("JSON parse error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        sendConnect();
    }

    private boolean isConnected() {
        return session != null && session.isOpen();
    }

    public CompletableFuture<ChessGame> getGameLoadedFuture() {
        return gameLoaded;
    }

    public void sendConnect() {
        if (!isConnected()) {
            printer.printError("WebSocket not connected yet. Can't send connect.");
            return;
        }
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.CONNECT, authToken, gameID, null);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending connect message: " + ex.getMessage());
        }
    }

    public void sendLeave() {
        if (!isConnected()) {
            return;
        }
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.LEAVE, authToken, gameID, null);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending leave message: " + ex.getMessage());
        }
    }

    public void sendResign() {
        if (!isConnected()) {
            return;
        }
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.RESIGN, authToken, gameID, null);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending resign message: " + ex.getMessage());
        }
    }

    public void sendMove(ChessPosition from, ChessPosition to) {
        if (!isConnected()) {
            return;
        }
        try {
            var move = new ChessMove(from, to, null);
            var msg = new ClientMessage(ClientMessage.ClientMessageType.MAKE_MOVE, authToken, gameID, move);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending move: " + ex.getMessage());
        }
    }

    public void sendPromotionMove(ChessPosition from, ChessPosition to, ChessPiece.PieceType promotionPiece) {
        if (!isConnected()) {
            return;
        }
        try {
            var move = new ChessMove(from, to, promotionPiece);
            var msg = new ClientMessage(ClientMessage.ClientMessageType.MAKE_MOVE, authToken, gameID, move);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending promotion move: " + ex.getMessage());
        }
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (IOException ex) {
            printer.printError("Error closing WebSocket: " + ex.getMessage());
        }
    }

    public ChessGame getGame() {
        return game;
    }

    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    @Override
    public void onError(Session session, Throwable thr) {
        printer.printError("WebSocket error: " + thr.getMessage());
        thr.printStackTrace();
    }
}
