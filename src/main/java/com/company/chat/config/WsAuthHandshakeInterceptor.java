package com.company.chat.config;

import com.company.chat.repository.UserRepository;
import com.company.chat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.*;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WsAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   org.springframework.http.server.ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String token = null;
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }


        if (token == null && request instanceof ServletServerHttpRequest ssr) {
            String q = ssr.getServletRequest().getParameter("token");
            if (StringUtils.hasText(q)) token = q;
        }

        if (!StringUtils.hasText(token) || !jwtService.isValid(token)) {
            return false; // handshake отклонён
        }

        String username = jwtService.extractUsername(token);
        attributes.put("username", username);

        var user = userRepo.findByUsername(username).orElse(null);
        var auths = user != null
                ? AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles())
                : List.of();
        attributes.put("authorities", auths);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               org.springframework.http.server.ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) { }
}