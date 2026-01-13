package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.auth.response.AuthResponse;
import fpt.tuanhm43.server.dtos.auth.request.LoginRequest;
import fpt.tuanhm43.server.dtos.auth.request.RegisterRequest;
import fpt.tuanhm43.server.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponseDTO<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(null, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> refresh(@RequestParam("token") String refreshToken) {
        AuthResponse response = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout(@RequestParam(value = "token", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }
}
