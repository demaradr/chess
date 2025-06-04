import client.ServerFacade;

public class Main {
    public static void main(String[] args) {
        var facade = new ServerFacade("http://localhost:8080");
        var eval = new CommandInterpreter(facade);
        eval.start();
    }
}