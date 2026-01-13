package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.auth.request.LoginRequest;
import fpt.tuanhm43.server.dtos.auth.request.RegisterRequest;
import fpt.tuanhm43.server.dtos.auth.response.AuthResponse;

/**
 * Auth Service Interface
 */
public interface AuthService {

    /**
     * Register new user
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Login
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh token
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Logout
     */
    void logout(String token);

    /**
     * Change password
     */
    void changePassword(String username, String oldPassword, String newPassword);

    /**
     * Request password reset
     */
    void requestPasswordReset(String email);

    /**
     * Reset password with token
     */
    void resetPassword(String token, String newPassword);
}


