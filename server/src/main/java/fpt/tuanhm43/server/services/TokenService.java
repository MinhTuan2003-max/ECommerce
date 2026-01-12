package fpt.tuanhm43.server.services;

import org.springframework.security.core.Authentication;

/**
 * Token Service Interface
 *
 * Xử lý JWT token generation, validation, và authentication extraction
 */
public interface TokenService {

    /**
     * Generate JWT access token
     *
     * @param authentication Spring Security Authentication object
     * @return JWT token string
     */
    String generateAccessToken(Authentication authentication);

    /**
     * Generate JWT refresh token
     *
     * @param authentication Spring Security Authentication object
     * @return Refresh token string
     */
    String generateRefreshToken(Authentication authentication);

    /**
     * Validate JWT token
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Get Authentication object from JWT token
     *
     * @param token JWT token string
     * @return Authentication object or null if invalid
     */
    Authentication getAuthenticationFromToken(String token);

    /**
     * Extract username from JWT token
     *
     * @param token JWT token string
     * @return username
     */
    String getUsernameFromToken(String token);

    /**
     * Get token expiration time in milliseconds
     *
     * @param token JWT token string
     * @return expiration timestamp
     */
    Long getExpirationTime(String token);
}