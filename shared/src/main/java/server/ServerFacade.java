package server;
import com.google.gson.Gson;
import exception.ResponseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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


    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder().uri(URI.create(serverURL + path)).method(method, makeRequestBody(body));

        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        return request.build();



    }


    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        }
        else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }


    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());

        }
        catch (Exception exep){
            throw new ResponseException(ResponseException.Code.ServerError, exep.getMessage());


        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();


        if (!isSuccessful(status)) {
            var body = response.body();

            if (body != null) {
                throw ResponseException.fromJSON(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }


        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }


}
