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

    public void remove(String session) {
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
}