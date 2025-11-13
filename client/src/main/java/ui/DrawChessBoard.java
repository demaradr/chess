package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


import static ui.EscapeSequences.*;

public class DrawChessBoard {

    public static void drawBoard(ChessGame.TeamColor color) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        ChessBoard board = new ChessBoard();
        board.resetBoard();
        if (color == ChessGame.TeamColor.WHITE) {
            drawBoardWhite(out, board);
        }
        else {
            drawBoardBlack(out,board);
        }
    }

    private static void drawBoardWhite(PrintStream out, ChessBoard board) {

        headersWhite(out);

        for (int row = 1; row < 9; row++) {
            int rowNum = 9- row;
            setBorder(out);
            out.print(" " + rowNum + " ");
            for (int col = 0; col < 8; col++) {
                int colNum = col + 1;
                boolean lightSquare = ((rowNum + colNum) % 2 != 0);

                if (lightSquare) {
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
            setBorder(out);
            out.print(" " + row + " ");
            reset(out);
            out.println();


        }

        headersWhite(out);


    }


    private static void drawBoardBlack(PrintStream out, ChessBoard board) {

        headersBlack(out);

        for (int row = 0; row < 8; row++) {
            int rowNum = row + 1;
            setBorder(out);
            out.print(" " + rowNum + " ");
            for (int col = 0; col < 8; col++) {
                int colNum = 8 - col;
                boolean lightSquare = ((rowNum + colNum) % 2 != 0);

                if (lightSquare) {
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




