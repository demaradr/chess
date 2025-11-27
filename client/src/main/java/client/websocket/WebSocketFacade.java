package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notiHandler;
    Gson gson = new Gson();

    public WebSocketFacade(String url, NotificationHandler notiHandler) throws ResponseException {
        try {
            url = url.replace("http","ws");
            URI socketURI = new URI(url + "/ws");
            this.notiHandler = notiHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    notiHandler.notify(serverMessage);
                }

            });
        }
        catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    public void send(UserGameCommand command) throws ResponseException {
        try {
            String json = gson.toJson(command);
            this.session.getBasicRemote().sendText(json);

        }
        catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

}
