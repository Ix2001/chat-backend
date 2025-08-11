package com.company.chat.security;

import com.company.chat.model.User;
import com.company.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Помощник для контроллеров: даёт локального User, создавая/обновляя его по JWT. */
@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final TokenUserExtractor extractor;
    private final UserService userService;

    public User ensure(@AuthenticationPrincipal Jwt jwt) {
        var tu = extractor.from(jwt);
        return userService.ensureFromToken(tu);
    }
}
