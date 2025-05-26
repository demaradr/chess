package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class KnightMovesCalculator implements MovesCalculator {
    private static final int[][] KNIGHT_MOVES = {
            {-2, 1}, {-2, -1},
            {2, 1}, {2, -1},
            {1, 2}, {-1, 2},
            {-1, -2}, {1, -2}
    };

    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        return MovesUtil.getJumpMoves(board, myPosition, KNIGHT_MOVES);
    }
}
