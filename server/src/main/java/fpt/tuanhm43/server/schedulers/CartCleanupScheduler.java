package fpt.tuanhm43.server.schedulers;

import fpt.tuanhm43.server.repositories.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartCleanupScheduler {

    private final ShoppingCartRepository cartRepository;

    /**
     * Clean up expired carts
     * Runs every hour at minute 0
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredCarts() {
        log.info("Starting cart cleanup job...");

        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = cartRepository.deleteExpiredCarts(now);

            if (deletedCount > 0) {
                log.info("Cart cleanup completed - Deleted {} expired cart(s)", deletedCount);
            } else {
                log.debug("Cart cleanup completed - No expired carts found");
            }
        } catch (Exception e) {
            log.error("Error during cart cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Log cart statistics
     * Runs every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void logCartStatistics() {
        try {
            long totalCarts = cartRepository.count();
            log.info("Cart statistics - Total active carts: {}", totalCarts);
        } catch (Exception e) {
            log.error("Error logging cart statistics: {}", e.getMessage(), e);
        }
    }
}