package fpt.tuanhm43.server.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    // --- SECURITY CONSTANTS ---
    public static final String JWT_HEADER_NAME = "Authorization";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final long JWT_EXPIRATION = 86400000;

    // --- API PATHS ---
    public static final String API_V1_PREFIX = "/api/v1";
    public static final String AUTH_WHITELIST_PATTERN = API_V1_PREFIX + "/auth/**";

    // --- ORDER STATUS (Dựa trên thiết kế DB của bạn) ---
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_PAID = "PAID";
    public static final String ORDER_STATUS_SHIPPING = "SHIPPING";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STAFF = "STAFF";

    // Error Codes
    public static final String ERR_UNAUTHORIZED = "UNAUTHORIZED";

    // --- PAYMENT METHODS ---
    public static final String PAYMENT_METHOD_COD = "COD";
    public static final String PAYMENT_METHOD_SEPAY = "SEPAY";

    // --- PAGINATION DEFAULTS (Dành cho Catalog API) ---
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // --- ERROR CODES (Dùng cho ApiResponseDTO) ---
    public static final String ERROR_CODE_FORBIDDEN = "FORBIDDEN";
    public static final String ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERROR_CODE_VALIDATION = "VALIDATION_ERROR";
    public static final String ERROR_CODE_INTERNAL_SERVER = "INTERNAL_SERVER_ERROR";
}