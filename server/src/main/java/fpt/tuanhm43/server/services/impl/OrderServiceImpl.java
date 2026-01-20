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
import fpt.tuanhm43.server.mappers.OrderMapper;
import fpt.tuanhm43.server.repositories.*;
import fpt.tuanhm43.server.services.InventoryService;
import fpt.tuanhm43.server.services.MailService;
import fpt.tuanhm43.server.services.OrderService;
import fpt.tuanhm43.server.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NUMBER_FORMAT = "ORD-%s-%05d";
    private static final String RESOURCE_NAME = "Order";
    private static final int MAX_PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ShoppingCartRepository cartRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final MailService mailService;

    // Inject Mapper
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    @Retryable(
            retryFor = { ObjectOptimisticLockingFailureException.class, ConcurrencyFailureException.class },
            maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public OrderDetailResponse createOrderFromCart(String sessionId, CreateOrderRequest request) {
        log.info("Creating order from cart - Session: {}", sessionId);

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ShoppingCart cart;

        if (currentUserId != null) {
            cart = cartRepository.findByUserId(currentUserId)
                    .orElseGet(() -> cartRepository.findBySessionId(sessionId)
                            .orElseThrow(() -> new BadRequestException("Cart not found or expired")));
        } else {
            cart = cartRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new BadRequestException("Cart not found or expired"));
        }

        if (!cart.hasItems()) throw new BadRequestException("Cart is empty");
        if (!cart.validateStock()) throw new BadRequestException("Some items are out of stock");

        Order order = buildBaseOrder(request);
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
            reservationItems.add(new InventoryService.ReservationItem(cartItem.getProductVariant().getId(), cartItem.getQuantity()));
        }

        order.calculateTotals();
        Order savedOrder = orderRepository.save(order);

        // Inventory Reservation
        inventoryService.reserveStock(sessionId, savedOrder.getId(), reservationItems, 15);

        savedOrder.addStatusHistory(OrderStatusHistory.builder()
                .order(savedOrder)
                .toStatus(OrderStatus.PENDING)
                .changedBy("SYSTEM")
                .reason("Order created from cart")
                .build());

        cartRepository.delete(cart);
        sendMailQuietly(savedOrder);

        return orderMapper.toDetailResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderDetailResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order directly - Customer: {}", request.getCustomerName());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must have items");
        }

        Order order = buildBaseOrder(request);
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
            reservationItems.add(new InventoryService.ReservationItem(itemRequest.getVariantId(), itemRequest.getQuantity()));
        }

        order.calculateTotals();
        Order savedOrder = orderRepository.save(order);

        inventoryService.reserveStock(savedOrder.getId().toString(), savedOrder.getId(), reservationItems, 15);

        savedOrder.addStatusHistory(OrderStatusHistory.builder()
                .order(savedOrder)
                .toStatus(OrderStatus.PENDING)
                .changedBy("SYSTEM")
                .reason("Order created directly")
                .build());

        sendMailQuietly(savedOrder);
        return orderMapper.toDetailResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(UUID id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByTrackingToken(UUID trackingToken) {
        return orderRepository.findByTrackingToken(trackingToken)
                .map(orderMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "trackingToken", trackingToken));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponse> getUserOrders(UUID userId, int page, int size) {
        if (!userRepository.existsById(userId)) throw new ResourceNotFoundException("User", "id", userId);

        Pageable pageable = PageRequest.of(page, Math.clamp(size, 1, MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return PageResponseDTO.from(orders.map(orderMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponse> getAllOrders(OrderFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? Math.clamp(filter.getSize(), 1, MAX_PAGE_SIZE) : 10;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders;

        if (filter.getStatus() != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(filter.getStatus(), pageable);
        } else if (filter.getPaymentStatus() != null) {
            orders = orderRepository.findByPaymentStatusOrderByCreatedAtDesc(filter.getPaymentStatus(), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return PageResponseDTO.from(orders.map(orderMapper::toResponse));
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", orderId));

        if (!order.canTransitionTo(request.getNewStatus())) {
            throw new BadRequestException("Invalid status transition");
        }

        order.updateStatus(request.getNewStatus(), request.getChangedBy(), request.getReason());
        Order updatedOrder = orderRepository.save(order);

        try {
            if (request.getNewStatus() == OrderStatus.SHIPPING)
                mailService.sendOrderShipped(updatedOrder);
            else if (request.getNewStatus() == OrderStatus.DELIVERED)
                mailService.sendOrderDelivered(updatedOrder);
        } catch (Exception e) {
            log.error("Mail error: {}", e.getMessage());
        }

        return orderMapper.toDetailResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", orderId));

        if (!order.canCancel()) throw new BadRequestException("Cannot cancel order");

        order.updateStatus(OrderStatus.CANCELLED, "CUSTOMER", reason);
        inventoryService.releaseReservationByOrder(orderId);

        Order cancelledOrder = orderRepository.save(order);
        try {
            mailService.sendOrderCancelled(cancelledOrder, reason);
        } catch (Exception ignored) {
            log.warn("Failed to send cancellation email for order {}", order.getOrderNumber());
        }

        return orderMapper.toDetailResponse(cancelledOrder);
    }

    @Override
    public String generateOrderNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long countToday = orderRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay());
        return String.format(ORDER_NUMBER_FORMAT, dateStr, countToday + 1);
    }

    private Order buildBaseOrder(CreateOrderRequest request) {
        return Order.builder()
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
    }

    private void sendMailQuietly(Order order) {
        try { mailService.sendOrderConfirmation(order); }
        catch (Exception e) { log.warn("Email failed for order {}", order.getOrderNumber()); }
    }

}