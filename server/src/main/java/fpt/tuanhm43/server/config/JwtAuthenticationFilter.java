package fpt.tuanhm43.server.config;

import fpt.tuanhm43.server.constants.AppConstants;
import fpt.tuanhm43.server.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from request
            String jwtToken = extractTokenFromRequest(request);

            // If token exists, validate and set authentication
            if (StringUtils.hasText(jwtToken)) {
                Authentication authentication = tokenService.getAuthenticationFromToken(jwtToken);

                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication for user: {} with authorities: {}",
                            authentication.getName(),
                            authentication.getAuthorities());
                } else {
                    log.debug("Invalid token, authentication not set");
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AppConstants.JWT_HEADER_NAME);

        if (StringUtils.hasText(bearerToken) &&
                bearerToken.startsWith(AppConstants.JWT_TOKEN_PREFIX)) {
            return bearerToken.substring(AppConstants.JWT_TOKEN_PREFIX.length()).trim();
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/api/v1/products") ||
                path.startsWith("/api/v1/categories") ||
                path.startsWith("/api/v1/cart") ||
                path.startsWith("/api/v1/orders/track/") ||
                path.startsWith("/api/v1/payments/") && path.contains("/webhook") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.equals("/actuator/health") ||
                path.equals("/actuator/info");
    }
}