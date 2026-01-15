package fpt.tuanhm43.server.config;

import fpt.tuanhm43.server.constants.AppConstants;
import fpt.tuanhm43.server.services.TokenService;
import fpt.tuanhm43.server.services.impl.TokenServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
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
            String jwtToken = extractTokenFromRequest(request);

            if (StringUtils.hasText(jwtToken)) {
                if (tokenService instanceof TokenServiceImpl tokenServiceImpl && tokenServiceImpl.isBlacklisted(jwtToken)) {
                        log.warn("Blacklisted token attempted for: {} {}",
                                request.getMethod(), request.getRequestURI());
                        filterChain.doFilter(request, response);
                        return;
                    }


                if (!tokenService.validateToken(jwtToken)) {
                    log.warn("Invalid token for: {} {}",
                            request.getMethod(), request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                Authentication authentication = tokenService.getAuthenticationFromToken(jwtToken);

                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("User authenticated: {} with roles: {} for: {} {}",
                            authentication.getName(),
                            authentication.getAuthorities(),
                            request.getMethod(),
                            request.getRequestURI());
                } else {
                    log.warn("Failed to extract authentication from token for: {} {}",
                            request.getMethod(), request.getRequestURI());
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for: {} {} - {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
        } catch (Exception e) {
            log.error("Authentication error for: {} {} - {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AppConstants.JWT_HEADER_NAME);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AppConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(7).trim();
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.equals("/actuator/health") ||
                path.equals("/actuator/info") ||
                (path.startsWith("/api/v1/payments/") && path.contains("/webhook")) ||
                ("GET".equalsIgnoreCase(method) &&
                        (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/categories"))) ||
                path.startsWith("/api/v1/cart") ||
                path.startsWith("/api/v1/orders/track/") ||
                (path.equals("/api/v1/orders") && "POST".equalsIgnoreCase(method));
    }
}