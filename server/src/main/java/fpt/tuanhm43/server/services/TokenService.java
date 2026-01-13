package fpt.tuanhm43.server.services;

import org.springframework.security.core.Authentication;

/**
 * Token Service Interface
 */
public interface TokenService {

    /**
     * Generate access token
     */
    String generateAccessToken(Authentication authentication);

    /**
     * Generate refresh token
     */
    String generateRefreshToken(Authentication authentication);

    /**
     * Validate token
     */
    boolean validateToken(String token);

    /**
     * Get authentication from token
     */
    Authentication getAuthenticationFromToken(String token);

    /**
     * Get username from token
     */
    String getUsernameFromToken(String token);

    /**
     * Get expiration time
     */
    Long getExpirationTime(String token);

    /**
     * Blacklist token
     */
    void blacklistToken(String token, long expirationTimeInMillis);

    /**
     * Check if token is blacklisted
     */
    boolean isBlacklisted(String token);
}