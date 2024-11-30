package barh.hangmantask.config;

import barh.hangmantask.handler.GameWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;


import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final GameWebSocketHandler gameWebSocketHandler;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        return new SimpleUrlHandlerMapping(Map.of("/game", gameWebSocketHandler),1); // Maps the WebSocketHandler to a URL path
    }
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
