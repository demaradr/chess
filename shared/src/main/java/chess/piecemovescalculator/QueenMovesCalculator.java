package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class QueenMovesCalculator implements MovesCalculator {
    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {1, 0}, {0, 1},
                {-1, 0}, {0, -1},
                {-1, 1}, {1, 1},
                {1, -1}, {-1, -1}
        };

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
                }
                else if (occupyingPiece.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                    break;
                }
                else {
                    break;
                }
            }
        }
        return moves;
    }
}

