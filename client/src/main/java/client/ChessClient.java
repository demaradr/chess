package client;

import chess.ChessGame;
import exception.ResponseException;
import models.GameData;
import results.CreateGameResult;
import results.ListGamesResult;
import results.LoginResult;
import results.RegisterResult;
import server.ServerFacade;
import ui.DrawChessBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.lang.Integer.parseInt;
import static ui.EscapeSequences.*;

public class ChessClient {

    private State state = State.LOGGED_OUT;
    private final ServerFacade server;
    private String authToken;
    private String username;
    private List<GameData> createdGames = new ArrayList<>();


    public ChessClient(String serverURL) {
        this.server = new ServerFacade(serverURL);
    }

    public void run() {
        System.out.println("\nHello there! Welcome to 240 Chess ♕ \nType help to start :)\n");

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();



            try {
                String line = scanner.nextLine();
                result = eval(line);
                System.out.print(result);
            }
            catch (Throwable error) {
                var message = error.getMessage();
                System.out.print(message);
            }
        }
    }


    enum State {
        LOGGED_OUT,
        LOGGED_IN
    }

    public String help() {
        if (state == State.LOGGED_OUT) {
            return "to register type:"+ SET_TEXT_COLOR_BLUE + " register <USERNAME> <PASSWORD> <EMAIL>" + RESET_TEXT_COLOR + "\n" +
                    "to login type:"+ SET_TEXT_COLOR_BLUE + " login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + "\n" +
                    "to exit chess type:"+ SET_TEXT_COLOR_BLUE + " quit" + RESET_TEXT_COLOR + "\n";
        }
        return  "to create a game type:"+ SET_TEXT_COLOR_BLUE + " create <NAME>" + RESET_TEXT_COLOR + "\n" +
                "to see all games type:" + SET_TEXT_COLOR_BLUE + " list" + RESET_TEXT_COLOR + "\n" +
                "to join a game type:" + SET_TEXT_COLOR_BLUE + " join <GAMEID> [WHITE|BLACK]" + RESET_TEXT_COLOR + "\n" +
                "to watch a game type:"+ SET_TEXT_COLOR_BLUE + " observe <GAMEID>" + RESET_TEXT_COLOR + "\n" +
                "to logout type:"+ SET_TEXT_COLOR_BLUE + " logout" + RESET_TEXT_COLOR + "\n" +
                "to exit chess type:"+ SET_TEXT_COLOR_BLUE + " quit" + RESET_TEXT_COLOR + "\n";
    }


    private void printPrompt() {
        System.out.print("[" + state + "] >>> ");
    }

    public String eval(String input) {

        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "help" -> help();
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "quit" -> "quit";
                default -> throw new IllegalStateException("Unexpected input: " + command +"\n");
            };

        } catch (ResponseException exep) {
            return printError(exep);
        }
    }


    private String register(String... params) throws ResponseException {
        if (state != State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You are logged in. Logout to register \n" + RESET_TEXT_COLOR;

        }
        if (params.length < 3) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: register <USERNAME> <PASSWORD> <EMAIL> \n" + RESET_TEXT_COLOR;
        }

        if (params.length > 3) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: register <USERNAME> <PASSWORD> <EMAIL> \n" + RESET_TEXT_COLOR;
        }
        RegisterResult result = server.register(params[0], params[1], params[2]);
        this.authToken = result.authToken();
        this.username = result.username();
        this.state = State.LOGGED_IN;
        return SET_TEXT_COLOR_GREEN + "Registered and logged in as " + username +"!\n" + RESET_TEXT_COLOR;
    }


    private String login(String... params) throws ResponseException {
        if (state == State.LOGGED_IN) {
            return SET_TEXT_COLOR_RED + "You're already logged in!\n"+ RESET_TEXT_COLOR;

        }

        if (params.length < 2) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: login <USERNAME> <PASSWORD>\n"+ RESET_TEXT_COLOR;

        }
        if (params.length > 2) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: login <USERNAME> <PASSWORD>\n"+ RESET_TEXT_COLOR;

        }



        LoginResult result = server.login(params[0], params[1]);
        this.authToken = result.authToken();
        this.username = result.username();
        this.state = State.LOGGED_IN;
        return SET_TEXT_COLOR_GREEN + "Logged in as " + username + "!\n" + RESET_TEXT_COLOR;



    }


    private String logout() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;

        }

        server.logout();
        this.authToken = null;
        this.username = null;
        this.state = State.LOGGED_OUT;


        return SET_TEXT_COLOR_GREEN + "You logged out! \n" + RESET_TEXT_COLOR;
    }

    private String createGame(String... params) throws ResponseException {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;

        }
        if (params.length < 1) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: create <GAME_NAME>\n"+ RESET_TEXT_COLOR;

        }
        if (params.length > 1) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: create <GAME_NAME>\n"+ RESET_TEXT_COLOR;

        }

        String game = String.join(" ", params);
        server.createGame(game);
        return SET_TEXT_COLOR_GREEN + "Created game " + game + "!\n" + RESET_TEXT_COLOR;


    }

    private String listGames() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;
        }
        ListGamesResult allGames = server.listGames();
        this.createdGames = allGames.games();

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < createdGames.size(); i++) {
            GameData game = createdGames.get(i);
            result.append(i + 1);
            result.append(" ");
            result.append(game.gameName());
            result.append(" - White: ");
            result.append(game.whiteUsername());
            result.append(", Black: ");
            result.append(game.blackUsername());
            result.append("\n");

        }
        return result.toString();


    }


    private String join(String... params) throws ResponseException{
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;
        }

        if (params.length < 2) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: join <NUMBER> <WHITE|BLACK>\n"+ RESET_TEXT_COLOR;
        }
        if (params.length > 2) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: join <NUMBER> <WHITE|BLACK>\n"+ RESET_TEXT_COLOR;
        }

        int gameNum;

        try {
            gameNum = parseInt(params[0]);

        }
        catch (NumberFormatException ex) {
            return SET_TEXT_COLOR_RED + "Please enter a number\n"+ RESET_TEXT_COLOR;

        }

        if (gameNum < 1 || gameNum > createdGames.size()) {
            return SET_TEXT_COLOR_RED + "Invalid game number! List games to see valid numbers\n"+ RESET_TEXT_COLOR;
        }

        String color = params[1].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return SET_TEXT_COLOR_RED + "Color must be WHITE or BLACK\n"+ RESET_TEXT_COLOR;

        }

        GameData game = createdGames.get(gameNum -1);
        server.joinGame(game.gameID(), color);
        ChessGame.TeamColor playerColor = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        DrawChessBoard.drawBoard(playerColor);

        return SET_TEXT_COLOR_GREEN + "Joined " + game.gameName() + " as " + color + "!\n" + RESET_TEXT_COLOR;


    }


    private String observe(String... params) throws ResponseException{
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;
        }

        if (params.length < 1) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: observe <NUMBER>\n"+ RESET_TEXT_COLOR;

        }

        if (params.length > 1) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: observe <NUMBER>\n"+ RESET_TEXT_COLOR;

        }

        int gameNum;

        try {
            gameNum = parseInt(params[0]);

        }
        catch (NumberFormatException ex) {
            return SET_TEXT_COLOR_RED + "Please enter a valid number\n"+ RESET_TEXT_COLOR;

        }

        if (gameNum < 1 || gameNum > createdGames.size()) {
            return SET_TEXT_COLOR_RED + "Invalid game number! List games to see valid numbers\n"+ RESET_TEXT_COLOR;
        }



        GameData game = createdGames.get(gameNum -1);
        DrawChessBoard.drawBoard(ChessGame.TeamColor.WHITE);

        return SET_TEXT_COLOR_GREEN + "Observing " + game.gameName() +  "!\n" + RESET_TEXT_COLOR;


    }



    private String parseErrors(String message) {

        if (message == null) {
            return "An error occurred";
        }

        if (message.toLowerCase().contains("bad request")) {
            return "Invalid input. Try again\n";
        }

        if (message.toLowerCase().contains("unauthorized")) {
            return "Not authorized. Try logging in again\n";
        }

        if (message.toLowerCase().contains("already exists")) {
            return "That username already exits. Choose a different one\n";
        }

        if (message.toLowerCase().contains("already taken")) {
            return "Color already taken! Try again with a different color or game\n";
        }

        return message;
    }

    private String printError(ResponseException exce) {
        String message = exce.getMessage();
        if(message == null) {
            message = "Request failed. Try again\n";
        }
        message = parseErrors(message);
        return SET_TEXT_COLOR_RED + message + RESET_TEXT_COLOR;
    }




}
