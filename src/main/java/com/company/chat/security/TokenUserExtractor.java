// src/main/java/com/company/chat/security/TokenUserExtractor.java
package com.company.chat.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TokenUserExtractor {

    public TokenUser from(Jwt jwt) {
        String sub = jwt.getSubject(); // "sub"
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            String given = jwt.getClaimAsString("given_name");
            String family = jwt.getClaimAsString("family_name");
            displayName = String.format("%s %s",
                    given == null ? "" : given,
                    family == null ? "" : family).trim();
        }
        if (displayName.isBlank()) {
            displayName = username;
        }

        return new TokenUser(sub, username, displayName, email);
    }
}
