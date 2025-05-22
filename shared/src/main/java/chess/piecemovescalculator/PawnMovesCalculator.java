package chess.piecemovescalculator;

import chess.*;
import java.util.HashSet;

public class PawnMovesCalculator implements MovesCalculator {
    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        int direction = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int newRow = row + direction;
        if (newRow >= 1 && newRow <= 8) {
            ChessPosition forwardPos = new ChessPosition(newRow, col);
            if (board.getPiece(forwardPos) == null) {
                addMove(moves, myPosition, forwardPos);

                boolean isAtStartingRow = (myPiece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2) ||
                        (myPiece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7);
                int twoForwardRow = row + 2 * direction;
                ChessPosition twoForwardPos = new ChessPosition(twoForwardRow, col);

                if (isAtStartingRow && board.getPiece(twoForwardPos) == null) {
                    moves.add(new ChessMove(myPosition, twoForwardPos, null));
                }
            }
        }

        int[][] captureOffsets = {{direction, -1}, {direction, 1}};
        for (int[] offset : captureOffsets) {
            int captureRow = row + offset[0];
            int captureCol = col + offset[1];

            if (captureRow >= 1 && captureRow <= 8 && captureCol >= 1 && captureCol <= 8) {
                ChessPosition capturePos = new ChessPosition(captureRow, captureCol);
                ChessPiece targetPiece = board.getPiece(capturePos);

                if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) {
                    addMove(moves, myPosition, capturePos);
                }
            }
        }

        return moves;
    }

    private void addMove(HashSet<ChessMove> moves, ChessPosition start, ChessPosition end) {
        int promotionRow = end.getRow();
        if (promotionRow == 1 || promotionRow == 8) {
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
