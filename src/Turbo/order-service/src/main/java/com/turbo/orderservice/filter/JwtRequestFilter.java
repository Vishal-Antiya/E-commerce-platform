package com.turbo.orderservice.filter;

import com.turbo.orderservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private UserDetailsService userDetailsService; // CustomUserDetailsService to load user details

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        List<String> roles = null;

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract the JWT (remove "Bearer ")
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                roles = jwtUtil.getRolesFromToken(jwt);
                logger.debug("JWT extracted for user: {} with roles: {}", username, roles);
            } catch (Exception e) {
                // Handle JWT exceptions (e.g., expired, invalid signature)
                logger.warn("JWT Error during parsing or validation: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Send 401 Unauthorized
                return; // Important: Stop processing the request!
            }
        } else {
            logger.trace("Authorization header is missing or does not start with Bearer for request: {}", request.getRequestURI());
        }

        // If username is extracted and no authentication exists in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Reconstruct UserDetails from token claims (username and roles)
            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // You might load UserDetails from your UserDetailsService if you need full user object
            // UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // OR create a simple UserDetails object if you only need username and roles from the token:
            UserDetails userDetails = new User(username, "", authorities); // Password is empty as it's not needed for token validation here

            if (jwtUtil.validateToken(jwt, userDetails)) {
                // If token is valid, set the authentication in the Spring Security context
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("Authentication set for user: {}", username);
            } else {
                logger.warn("JWT validation failed for user: {}", username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Send 401 Unauthorized
                return;
            }
        }
        filterChain.doFilter(request, response); // Continue the filter chain
    }
}
