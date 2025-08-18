package com.company.chat.security;

import com.company.chat.repository.UserRepository;
import com.company.chat.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            String h = req.getHeader("Authorization");
            if (h != null && h.startsWith("Bearer ")
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String token = h.substring(7);

                if (jwtService.isValid(token)) {
                    String username = jwtService.extractUsername(token);

                    var user = userRepo.findByUsername(username).orElse(null);
                    Collection<? extends GrantedAuthority> auths =
                            (user != null && user.getRoles() != null && !user.getRoles().isBlank())
                                    ? AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles())
                                    : List.of();

                    var auth = new UsernamePasswordAuthenticationToken(username, null, auths);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            chain.doFilter(req, res);
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"token_expired\"}");
        }
    }
}
