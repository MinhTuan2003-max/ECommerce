package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.order.request.CreateOrderRequest;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.repositories.OrderRepository;
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
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ShoppingCartRepository cartRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Nên gọi reserveStock khi tạo đơn hàng từ giỏ hàng")
    void createOrderFromCart_ShouldReserveStock() {

        Inventory mockInventory = Inventory.builder()
                .quantityAvailable(100)
                .quantityReserved(0)
                .build();

        UUID variantId = UUID.randomUUID();
        ProductVariant variant = ProductVariant.builder()
                .id(variantId)
                .inventory(mockInventory)
                .build();

        mockInventory.setProductVariant(variant);

        String sessionId = "test-session";
        BigDecimal price = new BigDecimal("5000000");
        int qty = 1;

        CartItem item = CartItem.builder()
                .productVariant(variant)
                .quantity(1)
                .unitPrice(new BigDecimal("5000000"))
                .subtotal(price.multiply(BigDecimal.valueOf(qty)))
                .build();

        ShoppingCart cart = ShoppingCart.builder()
                .sessionId(sessionId)
                .items(List.of(item))
                .build();

        when(orderRepository.findByDateRange(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(cart));

        orderService.createOrderFromCart(sessionId, new CreateOrderRequest());

        verify(inventoryService).reserveStock(eq(sessionId), anyList(), eq(15));
    }
}
