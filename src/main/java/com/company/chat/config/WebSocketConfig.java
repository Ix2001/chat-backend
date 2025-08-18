package com.company.chat.config;

import com.company.chat.repository.UserRepository;
import com.company.chat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Обычный WS (без SockJS), чтобы Postman подключался напрямую:
        registry.addEndpoint("/ws/chat").setAllowedOriginPatterns("*");
        // Если у тебя где-то фронт на SockJS — можешь дополнительно оставить второй эндпоинт:
        // registry.addEndpoint("/ws/chat-sock").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                var accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Токен берём из заголовка CONNECT: "Authorization: Bearer <jwt>"
                    String auth = accessor.getFirstNativeHeader("Authorization");
                    if (auth == null || !auth.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Missing Bearer token");
                    }
                    String token = auth.substring(7);
                    if (!jwtService.isValid(token)) { // твой метод с 1 аргументом
                        throw new IllegalArgumentException("Invalid/expired token");
                    }
                    String username = jwtService.extractUsername(token);

                    var user = userRepo.findByUsername(username)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));

                    var auths = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles());
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(username, null, auths);

                    // ВАЖНО: кладём Principal в WS-сессию,
                    // дальше его получишь как аргумент `Principal` в @MessageMapping
                    accessor.setUser(authentication);
                }

                // Можно дополнительно запретить SEND без юзера:
                if (StompCommand.SEND.equals(accessor.getCommand()) && accessor.getUser() == null) {
                    throw new IllegalArgumentException("Unauthenticated SEND");
                }
                return message;
            }
        });
    }
}
