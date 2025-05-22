package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class KnightMovesCalculator implements MovesCalculator {
    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-2, 1}, {-2, -1},
                {2, 1}, {2, -1},
                {1, 2}, {-1, 2},
                {-1, -2}, {1, -2}
        };

        for (int[] dir : directions) {
            int row = myPosition.getRow() + dir[0];
            int col = myPosition.getColumn() + dir[1];

            if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece occupyingPiece = board.getPiece(newPos);

                if (occupyingPiece == null) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                } else if (occupyingPiece.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
            }
        }

        return moves;
    }
}
