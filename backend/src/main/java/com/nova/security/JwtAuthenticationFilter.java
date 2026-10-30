package com.nova.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final CustomUserDetailsService users;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtService jwt, CustomUserDetailsService users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null) {
            if (!header.startsWith("Bearer ")) {
                reject(req, res, "Invalid Authorization header format");
                return;
            }
            String token = header.substring(7).trim();
            if (token.isEmpty()) {
                reject(req, res, "Missing bearer token");
                return;
            }
            try {
                String email = jwt.claims(token).getSubject();
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails details = users.loadUserByUsername(email);
                    if (jwt.valid(token, details.getUsername())) {
                        var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        reject(req, res, "Invalid or expired JWT. Please sign in again.");
                        return;
                    }
                }
            } catch (Exception ex) {
                reject(req, res, "Invalid or expired JWT. Please sign in again.");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private void reject(HttpServletRequest req, HttpServletResponse res, String message) throws IOException {
        SecurityContextHolder.clearContext();
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setHeader("Cache-Control", "no-store");
        res.getWriter().write(mapper.writeValueAsString(Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", message,
                "path", req.getRequestURI()
        )));
    }
}
