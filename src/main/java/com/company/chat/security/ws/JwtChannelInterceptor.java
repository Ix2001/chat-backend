package com.company.chat.security.ws;

import com.company.chat.repository.UserRepository;
import com.company.chat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = null;
            String auth = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
            if (token == null) token = accessor.getFirstNativeHeader("token");

            if (!StringUtils.hasText(token) || !jwtService.isValid(token)) {
                throw new IllegalArgumentException("Unauthorized: invalid JWT");
            }

            String username = jwtService.extractUsername(token);
            var user = userRepo.findByUsername(username).orElse(null);
            Collection<? extends GrantedAuthority> auths =
                    (user != null && user.getRoles() != null && !user.getRoles().isBlank())
                            ? AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles())
                            : Collections.emptyList();
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(username, null, auths);

            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            if (accessor.getUser() instanceof Authentication a) {
                SecurityContextHolder.getContext().setAuthentication(a);
            }
        }
        return message;
    }
}
