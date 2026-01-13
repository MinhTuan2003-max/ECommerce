package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.auth.response.AuthResponse;
import fpt.tuanhm43.server.dtos.auth.request.LoginRequest;
import fpt.tuanhm43.server.dtos.auth.request.RegisterRequest;
import fpt.tuanhm43.server.entities.Role;
import fpt.tuanhm43.server.entities.User;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.repositories.RoleRepository;
import fpt.tuanhm43.server.repositories.UserRepository;
import fpt.tuanhm43.server.services.AuthService;
import fpt.tuanhm43.server.services.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        String accessToken = tokenService.generateAccessToken(authentication);
        String refreshToken = tokenService.generateRefreshToken(authentication);
        Long expiresAt = tokenService.getExpirationTime(accessToken);
        Long refreshExpiresAt = tokenService.getExpirationTime(refreshToken);

        return new AuthResponse(accessToken, refreshToken, expiresAt, refreshExpiresAt);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenService.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String username = tokenService.getUsernameFromToken(refreshToken);
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                userRepository.findByUsername(username)
                        .orElseThrow(() -> new BadRequestException("User not found"))
                        .getRoles()
                        .stream()
                        .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getName()))
                        .toList()
        );

        String newAccess = tokenService.generateAccessToken(authentication);
        Long expiresAt = tokenService.getExpirationTime(newAccess);
        return new AuthResponse(newAccess, refreshToken, expiresAt, tokenService.getExpirationTime(refreshToken));
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        user.getRoles().add(userRole);
        userRepository.save(user);

        // Return auth response after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String accessToken = tokenService.generateAccessToken(authentication);
        String refreshToken = tokenService.generateRefreshToken(authentication);
        Long expiresAt = tokenService.getExpirationTime(accessToken);
        Long refreshExpiresAt = tokenService.getExpirationTime(refreshToken);

        return new AuthResponse(accessToken, refreshToken, expiresAt, refreshExpiresAt);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Processing logout for token: {}", refreshToken.substring(0, 10) + "...");

        try {
            long expirationTime = tokenService.getExpirationTime(refreshToken);

            tokenService.blacklistToken(refreshToken, expirationTime);

            log.info("User logged out and token blacklisted successfully");
        } catch (Exception e) {
            log.warn("Logout attempt with invalid or expired token: {}", e.getMessage());
        }
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        // Implementation would typically generate reset token and send email
        // For MVP, just log the request
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Implementation would typically validate token and reset password
        // For MVP, just log the request
    }
}
