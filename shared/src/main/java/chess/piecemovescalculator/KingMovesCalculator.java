package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class KingMovesCalculator implements MovesCalculator {
    private static final int[][] KING_MOVES = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        return MovesUtil.getJumpMoves(board, myPosition, KING_MOVES);
    }
}
