package com.company.chat.config;

import com.company.chat.security.TokenUserExtractor;
import com.company.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Валидация JWT при WebSocket handshake и сохранение username.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;
    private final TokenUserExtractor extractor;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest req,
                                   org.springframework.http.server.ServerHttpResponse resp,
                                   WebSocketHandler handler,
                                   Map<String, Object> attrs) {
        String token = null;

        var auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
        if (token == null) {
            var qp = UriComponentsBuilder.fromUri(req.getURI()).build().getQueryParams();
            token = qp.getFirst("token");
        }
        if (token == null) {
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            var tu  = extractor.from(jwt);
            var u   = userService.ensureFromToken(tu);

            attrs.put("userId", u.getId());        // локальный PK (Long)
            attrs.put("username", u.getUsername()); // для логов/удобства
            return true;
        } catch (Exception e) {
            log.warn("WS handshake failed: {}", e.getMessage());
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
