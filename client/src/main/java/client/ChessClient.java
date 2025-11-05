package client;

import java.util.Arrays;
import java.util.Scanner;

public class ChessClient {

    private State state = State.LOGGED_OUT;


    public ChessClient() {
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
                case "quit" -> "quit";
                default -> help();
            };

        }

        catch (Exception exep) {
            return exep.getMessage();
        }
    }



}
