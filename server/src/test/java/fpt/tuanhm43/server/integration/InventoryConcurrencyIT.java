package fpt.tuanhm43.server.integration;

import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.repositories.InventoryRepository;
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
import java.util.concurrent.*;
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

    private UUID variantId;

    @BeforeEach
    void setup() {
        Product product = productRepository.save(Product.builder()
                .name("Giày Nike Air Jordan Test")
                .slug("nike-aj1-test-" + UUID.randomUUID()) // Tránh trùng slug
                .basePrice(new BigDecimal("5000000"))
                .isActive(true)
                .build());

        // 2. Tạo Biến thể sản phẩm và GẮN VÀO sản phẩm ở trên
        ProductVariant variant = variantRepository.save(ProductVariant.builder()
                .product(product)
                .sku("LIMIT-EDITION-" + UUID.randomUUID().toString().toUpperCase())
                .priceAdjustment(BigDecimal.ZERO)
                .isActive(true)
                .build());


        variantId = variant.getId();

        // 3. Khởi tạo kho cho biến thể đó
        inventoryRepository.save(Inventory.builder()
                .productVariant(variant)
                .quantityAvailable(1) // Chỉ có 1 cái để test tranh chấp
                .quantityReserved(0)
                .build());
    }

    @Test
    @DisplayName("Test tranh chấp: 2 người cùng mua 1 món đồ - Chỉ 1 người thành công")
    void testRaceCondition() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1); // Súng lệnh xuất phát
        CountDownLatch endLatch = new CountDownLatch(threadCount); // Chờ cả 2 xong

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            String sessionId = "user-session-" + i;
            executor.execute(() -> {
                try {
                    startLatch.await();
                    inventoryService.reserveStock(sessionId,
                            List.of(new InventoryService.ReservationItem(variantId, 1)), 15);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failCount.incrementAndGet();
                    log.info("Khách hàng {} nhận thông báo: Hàng đã hết!", sessionId);
                } catch (Exception e) {
                    // Đề phòng các lỗi khác như Timeout hoặc Concurrency
                    log.error("Lỗi không mong muốn: {}", e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });

        }

        startLatch.countDown(); // BẮN SÚNG! 2 người cùng lao vào
        endLatch.await(); // Đợi 2 người hoàn thành

        // KIỂM TRA: Chỉ được phép có 1 người thành công, 1 người thất bại
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        // Kiểm tra kho cuối cùng: Available phải về 0, Reserved phải lên 1
        Inventory finalInv = inventoryRepository.findByProductVariantId(variantId).get();
        assertThat(finalInv.getQuantityAvailable()).isEqualTo(0);
        assertThat(finalInv.getQuantityReserved()).isEqualTo(1);
    }
}