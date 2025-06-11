package websocket.messages;

import chess.ChessMove;

import java.util.Objects;

public class ClientMessage {
    ClientMessageType commandType;

    private String authToken;
    private int gameID;
    private ChessMove move;
    private String playerColor;

    public String getPlayerColor() {
        return this.playerColor;
    }

    public enum ClientMessageType {
        CONNECT,
        MAKE_MOVE,
        RESIGN,
        LEAVE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientMessage that = (ClientMessage) o;
        return commandType == that.commandType && Objects.equals(authToken, that.authToken) && Objects.equals(gameID, that.gameID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType, authToken, gameID);
    }

    public ClientMessage(ClientMessageType type, String authToken, int gameID) {
        this.commandType = type;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = null;
    }

    public ClientMessage(ClientMessageType type, String authToken, int gameID, ChessMove move) {
        this.commandType = type;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public ClientMessageType getCommandType() {
        return this.commandType;
    }

    public String getAuthToken() {
        return this.authToken;
    }

    public int getGameID() {
        return this.gameID;
    }

    public ChessMove getMove() {
        return this.move;
    }
}
