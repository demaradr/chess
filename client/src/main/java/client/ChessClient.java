package client;

import exception.ResponseException;
import results.LoginResult;
import results.RegisterResult;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {

    private State state = State.LOGGED_OUT;
    private final ServerFacade server;
    private String authToken;
    private String username;


    public ChessClient(String serverURL) {
        this.server = new ServerFacade(serverURL);
    }

    public void run() {
        System.out.println("Hello there! Welcome to 240 Chess ♕ Type help to start :)");

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();

            String line = scanner.nextLine();

            try {
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
            return """
                    to register type: register <USERNAME> <PASSWORD> <EMAIL>
                    to login type: login <USERNAME> <PASSWORD>
                    to exit chess type: quit
                    """;
        }
        return """
                to create a game type: create <NAME>
                to see all games type: list
                to join a game type: join <GAMEID> [WHITE|BLACK]
                to watch a game type: observe <GAMEID>
                to logout type: logout
                to exit chess type: quit
                """;
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
                case "quit" -> "quit";
                default -> throw new IllegalStateException("Unexpected input: " + command +"\n");
            };

        } catch (Exception exep) {
            return exep.getMessage();
        }
    }


    private String register(String... params) throws ResponseException {
        if (state != State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You are logged in. Logout to register \n" + RESET_TEXT_COLOR;

        }
        if (params.length < 3) {
            return SET_TEXT_COLOR_RED + "Wrong input! Type: register <USERNAME> <PASSWORD> <EMAIL> \n" + RESET_TEXT_COLOR;
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

        if (params.length < 1) {
            return SET_TEXT_COLOR_RED + "Wrong input! Type: login <USERNAME> <PASSWORD>\n"+ RESET_TEXT_COLOR;

        }

        LoginResult result = server.login(params[0], params[1]);
        this.authToken = result.authToken();
        this.username = result.username();
        this.state = State.LOGGED_IN;
        return SET_TEXT_COLOR_GREEN + "Loggined in as " + username + "!\n" + RESET_TEXT_COLOR;



    }


    private String logout() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;

        }

        server.logout();
        this.authToken = null;
        this.username = null;
        this.state = State.LOGGED_OUT;


        return SET_TEXT_COLOR_GREEN + "You logged out!" + RESET_TEXT_COLOR;
    }




}
