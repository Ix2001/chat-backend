package com.company.chat.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TokenUserExtractor {

    public TokenUser from(Jwt jwt) {
        // username: берём по приоритету
        String username = firstNonBlank(
                claim(jwt, "preferred_username"),
                claim(jwt, "upn"),           // иногда встречается
                claim(jwt, "email"),
                claim(jwt, "phone_number"),
                jwt.getSubject()             // самый последний фолбэк
        );

        // displayName: name или given_name + family_name, иначе username
        String dn = claim(jwt, "name");
        if (isBlank(dn)) {
            String gn = claim(jwt, "given_name");
            String fn = claim(jwt, "family_name");
            String both = ((gn != null ? gn : "") + " " + (fn != null ? fn : "")).trim();
            dn = isBlank(both) ? username : both;
        }

        return new TokenUser(
                username,
                dn,
                claim(jwt, "email"),
                claim(jwt, "phone_number")
        );
    }

    private static String claim(Jwt jwt, String name) {
        Object v = jwt.getClaim(name);
        return v != null ? String.valueOf(v) : null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (!isBlank(v)) return v;
        return null;
    }
}
