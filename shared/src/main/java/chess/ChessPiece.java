package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (this.type) {
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case KING -> kingMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        int [][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        return slideMoves(board, position, moves, directions);
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int [][] directions = {
                {1,0}, {-1, 0}, {0, 1}, {0, -1}
        };

        return slideMoves(board, position, moves, directions);
        }


    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] directions = {
                {1, 1}, {1,-1}, {-1,1}, {-1, -1}, {0,1}, {0,-1}, {1, 0}, {-1, 0}
    };

        return slideMoves(board, position, moves, directions);
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int [][] directions = {
                {1,2}, {1,-2}, {-1, 2}, {-1,-2},
                {2,1}, {2,-1}, {-2,1}, {-2,-1}
        };

        return jumpMoves(board, position, moves, directions);


    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int [][] directions = {
                {1,0}, {-1,0}, {0,1}, {0,-1},
                {1,1}, {1,-1}, {-1,1}, {-1,-1}
        };

        return jumpMoves(board, position, moves, directions);


    }

    private Collection<ChessMove> jumpMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves, int[][] directions) {
        for (int[] dir: directions) {
            int row = position.getRow() + dir[0];
            int col = position.getColumn() + dir[1];

            if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(newPosition);

                if (currentPiece == null) {
                    moves.add(new ChessMove(position, newPosition, null));

                }
                else if (currentPiece.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> slideMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves, int[][] directions) {
        for (int[] dir : directions) {
            int row = position.getRow() + dir[0];
            int col = position.getColumn() + dir[1];

            while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(newPosition);
                if (currentPiece == null) {
                    moves.add(new ChessMove(position, newPosition, null));
                }

                else {
                    if (currentPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                    break;
                }
                row += dir[0];
                col += dir[1];
            }

        }
        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int directions;
        int startPosition;
        int promotion;
        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            directions = 1;
            startPosition = 2;
            promotion = 8;
        }
        else {
            directions = -1;
            startPosition = 7;
            promotion = 1;
        }

        int row = position.getRow();
        int col = position.getColumn();

        int moveForward = row + directions;
        if(isInBoard(moveForward, col) && board.getPiece(new ChessPosition(moveForward, col)) == null) {
            ChessPosition newPosition = new ChessPosition(moveForward, col);
            moveOrPromotion(position, moves, promotion, newPosition);

            if (row == startPosition) {
                int doubleMove = row + 2 * directions;
                if (isInBoard(doubleMove, col) && board.getPiece(new ChessPosition(doubleMove,col)) == null) {
                    moves.add(new ChessMove(position, new ChessPosition(doubleMove, col), null));
                }
            }
            }

        int [][] capture = {
                {directions, 1}, {directions, -1}
        };
        for (int[] capt : capture) {
            int captureRow = row + capt[0];
            int captureCol = col + capt[1];
            if (isInBoard(captureRow, captureCol)) {
                ChessPosition newPosition2 = new ChessPosition(captureRow, captureCol);
                ChessPiece currentPiece = board.getPiece(newPosition2);

                if (currentPiece != null && currentPiece.getTeamColor() != this.getTeamColor()) {
                    moveOrPromotion(position, moves, promotion, newPosition2);
                }
            }
        }
        return moves;
        }

    private void moveOrPromotion(ChessPosition position, List<ChessMove> moves, int promotion, ChessPosition newPosition) {
        if (newPosition.getRow() == promotion) {
            moves.add(new ChessMove(position, newPosition, PieceType.BISHOP));
            moves.add(new ChessMove(position, newPosition, PieceType.ROOK));
            moves.add(new ChessMove(position, newPosition, PieceType.KNIGHT));
            moves.add(new ChessMove(position, newPosition, PieceType.QUEEN));
        }
        else {
            moves.add(new ChessMove(position, newPosition, null));
        }
    }


    private boolean isInBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
    }


