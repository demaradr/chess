package websocket;

import io.javalin.websocket.*;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    @Override
    public void handleConnect(WsConnectContext context) {
        System.out.println("Websocket connected!");
        context.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext context) {
        System.out.print(context.message());
    }

    @Override
    public void handleClose(WsCloseContext context) {
        System.out.println("WebSocket closed!");
    }
}
