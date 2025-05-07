package com.turbo.userservice.filter;

import com.turbo.userservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;  //  Implement this

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract the JWT (remove "Bearer ")
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
            } catch (Exception e) {
                //  Handle JWT exceptions (e.g., expired, invalid signature)
                logger.warn("JWT Error: {"+ e.getMessage() +"}"); //  Log the error
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //  Send 401
                return; //  Important:  Stop processing the request!
            }
        } else {
            logger.trace("Authorization header is missing or does not start with Bearer");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //  Double check if username is not null and there is no authentication already

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);  //  Use your UserDetailsService
            if (jwtUtil.validateToken(jwt, userDetails)) {
                //  if token is valid
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken); //  Set the authentication in the context.
            }
        }
        filterChain.doFilter(request, response); //  Continue the filter chain
    }
}