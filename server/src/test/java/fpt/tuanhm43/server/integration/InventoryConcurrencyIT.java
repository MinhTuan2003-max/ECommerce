package fpt.tuanhm43.server.integration;

import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.repositories.InventoryRepository;
import fpt.tuanhm43.server.repositories.OrderRepository;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.services.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class InventoryConcurrencyIT {

    @Autowired private InventoryService inventoryService;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    private UUID variantId;
    private UUID orderId1;
    private UUID orderId2;

    @BeforeEach
    void setup() {
        Product product = productRepository.save(Product.builder()
                .name("Limited Sneaker")
                .slug("sneaker-" + UUID.randomUUID())
                .basePrice(new BigDecimal("5000000"))
                .isActive(true)
                .build());

        ProductVariant variant = variantRepository.save(ProductVariant.builder()
                .product(product)
                .sku("SKU-LIMIT-" + UUID.randomUUID().toString().toUpperCase().substring(0, 8))
                .priceAdjustment(BigDecimal.ZERO)
                .isActive(true)
                .build());

        variantId = variant.getId();

        inventoryRepository.save(Inventory.builder()
                .productVariant(variant)
                .quantityAvailable(1)
                .quantityReserved(0)
                .build());

        orderId1 = createDummyOrder("User 1").getId();
        orderId2 = createDummyOrder("User 2").getId();
    }

    private Order createDummyOrder(String customerName) {
        return orderRepository.save(Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .trackingToken(UUID.randomUUID())
                .customerName(customerName)
                .customerEmail(customerName.toLowerCase().replace(" ", "") + "@test.com")
                .customerPhone("0123456789")
                .shippingAddress("Test Address")
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.SEPAY)
                .totalAmount(new BigDecimal("5000000"))
                .currency("VND")
                .build());
    }

    @Test
    @DisplayName("Test race condition: 2 users buying last item - Only 1 succeeds")
    void testRaceCondition() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<UUID> orderIds = List.of(orderId1, orderId2);

        for (int i = 0; i < threadCount; i++) {
            String sessionId = "session-" + i;
            UUID orderId = orderIds.get(i);

            executor.execute(() -> {
                try {
                    startLatch.await();
                    inventoryService.reserveStock(sessionId, orderId,
                            List.of(new InventoryService.ReservationItem(variantId, 1)), 15);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Unexpected error: {}", e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        Inventory finalInv = inventoryRepository
                .findByProductVariantId(variantId)
                .orElseThrow(() -> new AssertionError("Inventory not found for variant " + variantId));

        assertThat(finalInv.getQuantityAvailable()).isZero();
        assertThat(finalInv.getQuantityReserved()).isEqualTo(1);
    }
}