package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class QueenMovesCalculator implements MovesCalculator {
    private static final int[][] QUEEN_DIRECTIONS = {
            {-1, 1}, {1, 1},
            {1, -1}, {-1, -1},
            {-1, 0}, {1, 0},
            {0, -1}, {0, 1}
    };

    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        return MovesUtil.getSlidingMoves(board, myPosition, QUEEN_DIRECTIONS);
    }
}
