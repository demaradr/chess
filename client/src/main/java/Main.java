import client.ServerFacade;
import ui.CommandInterpreter;

public class Main {
    public static void main(String[] args) {
        var facade = new ServerFacade("http://localhost:8080");
        var interpreter = new CommandInterpreter(facade);
        interpreter.start();
    }
}