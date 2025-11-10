import chess.*;
import client.ChessClient;

public class Main {
    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);
        String serverURL = "http://localhost:8080";
        var client = new ChessClient(serverURL);
        client.run();
    }
}