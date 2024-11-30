package barh.hangmantask.config;

import barh.hangmantask.handler.GameWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final GameWebSocketHandler gameWebSocketHandler;

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/path", gameWebSocketHandler);
        int order = -1; // before annotated controllers

        return new SimpleUrlHandlerMapping(map, order);
    }
}


/*    @Bean
    public HandlerMapping reactiveWebSocketHandlerMapping() {
        Map<String, GameWebSocketHandler> map = new ConcurrentHashMap<>();
        map.put("/game", gameWebSocketHandler);
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }*/

    /*    @Bean
        public HandlerMapping webSocketHandlerMapping() {
            return new SimpleUrlHandlerMapping(Map.of("/game", gameWebSocketHandler),1); // Maps the WebSocketHandler to a URL path
        }*/
/*    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }
    @Bean
    public WebSocketService webSocketService() {
        TomcatRequestUpgradeStrategy tomcatRequestUpgradeStrategy = new TomcatRequestUpgradeStrategy();
        tomcatRequestUpgradeStrategy.setMaxSessionIdleTimeout(10000L);
        tomcatRequestUpgradeStrategy.setAsyncSendTimeout(10000L);
        return new HandshakeWebSocketService(tomcatRequestUpgradeStrategy);
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
    }
    */

