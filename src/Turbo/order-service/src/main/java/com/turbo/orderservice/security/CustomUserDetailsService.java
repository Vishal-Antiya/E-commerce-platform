package com.turbo.orderservice.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // In a real microservice, this would typically be used if the Order Service
        // needed to fetch user details from its own local store or from the User Service.
        // For JWT validation, the JwtRequestFilter might construct UserDetails directly
        // from token claims (username and roles) without hitting this method.
        // If this method is called, it means Spring Security is trying to load a user
        // from a database or other source, which might not be the primary authentication flow for JWT.

        // For the purpose of getting Spring Security to work with the JWT filter,
        // we'll return a dummy user. The actual authentication details
        // (including roles) will be set by JwtRequestFilter.
        // The password is not used for token validation here.
        return new User(username, "", new ArrayList<>());
    }
}
