package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.cart.request.AddToCartRequest;
import fpt.tuanhm43.server.dtos.cart.request.UpdateCartItemRequest;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.repositories.*;
import fpt.tuanhm43.server.services.impl.CartServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CartServiceTest {

    @Mock private ShoppingCartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks private CartServiceImpl cartService;

    private String sessionId;
    private UUID variantId;
    private ProductVariant mockVariant;
    private Inventory mockInventory;
    private ShoppingCart mockCart;

    @BeforeEach
    void setUp() {
        sessionId = "session-123";
        variantId = UUID.randomUUID();

        mockVariant = ProductVariant.builder()
                .id(variantId)
                .sku("TSHIRT-RONG-L")
                .product(Product.builder().name("Áo Thun Rồng").basePrice(new BigDecimal("500000")).build())
                .priceAdjustment(BigDecimal.ZERO)
                .build();

        mockInventory = Inventory.builder()
                .productVariant(mockVariant)
                .quantityAvailable(5)
                .build();

        mockCart = ShoppingCart.builder()
                .id(UUID.randomUUID())
                .sessionId(sessionId)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();
    }

    @Test
    @DisplayName("Thêm vào giỏ: Thành công khi kho còn đủ hàng")
    void addToCart_Success() {

        AddToCartRequest request = new AddToCartRequest(variantId, 2);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(mockCart));
        when(variantRepository.findById(variantId)).thenReturn(Optional.of(mockVariant));
        when(inventoryRepository.findByProductVariantId(variantId)).thenReturn(Optional.of(mockInventory));

        CartResponse response = cartService.addToCart(sessionId, request);

        assertThat(response.getTotalItems()).isEqualTo(2);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        log.info("Test Passed: Đã thêm 2 Áo Thun Rồng vào giỏ thành công.");
    }

    @Test
    @DisplayName("Thêm vào giỏ: Thất bại khi thêm quá số lượng tồn kho")
    void addToCart_InsufficientStock() {
        AddToCartRequest request = new AddToCartRequest(variantId, 10);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(mockCart));
        when(variantRepository.findById(variantId)).thenReturn(Optional.of(mockVariant));
        when(inventoryRepository.findByProductVariantId(variantId)).thenReturn(Optional.of(mockInventory));

        assertThatThrownBy(() -> cartService.addToCart(sessionId, request))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartItemRepository, never()).save(any());
        log.info("Test Passed: Hệ thống đã chặn thành công việc mua 10 cái khi kho chỉ còn 5.");
    }

    @Test
    @DisplayName("Cập nhật giỏ: Tăng số lượng và check kho lần nữa")
    void updateCartItem_Success() {
        BigDecimal unitPrice = new BigDecimal("500000");
        int quantity = 2;

        CartItem existingItem = CartItem.builder()
                .productVariant(mockVariant)
                .quantity(2)
                .unitPrice(new BigDecimal("500000"))
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
        mockCart.addItem(existingItem);

        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest();
        updateRequest.setQuantity(4);

        when(cartRepository.findBySessionIdWithItems(sessionId)).thenReturn(Optional.of(mockCart));
        when(inventoryRepository.findByProductVariantId(variantId)).thenReturn(Optional.of(mockInventory));

        cartService.update(sessionId, variantId, updateRequest);

        assertThat(existingItem.getQuantity()).isEqualTo(4);
        verify(cartRepository).save(mockCart);
    }
}