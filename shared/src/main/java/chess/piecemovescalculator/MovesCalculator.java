package chess.piecemovescalculator;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;

public interface MovesCalculator {
    HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position);
}
