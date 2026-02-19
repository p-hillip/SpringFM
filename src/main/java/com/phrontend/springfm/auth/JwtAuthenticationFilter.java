package com.phrontend.springfm.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        log.debug("JWT Filter: {} {} {}", request.getMethod(), requestUri,
            queryString != null ? "?" + queryString : "");

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.validateToken(token)) {
                Claims claims = jwtService.parseToken(token);
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                Boolean canUpload = claims.get("canUpload", Boolean.class);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (Boolean.TRUE.equals(canUpload)) {
                    authorities.add(new SimpleGrantedAuthority("UPLOAD"));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authenticated user: {}", userId);
            } else {
                log.debug("JWT token validation failed");
            }
        } else {
            log.debug("No Authorization header or not Bearer token");
        }

        filterChain.doFilter(request, response);
    }
}
