package server;

import java.net.http.HttpClient;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverURL;


    public ServerFacade(String url) {
        serverURL = url;
    }

    public void register(String username, String password, String email) {
        throw new RuntimeException("not implemented!!");
    }

    public void login(String username, String password) {
        throw new RuntimeException("not implemented!!");
    }

    public void joinGame(Integer gameID, String color) {
        throw new RuntimeException("not implemented!!");
    }


    public void logout() {
        throw new RuntimeException("not implemented!!");
    }

    public void createGame(String gameName) {
        throw new RuntimeException("not implemented!!");
    }

    public void listGames() {
        throw new RuntimeException("not implemented!!");
    }

}
