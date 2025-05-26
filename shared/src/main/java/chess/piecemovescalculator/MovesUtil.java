package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class MovesUtil {

    public static HashSet<ChessMove> getSlidingMoves(ChessBoard board, ChessPosition myPosition, int[][] directions) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessGame.TeamColor myColor = board.getPiece(myPosition).getTeamColor();

        for (int[] dir : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];

                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }

                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece occupyingPiece = board.getPiece(newPos);

                if (occupyingPiece == null) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                } else {
                    if (occupyingPiece.getTeamColor() != myColor) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                }
            }
        }

        return moves;
    }

    public static HashSet<ChessMove> getJumpMoves(ChessBoard board, ChessPosition myPosition, int[][] deltas) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessGame.TeamColor myColor = board.getPiece(myPosition).getTeamColor();

        for (int[] delta : deltas) {
            int row = myPosition.getRow() + delta[0];
            int col = myPosition.getColumn() + delta[1];

            if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece occupyingPiece = board.getPiece(newPos);

                if (occupyingPiece == null || occupyingPiece.getTeamColor() != myColor) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
            }
        }

        return moves;
    }

}
