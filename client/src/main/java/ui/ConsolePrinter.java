package ui;

public class ConsolePrinter {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void printWelcome() {
        System.out.println("Welcome to the Chess client!");
    }

    public void printNotification(String message) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_GREEN + message);
        printPrompt();
    }

    public void printError(String errorMessage) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + errorMessage);
    }

    public void printResponse(String response) {
        System.out.println(EscapeSequences.SET_TEXT_BOLD_AND_BLUE + response);
    }

    public void printPrompt() {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_TEXT_BOLD +
                "[" + username + "]: ");
    }

    public void printSubCommand(String label) {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_TEXT_BOLD +
                "\n" + label + ": ");
    }
}
