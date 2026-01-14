package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.order.request.CreateOrderRequest;
import fpt.tuanhm43.server.dtos.order.request.OrderFilterRequest;
import fpt.tuanhm43.server.dtos.order.request.UpdateOrderStatusRequest;
import fpt.tuanhm43.server.dtos.order.response.OrderDetailResponse;
import fpt.tuanhm43.server.dtos.order.response.OrderResponse;
import fpt.tuanhm43.server.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "Endpoints for creating, tracking, and managing customer orders.")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/from-cart")
    @Operation(summary = "Create order from cart", description = "Converts the current session-based cart into a formal order. Can be used by both Guests and Registered Users.")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> createOrderFromCart(
            @Valid @RequestBody CreateOrderRequest request,
            @Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Creating order from cart for session: {}", sessionId);
        OrderDetailResponse response = orderService.createOrderFromCart(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Order created successfully from cart"));
    }

    @PostMapping
    @Operation(summary = "Create direct order", description = "Creates a new order directly without using the cart session. Required role: CUSTOMER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating direct order for authenticated user");
        OrderDetailResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Order created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Fetch detailed information of a specific order including order items. Required role: ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> getOrderById(
            @Parameter(description = "UUID of the order") @PathVariable("id") UUID id) {
        log.info("Fetching order details for ID: {}", id);
        OrderDetailResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/track/{trackingToken}")
    @Operation(summary = "Track order (Guest)", description = "Allows guests to track their order status using a unique tracking token sent to their email. No login required.")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> getOrderByTrackingToken(
            @Parameter(description = "Secure unique tracking token") @PathVariable("trackingToken") UUID trackingToken) {
        log.info("Tracking order with token: {}", trackingToken);
        OrderDetailResponse response = orderService.getOrderByTrackingToken(trackingToken);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/user/my-orders")
    @Operation(summary = "Get my orders", description = "Retrieve a paginated list of orders placed by the currently authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<OrderResponse>>> getUserOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(name = "size", defaultValue = "10") int size) {
        UUID userId = getCurrentUserId();
        log.info("Fetching orders for user: {}", userId);
        PageResponseDTO<OrderResponse> response = orderService.getUserOrders(userId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping
    @Operation(summary = "Filter all orders (Admin)", description = "Search and filter through all orders in the system with pagination. Required role: ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<OrderResponse>>> getAllOrders(
            @Valid OrderFilterRequest filter) {
        log.info("Admin fetching all orders with filter: {}", filter);
        PageResponseDTO<OrderResponse> response = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Change the order status (e.g., PENDING to SHIPPED). Required role: ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> updateOrderStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderDetailResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Allows customers to cancel their own order or Admin to cancel any order. Required role: CUSTOMER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> cancelOrder(
            @PathVariable("id") UUID id,
            @Parameter(description = "Reason for cancellation") @RequestParam(value = "reason", required = false) String reason) {
        log.info("Cancelling order: {}", id);
        OrderDetailResponse response = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    private UUID getCurrentUserId() {
        return UUID.randomUUID();
    }
}