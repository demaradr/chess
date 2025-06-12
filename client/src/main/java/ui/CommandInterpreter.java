package ui;

import chess.*;
import client.ResultException;
import client.ServerFacade;
import client.ChessWebSocketClient;
import model.*;
import request.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CommandInterpreter {
    private static final String LOGGED_OUT = "LoggedOut";
    private static final String WHITE_COLS = EscapeSequences.ROW_COL_FORMAT + "    a  b  c  d  e  f  g  h    " + EscapeSequences.RESET_BG_COLOR;
    private static final String BLACK_COLS = EscapeSequences.ROW_COL_FORMAT + "    h  g  f  e  d  c  b  a    " + EscapeSequences.RESET_BG_COLOR;

    private final ServerFacade facade;
    private boolean loggedIn;
    private String username;
    private String authToken;
    private ArrayList<Integer> gameIDList;
    private ChessWebSocketClient webSocketClient;
    private final Scanner scanner;

    public CommandInterpreter(ServerFacade facade) {
        this.facade = facade;
        this.gameIDList = new ArrayList<>();
        this.loggedIn = false;
        this.username = LOGGED_OUT;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to Chess client!");

        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_BOLD_AND_WHITE + EscapeSequences.SET_TEXT_BOLD +
                    "[" + username + "]: ");
            String input = scanner.nextLine();

            try {
                String output = loggedIn ? handleLoggedInCommand(input) : handleLoggedOutCommand(input);
                System.out.println(EscapeSequences.SET_TEXT_BOLD_AND_BLUE + output);

                if (!loggedIn && "quit".equals(input)) {
                    break;
                }
            } catch (ResultException ex) {
                printError(ex);
                // Continue loop so user can enter input again
            }
        }
    }

    private String handleLoggedOutCommand(String input) throws ResultException {
        String[] args = input.trim().split(" ");
        return switch (args[0]) {
            case "help" -> getLoggedOutHelp();
            case "register" -> {
                requireArgs(args, 4);
                var result = facade.register(new UserData(args[1], args[2], args[3]));
                loggedIn = true;
                username = result.username();
                authToken = result.authToken();
                yield "User " + username + " successfully registered and logged in.";
            }
            case "login" -> {
                requireArgs(args, 3);
                var result = facade.login(new UserData(args[1], args[2], null));
                loggedIn = true;
                username = result.username();
                authToken = result.authToken();
                yield "Logged in successfully. Welcome " + username + "!";
            }
            case "quit" -> "Quitting client. Goodbye.";
            default -> throw new ResultException(400, "Unknown command: " + input);
        };
    }

    private String handleLoggedInCommand(String input) throws ResultException {
        String[] args = input.trim().split(" ");
        return switch (args[0]) {
            case "help" -> getLoggedInHelp();
            case "list" -> listGames();
            case "create" -> {
                requireArgs(args, 2);
                facade.createGame(new CreateGameRequest(authToken, args[1]));
                yield "Created game: " + args[1];
            }
            case "join" -> joinGame(args);
            case "observe" -> observeGame(args);
            case "logout" -> {
                facade.logout(authToken);
                loggedIn = false;
                username = LOGGED_OUT;
                authToken = "";
                if (webSocketClient != null) {
                    webSocketClient.close();
                    webSocketClient = null;
                }
                yield "Logged out successfully.";
            }
            case "quit" -> throw new ResultException(400, "Error: Must log out before quitting.");
            case "redraw" -> {
                requireWebSocket();
                yield drawBoard(webSocketClient.getGame(), webSocketClient.getTeamColor());
            }
            case "leave" -> {
                requireWebSocket();
                webSocketClient.sendLeave();
                webSocketClient.close();
                webSocketClient = null;
                yield "You left the game.";
            }
            case "resign" -> {
                requireWebSocket();
                System.out.print("Are you sure you want to resign? (yes/no): ");
                String confirmation = scanner.nextLine().trim().toLowerCase();
                if ("yes".equals(confirmation)) {
                    webSocketClient.sendResign();
                    yield "You resigned from the game.";
                } else {
                    yield "Resign canceled. You remain in the game.";
                }
            }
            case "move" -> {
                requireWebSocket();
                requireArgs(args, 3);

                ChessPosition from = parsePosition(args[1]);
                String endArg = args[2];
                ChessPiece.PieceType promotion = null;

                if (endArg.length() == 3) {
                    promotion = switch (Character.toUpperCase(endArg.charAt(2))) {
                        case 'Q' -> ChessPiece.PieceType.QUEEN;
                        case 'R' -> ChessPiece.PieceType.ROOK;
                        case 'B' -> ChessPiece.PieceType.BISHOP;
                        case 'N' -> ChessPiece.PieceType.KNIGHT;
                        default -> throw new ResultException(400, "Invalid promotion piece: " + endArg.charAt(2));
                    };
                    endArg = endArg.substring(0, 2);
                }

                ChessPosition to = parsePosition(endArg);

                var pieceAtFrom = webSocketClient.getGame().getBoard().getPiece(from);
                if (pieceAtFrom == null) {
                    throw new ResultException(400, "Invalid move: No piece at " + args[1]);
                }
                if (pieceAtFrom.getTeamColor() != webSocketClient.getTeamColor()) {
                    throw new ResultException(400, "Invalid move: That is not your piece.");
                }

                if (promotion == null) {
                    webSocketClient.sendMove(from, to);
                } else {
                    webSocketClient.sendPromotionMove(from, to, promotion);
                }

                yield "Move sent: " + args[1] + " to " + args[2];
            }

            case "highlight" -> {
                requireWebSocket();
                requireArgs(args, 2);
                ChessPosition position = parsePosition(args[1]);
                Collection<ChessMove> validMoves = webSocketClient.getGame().validMoves(position);

                if (validMoves == null || validMoves.isEmpty()) {
                    throw new ResultException(400, "Invalid: There is no piece at this position.");
                }
                Set<ChessPosition> destinations = new HashSet<>();
                for (ChessMove move : validMoves) {
                    destinations.add(move.getEndPosition());
                }
                yield drawBoard(webSocketClient.getGame(), webSocketClient.getTeamColor(), destinations);
            }
            default -> throw new ResultException(400, "Unknown command: " + input);
        };
    }

    private String listGames() throws ResultException {
        var response = facade.listGames(new ListGamesRequest(authToken));
        gameIDList = new ArrayList<>();
        StringBuilder builder = new StringBuilder("Current Games:");

        int index = 1;
        for (var game : response.games()) {
            gameIDList.add(game.gameID());
            appendGameInfo(game, builder, index++);
        }
        return builder.toString();
    }

    private String joinGame(String[] args) throws ResultException {
        requireArgs(args, 3);
        int gameID = getGameIdFromList(args[1]);
        ChessGame.TeamColor color = parseColor(args[2]);
        facade.joinGame(new JoinGameRequest(authToken, color.name(), gameID));
        webSocketClient = new ChessWebSocketClient("ws://localhost:8080", authToken, gameID, username, color, this);

        try {
            ChessGame game = webSocketClient.getGameLoadedFuture().get();
            return drawBoard(game, color);
        } catch (Exception e) {
            throw new ResultException(500, "Failed to load game state: " + e.getMessage());
        }

    }

    private String observeGame(String[] args) throws ResultException {
        requireArgs(args, 2);
        int gameID = getGameIdFromList(args[1]);
        webSocketClient = new ChessWebSocketClient("ws://localhost:8080", authToken, gameID, username, null, this);

        try {
            ChessGame game = webSocketClient.getGameLoadedFuture().get();
            return drawBoard(game, ChessGame.TeamColor.WHITE);
        } catch (Exception e) {
            throw new ResultException(500, "Failed to load game state: " + e.getMessage());
        }

    }

    private static void appendGameInfo(GameData game, StringBuilder builder, int index) {
        builder.append("\n").append(EscapeSequences.SET_TEXT_BOLD_AND_BLUE)
                .append(index).append(" - ").append(game.gameName())
                .append(EscapeSequences.RESET_TEXT_BOLD_FAINT);

        builder.append(game.whiteUsername() == null ? "\n  White: (unclaimed)" : "\n  White: " + game.whiteUsername());
        builder.append(game.blackUsername() == null ? "\n  Black: (unclaimed)" : "\n  Black: " + game.blackUsername());
    }

    private int getGameIdFromList(String input) throws ResultException {
        try {
            int index = Integer.parseInt(input) - 1;
            if (index < 0 || index >= gameIDList.size()) {
                throw new ResultException(400, "Invalid game ID. Use 'list' to see games.");
            }
            return gameIDList.get(index);
        } catch (NumberFormatException e) {
            throw new ResultException(400, "Game ID must be a number.");
        }
    }

    private ChessGame.TeamColor parseColor(String colorStr) throws ResultException {
        return switch (colorStr.toUpperCase()) {
            case "WHITE" -> ChessGame.TeamColor.WHITE;
            case "BLACK" -> ChessGame.TeamColor.BLACK;
            default -> throw new ResultException(400, "Invalid color. Use WHITE or BLACK.");
        };
    }

    private ChessPosition parsePosition(String input) throws ResultException {
        if (input.length() != 2) {
            throw new ResultException(400, "Invalid position: " + input);
        }
        char colChar = input.charAt(0);
        char rowChar = input.charAt(1);
        int col = colChar - 'a' + 1;
        int row = rowChar - '1' + 1;
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new ResultException(400, "Invalid board position: " + input);
        }
        return new ChessPosition(row, col);
    }

    private void requireWebSocket() throws ResultException {
        if (webSocketClient == null) {
            throw new ResultException(400, "You're not in a game. Join or observe first.");
        }
    }

    public String drawBoard(ChessGame game, ChessGame.TeamColor playerColor) {
        return drawBoard(game, playerColor, new HashSet<>());
    }

    private String drawBoard(ChessGame game, ChessGame.TeamColor playerColor, Set<ChessPosition> highlights) {
        StringBuilder builder = new StringBuilder();
        ChessBoard board = game.getBoard();

        builder.append(playerColor == ChessGame.TeamColor.BLACK ? BLACK_COLS : WHITE_COLS).append("\n");
        int rowStart = playerColor == ChessGame.TeamColor.BLACK ? 1 : 8;
        int rowEnd = playerColor == ChessGame.TeamColor.BLACK ? 8 : 1;
        int rowStep = playerColor == ChessGame.TeamColor.BLACK ? 1 : -1;
        int jMult = playerColor == ChessGame.TeamColor.BLACK ? -1 : 1;
        int jOffset = playerColor == ChessGame.TeamColor.BLACK ? 9 : 0;

        for (int row = rowStart; row != rowEnd + rowStep; row += rowStep) {
            printRow(board, row, builder, jMult, jOffset, highlights);
        }

        builder.append(playerColor == ChessGame.TeamColor.BLACK ? BLACK_COLS : WHITE_COLS);
        return builder.toString();
    }

    private void printRow(ChessBoard board, int i, StringBuilder builder, int jMult, int jOffset, Set<ChessPosition> highlights) {
        builder.append(EscapeSequences.ROW_COL_FORMAT);
        builder.append(" ");
        builder.append(i);
        builder.append(" ");
        builder.append(EscapeSequences.RESET_TEXT_BOLD_FAINT);

        for (int j = 1; j <= 8; j++) {
            int col = jOffset + (jMult * j);
            ChessPosition position = new ChessPosition(i, col);

            boolean isBrownSquare = (i + col) % 2 == 0;

            if (highlights.contains(position)) {
                if (isBrownSquare) {
                    builder.append(EscapeSequences.SET_BG_COLOR_GREEN_ON_BROWN);
                } else {
                    builder.append(EscapeSequences.SET_BG_COLOR_GREEN_ON_IVORY);
                }
            } else {
                builder.append(isBrownSquare ? EscapeSequences.SET_BG_COLOR_BROWN : EscapeSequences.SET_BG_COLOR_IVORY);
            }


            var piece = board.getPiece(position);
            builder.append(formatPiece(piece));
        }

        builder.append(EscapeSequences.ROW_COL_FORMAT);
        builder.append(" ");
        builder.append(i);
        builder.append(" ");
        builder.append(EscapeSequences.RESET_BG_COLOR);
        builder.append("\n");
    }

    private String formatPiece(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        String colorCode = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ? EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_TEXT_BOLD
                : EscapeSequences.SET_TEXT_COLOR_BLACK;

        return colorCode + switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        } + EscapeSequences.RESET_TEXT_BOLD_FAINT;
    }

    private void printError(ResultException ex) {
        String message = switch (ex.statusCode()) {
            case 400 -> ex.getMessage();
            case 401 -> "Error: Unauthorized.";
            case 403 -> "Error: Already taken.";
            default -> "Internal server error.";
        };
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + message);
    }

    private void requireArgs(String[] args, int expected) throws ResultException {
        if (args.length != expected) {
            throw new ResultException(400, "Expected " + (expected - 1) + " arguments.");
        }
    }

    private String getLoggedOutHelp() {
        return """
                Logged out commands:
                \t\033[34;1mhelp\033[0m - Show this help.
                \t\033[34;1mlogin <username> <password>\033[0m - Log in.
                \t\033[34;1mregister <username> <password> <email>\033[0m - Register a new user.
                \t\033[34;1mquit\033[0m - Exit the client.
                """;
    }

    private String getLoggedInHelp() {
        return """
                Logged in commands:
                \t\033[34;1mhelp\033[0m - Show this help.
                \t\033[34;1mlist\033[0m - List available games.
                \t\033[34;1mcreate <gameName>\033[0m - Create a new game.
                \t\033[34;1mjoin <gameID> <WHITE|BLACK>\033[0m - Join a game.
                \t\033[34;1mobserve <gameID>\033[0m - Observe a game.
                \t\033[34;1mmove <startPos> <endPos>[Q|R|B|N]\033[0m - Make a move, with optional promotion (e.g., e7 e8Q).
                \t\033[34;1mhighlight <position>\033[0m - Highlight valid moves from the position.
                \t\033[34;1mredraw\033[0m - Reprint the board.
                \t\033[34;1mleave\033[0m - Leave the game.
                \t\033[34;1mresign\033[0m - Resign from the game.
                \t\033[34;1mlogout\033[0m - Log out.
                """;
    }
}
