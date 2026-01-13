package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.order.request.CreateOrderRequest;
import fpt.tuanhm43.server.dtos.order.request.OrderFilterRequest;
import fpt.tuanhm43.server.dtos.order.request.UpdateOrderStatusRequest;
import fpt.tuanhm43.server.dtos.order.response.OrderDetailResponse;
import fpt.tuanhm43.server.dtos.order.response.OrderResponse;

import java.util.UUID;

/**
 * Order Service Interface
 */
public interface OrderService {

    /**
     * Create order from cart (guest or user)
     */
    OrderDetailResponse createOrderFromCart(String sessionId, CreateOrderRequest request);

    /**
     * Create order directly (without cart)
     */
    OrderDetailResponse createOrder(CreateOrderRequest request);

    /**
     * Get order by ID (Admin)
     */
    OrderDetailResponse getOrderById(UUID id);

    /**
     * Get order by tracking token (Guest)
     */
    OrderDetailResponse getOrderByTrackingToken(UUID trackingToken);

    /**
     * Get user orders
     */
    PageResponseDTO<OrderResponse> getUserOrders(UUID userId, int page, int size);

    /**
     * Get all orders with filter (Admin)
     */
    PageResponseDTO<OrderResponse> getAllOrders(OrderFilterRequest filter);

    /**
     * Update order status (Admin)
     */
    OrderDetailResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request);

    /**
     * Cancel order
     */
    OrderDetailResponse cancelOrder(UUID orderId, String reason);

    /**
     * Generate order number
     */
    String generateOrderNumber();
}
