package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.order.request.CreateOrderRequest;
import fpt.tuanhm43.server.dtos.order.request.OrderItemRequest;
import fpt.tuanhm43.server.dtos.order.response.OrderDetailResponse;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.mappers.OrderMapper;
import fpt.tuanhm43.server.repositories.OrderRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.repositories.ShoppingCartRepository;
import fpt.tuanhm43.server.services.InventoryService;
import fpt.tuanhm43.server.services.MailService;
import fpt.tuanhm43.server.services.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private ShoppingCartRepository cartRepository;
    @Mock private InventoryService inventoryService;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private MailService mailService;

    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderServiceImpl orderService;

    @Test
    @DisplayName("Nên gọi reserveStock với OrderId khi tạo đơn hàng từ giỏ hàng")
    void createOrderFromCart_ShouldReserveStockWithOrderId() {
        String sessionId = "test-session";
        UUID variantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Inventory mockInventory = Inventory.builder().quantityAvailable(100).quantityReserved(0).build();
        ProductVariant variant = ProductVariant.builder().id(variantId).inventory(mockInventory).build();
        mockInventory.setProductVariant(variant);

        CartItem item = CartItem.builder()
                .productVariant(variant)
                .quantity(1)
                .unitPrice(new BigDecimal("5000000"))
                .subtotal(new BigDecimal("5000000"))
                .build();

        ShoppingCart cart = ShoppingCart.builder().sessionId(sessionId).items(List.of(item)).build();

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(cart));

        when(orderRepository.countByCreatedAtAfter(any())).thenReturn(0L);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(orderId);
            return o;
        });

        when(orderMapper.toDetailResponse(any())).thenReturn(OrderDetailResponse.builder().build());

        orderService.createOrderFromCart(sessionId, new CreateOrderRequest());

        verify(inventoryService).reserveStock(eq(sessionId), eq(orderId), anyList(), eq(15));
    }

    @Test
    @DisplayName("Nên gọi reserveStock với OrderId khi tạo đơn hàng trực tiếp")
    void createOrder_ShouldReserveStockWithOrderId() {
        UUID variantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Inventory mockInventory = Inventory.builder().quantityAvailable(100).build();
        ProductVariant variant = ProductVariant.builder().id(variantId).inventory(mockInventory).sku("SKU-01").build();
        mockInventory.setProductVariant(variant);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(new OrderItemRequest(variantId, 1)));

        when(variantRepository.findById(variantId)).thenReturn(Optional.of(variant));

        when(orderRepository.countByCreatedAtAfter(any())).thenReturn(0L);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(orderId);
            return o;
        });

        when(orderMapper.toDetailResponse(any())).thenReturn(OrderDetailResponse.builder().build());

        orderService.createOrder(request);

        verify(inventoryService).reserveStock(anyString(), eq(orderId), anyList(), eq(15));
    }

    @Test
    @DisplayName("Track đơn hàng: Thành công khi mã Token hợp lệ")
    void getOrderByTrackingToken_Success() {
        UUID secureToken = UUID.randomUUID();
        Order mockOrder = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-001")
                .trackingToken(secureToken)
                .status(OrderStatus.CONFIRMED)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findByTrackingToken(secureToken)).thenReturn(Optional.of(mockOrder));

        OrderDetailResponse dummyResponse = OrderDetailResponse.builder()
                .trackingToken(secureToken)
                .status(OrderStatus.CONFIRMED)
                .build();
        when(orderMapper.toDetailResponse(any(Order.class))).thenReturn(dummyResponse);

        OrderDetailResponse result = orderService.getOrderByTrackingToken(secureToken);

        assertThat(result.getTrackingToken()).isEqualTo(secureToken);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Track đơn hàng: Ném lỗi khi mã Token sai")
    void getOrderByTrackingToken_NotFound() {
        UUID invalidToken = UUID.randomUUID();
        when(orderRepository.findByTrackingToken(invalidToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByTrackingToken(invalidToken))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}