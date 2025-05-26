import chess.*;
import server.Server;
import dataaccess.DatabaseInitializer;
import dataaccess.DataAccessException;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        try {
            DatabaseInitializer.initializeDatabase();
            System.out.println("Database initialized successfully.");
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Server server = new Server();
        int port = server.run(8080);
        System.out.println("Server started on port " + port);
    }
}
