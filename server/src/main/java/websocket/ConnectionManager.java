package websocket;

import chess.ChessGame;
import io.javalin.websocket.WsContext;
import org.eclipse.jetty.websocket.api.Session;

import javax.management.Notification;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();


    public void add(Connection connection) {
        connections.put(String.valueOf(connection.session()), connection);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    Collection<Connection> allConnections() {
        return connections.values();
    }


    public record Connection(Session session,
                             int gameID,
                             String user,
                             ChessGame.TeamColor color,
                             boolean observer) {
    }

    public void broadcast(int gameID, Session exludeSession, String message) throws IOException {
        for (Connection c : connections.values()) {
            if (c.gameID() == gameID && c.session().isOpen()) {
                if (c.session().equals(exludeSession)) {
                    c.session().getRemote().sendString(message);
                }
            }
        }
    }
}