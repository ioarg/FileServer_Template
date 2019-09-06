package jarg.templates.FileServer.notifications.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomWebSocketHandler extends TextWebSocketHandler {
    private List<WebSocketSession> wbsSessions;
    private final Logger logger = LoggerFactory.getLogger(CustomWebSocketHandler.class);

    public CustomWebSocketHandler(){
        wbsSessions = Collections.synchronizedList(new ArrayList<>());
    }

    /***********************************************************
     *   Utilities
     ************************************************************/
    //Subscribe for notifications
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        wbsSessions.add(session);
        logger.info("Wbs Subscription");
    }

    //Ignore messages from clients at this point
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }

    //Broadcast messages to subscribers
    public void broadcastMessage(String message){
        logger.info("Wbs Sending message");
        List<WebSocketSession> toBeRemoved = new ArrayList<>();
        for(WebSocketSession wbsSession : wbsSessions){
            try {
                wbsSession.sendMessage(new TextMessage(message.getBytes()));
            } catch (Exception e) {
                logger.error(e.getMessage());
                toBeRemoved.add(wbsSession);
            }
        }
        for(WebSocketSession session : toBeRemoved){
            wbsSessions.remove(session);
        }
    }
}
