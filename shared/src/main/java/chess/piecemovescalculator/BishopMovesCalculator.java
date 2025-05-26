package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class BishopMovesCalculator implements MovesCalculator {
    private static final int[][] BISHOP_DIRECTIONS = {
            {-1, 1}, {1, 1},
            {1, -1}, {-1, -1}
    };

    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        return MovesUtil.getSlidingMoves(board, myPosition, BISHOP_DIRECTIONS);
    }
}
