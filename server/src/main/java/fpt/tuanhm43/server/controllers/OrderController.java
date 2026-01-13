package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.order.request.CreateOrderRequest;
import fpt.tuanhm43.server.dtos.order.request.OrderFilterRequest;
import fpt.tuanhm43.server.dtos.order.request.UpdateOrderStatusRequest;
import fpt.tuanhm43.server.dtos.order.response.OrderDetailResponse;
import fpt.tuanhm43.server.dtos.order.response.OrderResponse;
import fpt.tuanhm43.server.services.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Order Controller
 * Handles order creation, retrieval, and management
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create order from cart (guest or user)
     */
    @PostMapping("/from-cart")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> createOrderFromCart(
            @Valid @RequestBody CreateOrderRequest request,
            HttpSession session) {
        String sessionId = session.getId();
        OrderDetailResponse response = orderService.createOrderFromCart(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Order created successfully from cart"));
    }

    /**
     * Create order directly without cart
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDetailResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Order created successfully"));
    }

    /**
     * Get order by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> getOrderById(
            @PathVariable("id") UUID id) {
        OrderDetailResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Get order by tracking token (Guest)
     * Used for tracking orders without authentication
     */
    @GetMapping("/track/{trackingToken}")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> getOrderByTrackingToken(
            @PathVariable("trackingToken") UUID trackingToken) {
        OrderDetailResponse response = orderService.getOrderByTrackingToken(trackingToken);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Get user's orders (Authenticated users)
     */
    @GetMapping("/user/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<OrderResponse>>> getUserOrders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        // Get current user from SecurityContext
        UUID userId = getCurrentUserId();
        PageResponseDTO<OrderResponse> response = orderService.getUserOrders(userId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Get all orders with filters (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<OrderResponse>>> getAllOrders(
            @Valid OrderFilterRequest filter) {
        PageResponseDTO<OrderResponse> response = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Update order status (Admin only)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> updateOrderStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderDetailResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Cancel order (User or Admin)
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<OrderDetailResponse>> cancelOrder(
            @PathVariable("id") UUID id,
            @RequestParam(value = "reason", required = false) String reason) {
        OrderDetailResponse response = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Helper method to get current user ID from SecurityContext
     */
    private UUID getCurrentUserId() {
        // This would be implemented based on your security context
        // For now, returning a placeholder
        return UUID.randomUUID();
    }
}

