package com.turbo.orderservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // Import Keys for secret handling
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Base64; // Import Base64

@Component
public class JwtUtil {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.expiration}")
    private long jwtExpirationInMs;

    // Generate token with roles
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = Map.of(
                "roles", userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        // Ensure the secret key is safely decoded for signing
        byte[] decodedSecret = Base64.getDecoder().decode(secret);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Set expiration time (e.g., 10 hours from now)
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                // Sign with HS256 algorithm and the decoded secret key
                .signWith(Keys.hmacShaKeyFor(decodedSecret), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Extract roles from token
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        // Assuming roles are stored as a List
        return claims.get("roles", List.class);
    }

    // Extract a specific claim
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimResolver.apply(claims);
    }

    // Retrieve all claims from token
    private Claims getAllClaimsFromToken(String token) {
        // Ensure the secret key is safely decoded for parsing
        byte[] decodedSecret = Base64.getDecoder().decode(secret);

        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(decodedSecret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return getClaimFromToken(token, Claims::getExpiration).before(new Date());
    }

    // Validate token
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
