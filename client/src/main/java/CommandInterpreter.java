import chess.*;
import client.ResultException;
import client.ServerFacade;
import model.*;
import ui.EscapeSequences;
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

}}
