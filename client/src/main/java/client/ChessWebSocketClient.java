package client;

import chess.ChessGame;
import chess.ChessMove;
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

public class ChessWebSocketClient extends Endpoint {

    private Session session;
    private final ConsolePrinter printer;
    private final CommandInterpreter interpreter;
    private final Gson gson;

    private final String authToken;
    private final int gameID;
    private final String username;
    private final ChessGame.TeamColor teamColor;

    private ChessGame game;

    public ChessWebSocketClient(String url, String authToken, int gameID, String username, ChessGame.TeamColor teamColor) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.username = username;
        this.teamColor = teamColor;

        this.gson = new Gson();
        this.printer = new ConsolePrinter();
        this.interpreter = null;

        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                ServerMessage notification = gson.fromJson(message, ServerMessage.class);
                switch (notification.getServerMessageType()) {
                    case NOTIFICATION -> printer.notify();
                    case LOAD_GAME -> this.game = notification.getGame();
                    default -> printer.printError(notification.getErrorMessage());
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            printer.printError(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        sendConnect();
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

    public void sendConnect() {
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.CONNECT, authToken, gameID);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending connect message: " + ex.getMessage());
        }
    }

    public void sendLeave() {
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.LEAVE, authToken, gameID);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending leave message: " + ex.getMessage());
        }
    }

    public void sendResign() {
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.RESIGN, authToken, gameID);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending resign message: " + ex.getMessage());
        }
    }

    public void sendMove(ChessPosition from, ChessPosition to) {
        try {
            var move = new ChessMove(from, to, null);
            var msg = new ClientMessage(ClientMessage.ClientMessageType.MAKE_MOVE, authToken, gameID);
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            printer.printError("Error sending move: " + ex.getMessage());
        }
    }

    public ChessGame getGame() {
        return game;
    }

    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }
}
