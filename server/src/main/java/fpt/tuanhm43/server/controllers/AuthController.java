package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.auth.response.AuthResponse;
import fpt.tuanhm43.server.dtos.auth.request.LoginRequest;
import fpt.tuanhm43.server.dtos.auth.request.RegisterRequest;
import fpt.tuanhm43.server.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Controller
 * Handles user authentication, registration, and password management
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     * Public endpoint - no authentication required
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponseDTO<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(null, "User registered successfully. Please check your email for verification."));
    }

    /**
     * Login user
     * Public endpoint - no authentication required
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("User login attempt for: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Login successful"));
    }

    /**
     * Refresh access token using refresh token
     * Public endpoint
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> refresh(
            @RequestParam("token") String refreshToken) {
        log.info("Refreshing access token");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Token refreshed successfully"));
    }

    /**
     * Logout user
     * Authenticated users only
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Void>> logout(
            @RequestParam(value = "token", required = false) String refreshToken) {
        log.info("User logout");
        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Logged out successfully"));
    }

    /**
     * Change password for authenticated user
     * Authenticated users only
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Void>> changePassword(
            @RequestParam("username") String username,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        log.info("User requesting password change: {}", username);
        authService.changePassword(username, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Password changed successfully"));
    }

    /**
     * Request password reset
     * Public endpoint - no authentication required
     * User will receive reset link via email
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponseDTO<Void>> requestPasswordReset(
            @RequestParam("email") String email) {
        log.info("Password reset requested for email: {}", email);
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(
                ApiResponseDTO.success(null, "If an account exists with this email, you will receive a password reset link"));
    }

    /**
     * Reset password with token
     * Public endpoint - no authentication required
     * Token is provided in email link
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword) {
        log.info("Processing password reset with token");
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Password reset successfully. You can now login with your new password."));
    }
}
