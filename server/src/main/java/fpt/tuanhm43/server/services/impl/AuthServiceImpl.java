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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
    public AuthResponse refresh(String refreshToken) {
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
    public void register(RegisterRequest request) {
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
    }

    @Override
    public void logout(String refreshToken) {
        // For stateless JWT, logout can be a no-op or implement blacklist.
        // MVP: no-op
    }
}
