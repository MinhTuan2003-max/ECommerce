package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.order.request.CreateOrderRequest;
import fpt.tuanhm43.server.dtos.order.request.OrderFilterRequest;
import fpt.tuanhm43.server.dtos.order.request.OrderItemRequest;
import fpt.tuanhm43.server.dtos.order.request.UpdateOrderStatusRequest;
import fpt.tuanhm43.server.dtos.order.response.OrderDetailResponse;
import fpt.tuanhm43.server.dtos.order.response.OrderResponse;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.enums.PaymentStatus;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.*;
import fpt.tuanhm43.server.services.InventoryService;
import fpt.tuanhm43.server.services.MailService;
import fpt.tuanhm43.server.services.OrderService;
import fpt.tuanhm43.server.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String ORDER_NUMBER_FORMAT = "ORD-%s-%05d";
    private static final String RESOURCE_NAME = "Order";
    private static final int MAX_PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ShoppingCartRepository cartRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final MailService mailService;

    @Override
    @Transactional
    public OrderDetailResponse createOrderFromCart(String sessionId, CreateOrderRequest request) {
        log.info("Creating order from cart - Session: {}, Customer: {}", sessionId, request.getCustomerName());

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        ShoppingCart cart = cartRepository.findByUserId(currentUserId)
                .orElseGet(() -> cartRepository.findBySessionId(sessionId)
                        .orElseThrow(() -> new BadRequestException("Cart not found or expired")));

        if (!cart.hasItems()) {
            throw new BadRequestException("Cart is empty");
        }

        if (!cart.validateStock()) {
            throw new BadRequestException("Some items in cart are out of stock");
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .trackingToken(UUID.randomUUID())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .status(OrderStatus.PENDING)
                .currency("VND")
                .build();

        List<InventoryService.ReservationItem> reservationItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(cartItem.getProductVariant())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            order.addItem(orderItem);

            reservationItems.add(new InventoryService.ReservationItem(
                    cartItem.getProductVariant().getId(),
                    cartItem.getQuantity()
            ));
        }

        order.calculateTotals();
        Order savedOrder = orderRepository.save(order);

        try {
            inventoryService.reserveStock(sessionId, savedOrder.getId(), reservationItems, 15);
            log.info("Inventory reserved for order: {}", savedOrder.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order: {}", savedOrder.getId(), e);
            throw new BadRequestException("Failed to reserve items: " + e.getMessage());
        }

        savedOrder.addStatusHistory(OrderStatusHistory.builder()
                .order(savedOrder)
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING)
                .changedBy("SYSTEM")
                .reason("Order created from cart")
                .build());

        cartRepository.delete(cart);

        try {
            mailService.sendOrderConfirmation(savedOrder);
        } catch (Exception e) {
            log.warn("Failed to send confirmation email for order: {}", savedOrder.getId(), e);
        }

        return mapToOrderDetailResponse(savedOrder);
    }

    @Override
    @Transactional // Đảm bảo có Transactional để rollback nếu reserveStock thất bại
    public OrderDetailResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order directly - Customer: {}", request.getCustomerName());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must have at least one item");
        }

        // 1. Tạo đối tượng Order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .trackingToken(UUID.randomUUID())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .status(OrderStatus.PENDING)
                .currency("VND")
                .build();

        List<InventoryService.ReservationItem> reservationItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", itemRequest.getVariantId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(variant.getFinalPrice())
                    .subtotal(variant.getFinalPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                    .build();
            order.addItem(orderItem);

            reservationItems.add(new InventoryService.ReservationItem(
                    itemRequest.getVariantId(),
                    itemRequest.getQuantity()
            ));
        }

        order.calculateTotals();
        Order savedOrder = orderRepository.save(order);

        try {
            inventoryService.reserveStock(savedOrder.getId().toString(), savedOrder.getId(), reservationItems, 15);
            log.info("Inventory reserved for order: {}", savedOrder.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order: {}", savedOrder.getId(), e);
            throw new BadRequestException("Failed to reserve items: " + e.getMessage());
        }

        savedOrder.addStatusHistory(OrderStatusHistory.builder()
                .order(savedOrder)
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING)
                .changedBy("SYSTEM")
                .reason("Order created directly")
                .build());

        try {
            mailService.sendOrderConfirmation(savedOrder);
        } catch (Exception e) {
            log.warn("Failed to send confirmation email for order: {}", savedOrder.getId(), e);
        }

        return mapToOrderDetailResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(UUID id) {
        log.info("Getting order by id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", id));

        return mapToOrderDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByTrackingToken(UUID trackingToken) {
        log.info("Getting order by tracking token: {}", trackingToken);

        Order order = orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "trackingToken", trackingToken));

        return mapToOrderDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponse> getUserOrders(UUID userId, int page, int size) {
        log.info("Getting orders for user: {}, Page: {}, Size: {}", userId, page, size);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        int pageNumber = Math.max(page, 0);
        int pageSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return PageResponseDTO.from(orders.map(this::mapToOrderResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponse> getAllOrders(OrderFilterRequest filter) {
        log.info("Getting all orders with filter - Status: {}, PaymentStatus: {}, Page: {}, Size: {}",
                filter.getStatus(), filter.getPaymentStatus(), filter.getPage(), filter.getSize());

        int pageNumber = filter.getPage() != null ? filter.getPage() : 0;
        int pageSize = filter.getSize() != null
                ? Math.clamp(filter.getSize(), 1, MAX_PAGE_SIZE)
                : DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders = orderRepository.findAll(pageable);

        if (filter.getStatus() != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(filter.getStatus(), pageable);
        }

        if (filter.getPaymentStatus() != null) {
            orders = orderRepository.findByPaymentStatusOrderByCreatedAtDesc(filter.getPaymentStatus(), pageable);
        }

        return PageResponseDTO.from(orders.map(this::mapToOrderResponse));
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order status - Order: {}, NewStatus: {}", orderId, request.getNewStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", orderId));

        if (!order.canTransitionTo(request.getNewStatus())) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s", order.getStatus(), request.getNewStatus())
            );
        }

        order.updateStatus(request.getNewStatus(), request.getChangedBy(), request.getReason());
        Order updatedOrder = orderRepository.save(order);

        try {
            if (request.getNewStatus() == OrderStatus.SHIPPING) {
                mailService.sendOrderShipped(updatedOrder);
            } else if (request.getNewStatus() == OrderStatus.DELIVERED) {
                mailService.sendOrderDelivered(updatedOrder);
            }
        } catch (Exception e) {
            log.warn("Failed to send status email for order: {}", orderId, e);
        }

        log.info("Order status updated successfully: {}", orderId);
        return mapToOrderDetailResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelOrder(UUID orderId, String reason) {
        log.info("Cancelling order: {}, Reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", orderId));

        if (!order.canCancel()) {
            throw new BadRequestException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.updateStatus(OrderStatus.CANCELLED, "CUSTOMER", reason);
        Order cancelledOrder = orderRepository.save(order);

        try {
            inventoryService.releaseReservationByOrder(orderId);
        } catch (Exception e) {
            log.warn("Failed to release inventory for cancelled order: {}", orderId, e);
        }

        try {
            mailService.sendOrderCancelled(cancelledOrder, reason);
        } catch (Exception e) {
            log.warn("Failed to send cancellation email for order: {}", orderId, e);
        }

        log.info("Order cancelled successfully: {}", orderId);
        return mapToOrderDetailResponse(cancelledOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateOrderNumber() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        long countToday = orderRepository.findByDateRange(startOfDay, endOfDay, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
        int sequence = (int) ((countToday % 100000) + 1);

        return String.format(ORDER_NUMBER_FORMAT, dateStr, sequence);
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .trackingToken(order.getTrackingToken())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * Map Order entity to OrderDetailResponse DTO
     */
    private OrderDetailResponse mapToOrderDetailResponse(Order order) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .trackingToken(order.getTrackingToken())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .totalItems(order.getTotalItemsCount())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .currency(order.getCurrency())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

