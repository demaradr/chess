package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class RookMovesCalculator implements MovesCalculator {
    private static final int[][] ROOK_DIRECTIONS = {
            {-1, 0}, {1, 0},
            {0, -1}, {0, 1}
    };

    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        return MovesUtil.getSlidingMoves(board, myPosition, ROOK_DIRECTIONS);
    }
}
