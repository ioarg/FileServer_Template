package jarg.templates.FileServer.config;

import jarg.templates.FileServer.notifications.websockets.CustomWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /************************************************
     *   Websocket Beans
     ************************************************/
    @Bean
    public CustomWebSocketHandler wbsConnectionsHandler(){
        return new CustomWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(wbsConnectionsHandler(), "/notifications_wbs");
    }

}
