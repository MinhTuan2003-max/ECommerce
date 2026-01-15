package fpt.tuanhm43.server.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    // --- SECURITY CONSTANTS ---
    public static final String JWT_HEADER_NAME = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // --- ROLES ---
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STAFF = "STAFF";

    // --- CART CONFIGURATION ---
    public static final int CART_SESSION_TIMEOUT_HOURS = 24;

    // --- API ---
    public static final String API_PRODUCT = "/api/v1/products/**";
    public static final String API_CATEGORY = "/api/v1/categories/**";
    public static final String API_ADMIN = "/api/v1/admin/**";
    public static final String API_ADMIN_ORDER = "/api/v1/admin/orders/**";
    public static final String API_ADMIN_INVENTORY = "/api/v1/admin/inventory/**";

    // --- PUBLIC ENDPOINTS ---
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/api/v1/cart/**",
            "/api/v1/orders/**",
            "/api/v1/orders/track/**",
            "/api/v1/payments/sepay/webhook",
            "/api/v1/payments/*/webhook",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info"
    };
}