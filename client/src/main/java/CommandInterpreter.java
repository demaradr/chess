import chess.*;
import client.ResultException;
import client.ServerFacade;
import model.*;
import request.*;
import ui.EscapeSequences;
import java.util.ArrayList;
import java.util.Scanner;

public class CommandInterpreter {
    private static final String LOGGED_OUT = "LoggedOut";

    private final ServerFacade facade;
    private boolean loggedIn;
    private String username;


    public CommandInterpreter(ServerFacade facade) {
        this.facade = facade;
        this.loggedIn = false;
        this.username = LOGGED_OUT;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
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
            }
        }
    }

    private String handleLoggedOutCommand(String input) throws ResultException {
        String[] args = input.trim().split(" ");
        return switch (args[0]) {
            case "help" -> getLoggedOutHelp();
            case "register" -> {
                requireArgs(args, 4);
                facade.register(new UserData(args[1], args[2], args[3]));
                yield "User " + args[1] + " successfully registered.";
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
                yield "Logged out successfully.";
            }
            case "quit" -> throw new ResultException(400, "Error: Must log out before quitting.");
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
        return drawBoard(new ChessGame(), color);
    }

    private String observeGame(String[] args) throws ResultException {
        requireArgs(args, 2);
        int gameID = getGameIdFromList(args[1]);
        return drawBoard(new ChessGame(), ChessGame.TeamColor.WHITE);
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

    private String drawBoard(ChessGame game, ChessGame.TeamColor playerColor) {
        StringBuilder builder = new StringBuilder();
        ChessBoard board = game.getBoard();

        builder.append(playerColor == ChessGame.TeamColor.BLACK ? BLACK_COLS : WHITE_COLS).append("\n");
        int step = (playerColor == ChessGame.TeamColor.BLACK) ? 1 : -1;
        int startRow = (playerColor == ChessGame.TeamColor.BLACK) ? 1 : 8;
        int endRow = (playerColor == ChessGame.TeamColor.BLACK) ? 8 : 1;

        for (int i = startRow; i != endRow + step; i += step) {
            printRow(board, i, builder);
        }

        builder.append(playerColor == ChessGame.TeamColor.BLACK ? BLACK_COLS : WHITE_COLS);
        return builder.toString();
    }
}