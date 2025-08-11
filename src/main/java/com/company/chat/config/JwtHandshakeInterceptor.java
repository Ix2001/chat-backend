// src/main/java/com/company/chat/websocket/JwtHandshakeInterceptor.java
package com.company.chat.websocket;

import com.company.chat.model.User;
import com.company.chat.security.TokenUserExtractor;
import com.company.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;
    private final TokenUserExtractor extractor;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                   org.springframework.http.server.ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        HttpHeaders headers = request.getHeaders();
        String bearer = headers.getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (bearer != null && bearer.startsWith("Bearer ")) {
            token = bearer.substring(7);
        }
        if (token == null) {
            // Дополнительно можно поддержать ?token= для браузера
            var uri = request.getURI().toString();
            int idx = uri.indexOf("token=");
            if (idx > 0) token = uri.substring(idx + 6);
        }
        if (token == null) return false;

        Jwt jwt = jwtDecoder.decode(token);
        var tu = extractor.from(jwt);
        User u = userService.ensureFromToken(tu);   // upsert локального юзера
        attributes.put("userId", u.getId());
        return true;
    }

    @Override public void afterHandshake(
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            org.springframework.web.socket.WebSocketHandler wsHandler,
            Exception exception) { }
}
