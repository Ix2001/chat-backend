package com.company.chat.config;


import com.company.chat.websocket.ChatWebSocketHandler;
import com.company.chat.websocket.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

/**
 * Регистрируем WebSocket-эндпоинт и JWT-интерсептор.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatWebSocketHandler handler;
    private final JwtHandshakeInterceptor jwtInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/chat")
                .addInterceptors(jwtInterceptor)
                .setAllowedOrigins("*");
    }
}
