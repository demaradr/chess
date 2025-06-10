package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard board;
    private TeamColor winner = null;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    public TeamColor getWinner() {
        return winner;
    }

    public void setWinner(TeamColor winner) {
        this.winner = winner;
    }

    public void resign(TeamColor team) {
        if (winner != null) {
            return;
        }
        winner = (team == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentTurn=" + currentTurn +
                ", board=" + board +
                ", winner=" + winner +
                '}';
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : moves) {
            ChessBoard newBoard = board.copyBoard();

            newBoard.addPiece(move.getEndPosition(), piece);
            newBoard.addPiece(startPosition, null);

            if (!checkAfterMove(piece.getTeamColor(), newBoard)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private boolean checkAfterMove(TeamColor teamColor, ChessBoard board) {
        ChessBoard original = this.board;
        this.board = board;
        boolean inCheck = isInCheck(teamColor);
        this.board = original;
        return inCheck;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public boolean makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("No piece at " + start);
        }

        if (piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("It's not " + currentTurn + "'s turn");
        }

        Collection<ChessMove> validMoves = validMoves(start);
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        board.addPiece(end, piece);
        board.addPiece(start, null);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            ChessPiece.PieceType promotion = move.getPromotionPiece();
            if (promotion != null) {
                if (promotion == ChessPiece.PieceType.QUEEN ||
                        promotion == ChessPiece.PieceType.ROOK ||
                        promotion == ChessPiece.PieceType.BISHOP ||
                        promotion == ChessPiece.PieceType.KNIGHT) {
                    ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), promotion);
                    board.addPiece(end, promotedPiece);
                } else {
                    throw new InvalidMoveException("Invalid promotion piece: " + promotion);
                }
            }
        }

        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        return true;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard board = getBoard();
        ChessPosition kingPos = findKingPosition(teamColor, board);

        return isThreatenedByOpponent(teamColor, kingPos, board);
    }

    private ChessPosition findKingPosition(TeamColor teamColor, ChessBoard board) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null &&
                        piece.getPieceType() == ChessPiece.PieceType.KING &&
                        piece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean isThreatenedByOpponent(TeamColor teamColor, ChessPosition kingPos, ChessBoard board) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }

                Collection<ChessMove> moves = piece.pieceMoves(board, pos);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    private boolean scanBoard(TeamColor teamColor, BoardAction action) {
        ChessBoard board = getBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (action.apply(pos, piece, teamColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface BoardAction {
        boolean apply(ChessPosition pos, ChessPiece piece, TeamColor teamColor);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return !scanBoard(teamColor, (pos, piece, team) -> {
            if (piece != null && piece.getTeamColor() == team) {
                Collection<ChessMove> moves = validMoves(pos);
                return moves != null && !moves.isEmpty();
            }
            return false;
        });
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return !scanBoard(teamColor, (pos, piece, team) -> {
            if (piece != null && piece.getTeamColor() == team) {
                Collection<ChessMove> moves = validMoves(pos);
                return moves != null && !moves.isEmpty();
            }
            return false;
        });
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
