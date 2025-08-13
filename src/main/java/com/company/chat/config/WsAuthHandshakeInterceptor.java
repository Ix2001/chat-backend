package com.company.chat.websocket;

import com.company.chat.repository.UserRepository;
import com.company.chat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WsAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwt;
    private final UserRepository userRepo;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) return false;

        String token = auth.substring(7);
        String username;
        try {
            username = jwt.extractUsername(token);
        } catch (Exception e) {
            return false;
        }

        var user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return false;

        attributes.put("username", username);
        attributes.put("userId", user.getId());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
