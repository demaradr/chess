package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import model.UserData;
import request.*;
import response.*;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public LoginResponse register(UserData request) throws ResultException {
        return sendRequest("POST", "/user", request, LoginResponse.class, null);
    }

    public LoginResponse login(UserData request) throws ResultException {
        return sendRequest("POST", "/session", request, LoginResponse.class, null);
    }

    public void createGame(CreateGameRequest request) throws ResultException {
        sendRequest("POST", "/game", request, CreateGameResponse.class, request.authToken());
    }

    public void joinGame(JoinGameRequest request) throws ResultException {
        sendRequest("PUT", "/game", request, Object.class, request.authToken());
    }

    public void logout(String authToken) throws ResultException {
        sendRequest("DELETE", "/session", null, Object.class, authToken);
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws ResultException {
        return sendRequest("GET", "/game", null, ListGamesResponse.class, request.authToken());
    }

    private <T> T sendRequest(String method, String path, Object requestBody, Class<T> responseClass, String authToken) throws ResultException {
        HttpURLConnection connection = null;
        try {
            URL url = new URI(serverUrl + path).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);

            addHeaders(connection, authToken, requestBody != null);
            writeRequestBody(connection, requestBody);
            connection.connect();

            validateResponse(connection);
            return readResponseBody(connection, responseClass);

        } catch (ResultException e) {
            throw e;
        } catch (Exception e) {
            throw new ResultException(500, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void addHeaders(HttpURLConnection connection, String authToken, boolean hasBody) {
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }
        if (hasBody) {
            connection.setRequestProperty("Content-Type", "application/json");
        }
    }

    private void writeRequestBody(HttpURLConnection connection, Object requestBody) throws IOException {
        if (requestBody != null) {
            try (OutputStream out = connection.getOutputStream()) {
                out.write(gson.toJson(requestBody).getBytes());
            }
        }
    }

    private void validateResponse(HttpURLConnection connection) throws IOException, ResultException {
        int status = connection.getResponseCode();
        if (status / 100 != 2) {
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    throw ResultException.fromJson(errorStream, status);
                }
            }
            throw new ResultException(status, "Unexpected response: " + status);
        }
    }

    private <T> T readResponseBody(HttpURLConnection connection, Class<T> responseClass) throws IOException {
        if (responseClass == null || connection.getContentLength() == 0) {
            return null;
        }

        try (InputStream in = connection.getInputStream();
             Reader reader = new InputStreamReader(in)) {
            return gson.fromJson(reader, responseClass);
        }
    }
}
