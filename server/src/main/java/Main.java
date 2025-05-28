import chess.*;
import dataaccess.DatabaseManager;
import server.Server;
import dataaccess.DatabaseInitializer;
import dataaccess.DataAccessException;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        try {
            // Properties are loaded automatically via static block in DatabaseManager
            DatabaseManager.createDatabase();
            System.out.println("Database created successfully.");

            DatabaseInitializer.initializeDatabase();
            System.out.println("Database initialized successfully.");

        } catch (Exception e) {
            System.err.println("Failed to set up the database: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Server server = new Server();
        int port = server.run(8080);
        System.out.println("Server started on port " + port);
    }
}
