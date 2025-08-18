// com.company.chat.security.ws.PrincipalHandshakeHandler
package com.company.chat.security.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class PrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String username = (String) attributes.get("username");
        @SuppressWarnings("unchecked")
        Collection<? extends GrantedAuthority> auths =
                (Collection<? extends GrantedAuthority>) attributes.getOrDefault("authorities", List.of());

        if (username == null) return null;
        return new UsernamePasswordAuthenticationToken(username, null, auths);
    }
}
