package com.company.chat.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private Key key() { return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); }
    @Value("${app.jwt.secret}") private String secret;          // base64
    @Value("${app.jwt.access-ttl:PT24H}") private Duration ttl;

    public String generate(String username, String rolesCsv) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesCsv)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key signingKey() {
        String raw = secret;
        byte[] keyBytes;
        if (raw != null && raw.startsWith("base64:")) {
            keyBytes = Decoders.BASE64.decode(raw.substring(7));
        } else {
            keyBytes = (raw == null ? "" : raw).getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token, String expectedUsername) {
        String username = extractUsername(token);
        return username != null && username.equals(expectedUsername) && !isExpired(token);
    }

    public boolean isExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }

    public boolean isValid(String token) {
        try {
            String username = extractUsername(token);
            return isValid(token, username);
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }
}
