package websocket;

import chess.ChessGame;
import io.javalin.websocket.WsContext;
import org.eclipse.jetty.websocket.api.Session;

import javax.management.Notification;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();


    public void add(Connection connection) {
        connections.put(connection.sessionID(), connection);
    }

    public void remove(String sessionID) {
        connections.remove(sessionID);
    }

    public void broadcast(Session excludeSession, Notification notification ) throws IOException {
        String message = notification.toString();
        for (Session c : connections.values()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(message);
                }
            }
        }
    }

    public record Connection(String sessionID, WsContext context, int gameID, String user, ChessGame.TeamColor color) {
    }
}