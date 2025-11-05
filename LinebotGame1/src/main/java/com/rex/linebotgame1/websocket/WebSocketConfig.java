package com.rex.linebotgame1.websocket;

import com.rex.linebotgame1.handler.GameWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public GameWebSocketHandler gameWebSocketHandler()  {
        return new GameWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 註冊 endpoint： ws://{host}:{port}/ws/game
        registry.addHandler(gameWebSocketHandler(), "/ws/game")
                .setAllowedOrigins("*"); // 測試可用，正式請限制來源
    }

}
