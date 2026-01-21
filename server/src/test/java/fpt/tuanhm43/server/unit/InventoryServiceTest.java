package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.inventory.ReservationItem;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.repositories.InventoryRepository;
import fpt.tuanhm43.server.repositories.InventoryReservationRepository;
import fpt.tuanhm43.server.repositories.OrderRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.services.InventoryService;
import fpt.tuanhm43.server.services.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private InventoryReservationRepository reservationRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private InventoryServiceImpl inventoryService;

    private UUID variantId;
    private UUID orderId;
    private Inventory inventory;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        variantId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        inventory = Inventory.builder()
                .quantityAvailable(10)
                .quantityReserved(0)
                .productVariant(ProductVariant.builder().id(variantId).build())
                .build();

        mockOrder = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .build();
    }

    @Test
    @DisplayName("Reserve stock success when quantity is sufficient")
    void reserveStock_Success() {
        List<ReservationItem> items = List.of(
                new ReservationItem(variantId, 2)
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(inventoryRepository.findByVariantIdWithLock(variantId)).thenReturn(Optional.of(inventory));

        inventoryService.reserveStock("session-123", orderId, items, 15);

        assertThat(inventory.getQuantityAvailable()).isEqualTo(8);
        assertThat(inventory.getQuantityReserved()).isEqualTo(2);
        verify(reservationRepository, times(1)).save(any(InventoryReservation.class));
    }

    @Test
    @DisplayName("Throw InsufficientStockException when stock is not enough")
    void reserveStock_InsufficientStock() {
        List<ReservationItem> items = List.of(
                new ReservationItem(variantId, 15)
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(inventoryRepository.findByVariantIdWithLock(variantId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.reserveStock("session-123", orderId, items, 15))
                .isInstanceOf(InsufficientStockException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throw ResourceNotFoundException when order does not exist")
    void reserveStock_OrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.reserveStock("session-123", orderId, List.of(), 15))
                .isInstanceOf(fpt.tuanhm43.server.exceptions.ResourceNotFoundException.class);
    }
}