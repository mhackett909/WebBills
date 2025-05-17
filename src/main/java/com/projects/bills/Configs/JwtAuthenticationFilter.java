package com.projects.bills.Configs;

import com.projects.bills.Services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
//        System.out.println("JWT Filter: Incoming request to " + path);

        // Skip JWT filter for login and health endpoints
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/actuator/health")) {
//            System.out.println("JWT Filter: Skipping for " + path);
            chain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            System.out.println("JWT Filter: Missing or invalid Authorization header");
            chain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);
        Claims claims = null;
        try {
            claims = jwtService.validateJwt(token);
        } catch (ExpiredJwtException e) {
          //  System.out.println("JWT Filter: JWT token expired");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired JWT token");
            return;
        }

        if (claims == null) {
           // System.out.println("JWT Filter: Invalid JWT token");
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
            return;
        }

        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

//        System.out.println("JWT Filter: JWT is valid for user " + username);
        chain.doFilter(request, response);
    }
}

