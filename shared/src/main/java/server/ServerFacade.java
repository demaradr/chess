package server;
import com.google.gson.Gson;
import exception.ResponseException;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.LoginResult;
import results.RegisterResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverURL;
    private String authToken;


    public ServerFacade(String url) {
        serverURL = url;
    }

    public RegisterResult register(String username, String password, String email) throws ResponseException{
        var requestBody = new RegisterRequest(username,password,email);
        var request = buildRequest("POST", "/user", requestBody);
        var response = sendRequest(request);
        var result = handleResponse(response, RegisterResult.class);
        this.authToken = result.authToken();
        return result;
    }

    public LoginResult login(String username, String password) throws ResponseException {
        var requestBody = new LoginRequest(username,password);
        var request = buildRequest("POST", "/session", requestBody);
        var response = sendRequest(request);
        var result = handleResponse(response, LoginResult.class);
        this.authToken = result.authToken();

        return result;
    }

    public void joinGame(Integer gameID, String color) throws ResponseException {
        if (authToken == null) {
            throw new ResponseException(ResponseException.Code.ClientError, "User not logged in!");

        }

        var request = buildRequest("PUT", "/game", new JoinGameRequest(color, gameID));
        var response = sendRequest(request);
        handleResponse(response, JoinGameRequest.class);
    }


    public void logout() throws ResponseException{
        if (authToken == null) {
            throw new ResponseException(ResponseException.Code.ClientError, "User not logged in!");

        }
        var request = buildRequest("DELETE", "/session", null);
        var response = sendRequest(request);
        handleResponse(response, null);
        this.authToken = null;


    }

    public CreateGameResult createGame(String gameName) throws ResponseException {
        if (authToken == null) {
            throw new ResponseException(ResponseException.Code.ClientError, "User not logged in!");

        }
        var request = buildRequest("POST", "/game", null);
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResult.class);

    }

    public ListGamesResult listGames() throws ResponseException {
        if (authToken == null) {
            throw new ResponseException(ResponseException.Code.ClientError, "User not logged in!");

        }
        var request = buildRequest("GET", "/game", null);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResult.class);
    }


    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }


    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder().uri(URI.create(serverURL + path));

        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        return request.method(method, makeRequestBody(body)).build();



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
