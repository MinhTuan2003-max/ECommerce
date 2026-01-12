package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.auth.AuthResponse;
import fpt.tuanhm43.server.dtos.auth.LoginRequest;
import fpt.tuanhm43.server.dtos.auth.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void register(RegisterRequest request);
    void logout(String refreshToken);
}

