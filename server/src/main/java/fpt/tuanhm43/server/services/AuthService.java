package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.auth.response.AuthResponse;
import fpt.tuanhm43.server.dtos.auth.request.LoginRequest;
import fpt.tuanhm43.server.dtos.auth.request.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void register(RegisterRequest request);
    void logout(String refreshToken);
}

