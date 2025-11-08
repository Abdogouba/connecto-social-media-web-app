package com.socialmedia.connecto.auth;

import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            String username = null;
            String role = null;
            if (jwtUtil.validateToken(token)) {
                try {
                    username = jwtUtil.extractUsername(token);
                    role = jwtUtil.extractRole(token);
                } catch (Exception e) {
                    // not authenticated
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Check if user is banned
                User user = userRepository.findByEmail(username).orElse(null);
                if (user != null && user.isBanned()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("User is currently banned from the platform");
                    return; // stop filter chain
                }

                // Create authorities from token role
                var authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                // Authenticate user with role from JWT
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}


