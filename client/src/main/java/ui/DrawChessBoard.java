package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


import static ui.EscapeSequences.*;

public class DrawChessBoard {

    public static void drawBoard(ChessGame game, ChessGame.TeamColor color) {
        drawBoard(game, color, null);
    }

    public static void drawBoard(ChessGame game, ChessGame.TeamColor color, Collection<ChessPosition> highlightPos) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println();
        out.print(ERASE_SCREEN);

        ChessBoard board = game.getBoard();
        Set<ChessPosition> highlight = highlightPos != null ? new HashSet<>(highlightPos) : new HashSet<>();

        if (color == ChessGame.TeamColor.WHITE) {
            drawBoardWhite(out, board, highlight);
        }
        else {
            drawBoardBlack(out,board, highlight);
        }
        out.println();
    }


    private static void drawBoardWhite(PrintStream out, ChessBoard board) {
        drawBoardWhite(out, board, new HashSet<>());
    }



    private static void drawBoardWhite(PrintStream out, ChessBoard board, Set<ChessPosition> highlightPos) {

        headersWhite(out);

        for (int row = 1; row < 9; row++) {
            int rowNum = 9- row;
            setBorder(out);
            out.print(" " + rowNum + " ");
            for (int col = 0; col < 8; col++) {
                int colNum = col + 1;
                ChessPosition pos = new ChessPosition(rowNum, colNum);

                boolean highlighted = highlightPos.contains(pos);
                drawHelper(out, board, rowNum, colNum, highlighted);


            }
            setBorder(out);
            out.print(" " + row + " ");
            reset(out);
            out.println();


        }

        headersWhite(out);


    }

    private static void drawHelper(PrintStream out, ChessBoard board, int rowNum, int colNum, boolean highlighted) {
        boolean lightSquare = ((rowNum + colNum) % 2 != 0);

        if (highlighted) {
            out.print(SET_BG_COLOR_DARK_GREEN);
        }
        else if (lightSquare) {
            setLightSquare(out);
        }
        else {
            setDarkSquare(out);
        }

        ChessPosition pos = new ChessPosition(rowNum, colNum);
        ChessPiece piece = board.getPiece(pos);


        if (piece == null) {
            out.print(EMPTY);

        }

        else {
            printPiece(out, piece);
        }
    }



    private static void drawBoardBlack(PrintStream out, ChessBoard board) {
        drawBoardBlack(out, board, new HashSet<>());
    }

    private static void drawBoardBlack(PrintStream out, ChessBoard board, Set<ChessPosition> highlightPos) {

        headersBlack(out);

        for (int row = 0; row < 8; row++) {
            int rowNum = row + 1;
            setBorder(out);
            out.print(" " + rowNum + " ");
            for (int col = 0; col < 8; col++) {
                int colNum = 8 - col;
                ChessPosition pos = new ChessPosition(rowNum, colNum);
                boolean highlighted = highlightPos.contains(pos);



                drawHelper(out, board, rowNum, colNum, highlighted);

                setBorder(out);

            }

            setBorder(out);
            out.print(" " + rowNum + " ");
            reset(out);
            out.println();


        }

        headersBlack(out);


    }

    private static void printPiece(PrintStream out, ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            out.print(SET_TEXT_COLOR_WHITE);
        }
        else {
            out.print(SET_TEXT_COLOR_BLACK);
        }



        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                out.print(WHITE_KING);
            }
            else {
                out.print(BLACK_KING);
            }
        }


        else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
           if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
               out.print(WHITE_QUEEN);

           }
           else {
               out.print(BLACK_QUEEN);
           }
        }

        else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                out.print(WHITE_BISHOP);

            }
            else {
                out.print(BLACK_BISHOP);
            }
        }

        else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                out.print(WHITE_KNIGHT);

            }
            else {
                out.print(BLACK_KNIGHT);
            }
        }

        else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                out.print(WHITE_ROOK);

            }
            else {
                out.print(BLACK_ROOK);
            }
        }

        else if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                out.print(WHITE_PAWN);

            }
            else {
                out.print(BLACK_PAWN);
            }
        }

    }


    private static void setDarkSquare(PrintStream out) {
        out.print(SET_BG_COLOR_BROWN);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setLightSquare(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_BROWN);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setBorder(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_BLUE);
        out.print(SET_TEXT_COLOR_WHITE);
    }



    private static void headersWhite(PrintStream out) {
        setBorder(out);
        out.print("   ");
        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        for (String header : headers) {
            out.print(" " + header + " ");
        }
        out.print("   ");
        reset(out);
        out.println();
    }

    private static void reset(PrintStream out) {
        out.print(RESET_TEXT_COLOR);
        out.print(RESET_BG_COLOR);
    }


    private static void headersBlack(PrintStream out) {

        setBorder(out);
        out.print("   ");
        String[] headers = {"h", "g", "f", "e", "d", "c", "b", "a"};
        for (String header : headers) {
            out.print(" " + header + " ");
        }
        out.print("   ");
        reset(out);
        out.println();

        }

    }




