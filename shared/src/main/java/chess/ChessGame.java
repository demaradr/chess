package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
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
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return List.of();
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        List<ChessMove> validMove = new ArrayList<>();

        for (ChessMove move : moves) {
            ChessBoard copy = new ChessBoard();
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition pos = new ChessPosition(i, j);
                    ChessPiece p = board.getPiece(pos);
                    if (p != null) {
                        copy.addPiece(pos, new ChessPiece(p.getTeamColor(), p.getPieceType()));
                    }
                }
            }
            copy.addPiece(move.getStartPosition(), null);

            ChessPiece.PieceType promotion = move.getPromotionPiece();
            ChessPiece toPlace;
            if (promotion != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                toPlace = new ChessPiece(piece.getTeamColor(), promotion);
            }
            else {
                toPlace = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
            }
            copy.addPiece(move.getEndPosition(), toPlace);

            if (!isInCheck(copy, piece.getTeamColor())) {
                validMove.add(move);
            }
        }
        return validMove;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException();
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> legal = validMoves(move.getStartPosition());
        if (!legal.contains(move)) {
            throw new InvalidMoveException();
        }

        board.addPiece(move.getStartPosition(), null);

        ChessPiece.PieceType promotion = move.getPromotionPiece();
        ChessPiece newPiece;
        if (promotion != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            newPiece = new ChessPiece(piece.getTeamColor(), promotion);
        } else {
            newPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
        }
        board.addPiece(move.getEndPosition(), newPiece);

        this.teamTurn = (this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(this.board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {

                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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

    private boolean attackedBy(ChessBoard board, ChessPosition target, TeamColor attacker) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {

                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == attacker) {

                    for (ChessMove m : piece.pieceMoves(board, pos)) {
                        if (m.getEndPosition().equals(target)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPos = null;

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece p = board.getPiece(pos);
                if (p != null && p.getTeamColor() == teamColor && p.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = pos;
                    break;
                }
            }
            if (kingPos != null) break;
        }
        if (kingPos == null) {
            return false;
        }
        TeamColor opponent = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        return attackedBy(board, kingPos, opponent);}
}