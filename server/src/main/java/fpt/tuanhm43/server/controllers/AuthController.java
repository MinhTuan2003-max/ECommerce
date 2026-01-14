package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.auth.response.AuthResponse;
import fpt.tuanhm43.server.dtos.auth.request.LoginRequest;
import fpt.tuanhm43.server.dtos.auth.request.RegisterRequest;
import fpt.tuanhm43.server.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user identity management, including registration, login, token refresh, and password recovery.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with ROLE_USER and sends a verification email.")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Username or Email already exists")
    public ResponseEntity<ApiResponseDTO<Void>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.created(null, "User registered successfully. Please check your email for verification."));
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates credentials and returns an Access Token (JWT) and a Refresh Token.")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt for: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new Access Token using a valid Refresh Token. Use this when the current access token expires.")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> refresh(
            @Parameter(description = "The valid refresh token string") @RequestParam("token") String refreshToken) {
        log.info("Refreshing access token");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidates the current session and blacklists the token in Redis to prevent further use.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseDTO<Void>> logout(HttpServletRequest request) {
        log.info("Processing logout request");
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token != null) {
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Logged out successfully"));
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Allows an authenticated user to update their password. Requires current password verification.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Void>> changePassword(
            @Parameter(description = "Account username") @RequestParam("username") String username,
            @Parameter(description = "Current password") @RequestParam("oldPassword") String oldPassword,
            @Parameter(description = "New secure password") @RequestParam("newPassword") String newPassword) {
        log.info("User requesting password change: {}", username);
        authService.changePassword(username, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Password changed successfully"));
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Request password reset", description = "Triggers the password recovery flow by sending a unique token to the user's email.")
    public ResponseEntity<ApiResponseDTO<Void>> requestPasswordReset(
            @Parameter(description = "User's registered email", example = "tuan@example.com") @RequestParam("email") String email) {
        log.info("Password reset requested for email: {}", email);
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "If an account exists with this email, you will receive a password reset link"));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirm password reset", description = "Updates the user's password using the token received via email.")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(
            @Parameter(description = "UUID reset token from email") @RequestParam("token") String token,
            @Parameter(description = "New password to set") @RequestParam("newPassword") String newPassword) {
        log.info("Processing password reset with token");
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Password reset successfully. You can now login with your new password."));
    }
}