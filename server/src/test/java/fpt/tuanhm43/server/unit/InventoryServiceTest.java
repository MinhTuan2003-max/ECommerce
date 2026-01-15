package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.InventoryReservation;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.repositories.InventoryRepository;
import fpt.tuanhm43.server.repositories.InventoryReservationRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryReservationRepository reservationRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID variantId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        variantId = UUID.randomUUID();
        inventory = Inventory.builder()
                .quantityAvailable(10)
                .quantityReserved(0)
                .build();
    }

    @Test
    @DisplayName("Nên giữ chỗ kho thành công khi đủ số lượng")
    void reserveStock_Success() {
        // Given
        List<InventoryService.ReservationItem> items = List.of(
                new InventoryService.ReservationItem(variantId, 2)
        );
        ProductVariant variant = ProductVariant.builder().id(variantId).build();

        when(inventoryRepository.findByVariantIdWithLock(variantId)).thenReturn(Optional.of(inventory));
        when(variantRepository.findById(variantId)).thenReturn(Optional.of(variant));

        // When
        inventoryService.reserveStock("session-123", items, 15);

        // Then
        assertThat(inventory.getQuantityAvailable()).isEqualTo(8); // 10 - 2
        assertThat(inventory.getQuantityReserved()).isEqualTo(2); // 0 + 2
        verify(reservationRepository, times(1)).save(any(InventoryReservation.class));
    }

    @Test
    @DisplayName("Nên ném lỗi InsufficientStockException khi kho không đủ")
    void reserveStock_InsufficientStock() {
        // Given
        List<InventoryService.ReservationItem> items = List.of(
                new InventoryService.ReservationItem(variantId, 15) // Yêu cầu 15 nhưng chỉ có 10
        );
        when(inventoryRepository.findByVariantIdWithLock(variantId)).thenReturn(Optional.of(inventory));

        // When & Then
        assertThatThrownBy(() -> inventoryService.reserveStock("session-123", items, 15))
                .isInstanceOf(InsufficientStockException.class);
    }
}
