package fpt.tuanhm43.server.schedulers;

import fpt.tuanhm43.server.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCleanupScheduler {

    private final InventoryService inventoryService;

    /**
     * Periodically cleans up expired inventory reservations.
     * Runs every 1 minute to ensure high availability of stock.
     * fixedRate = 60000ms (1 minute)
     */
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    public void cleanupExpiredReservations() {
        log.debug("InventoryCleanupScheduler: Starting scheduled task to release expired stock...");

        try {
            int cleanedCount = inventoryService.cleanupExpiredReservations();

            if (cleanedCount > 0) {
                log.info("InventoryCleanupScheduler: Successfully released stock for {} expired reservations.", cleanedCount);
            }
        } catch (Exception e) {
            log.error("InventoryCleanupScheduler: Critical error during stock cleanup: {}", e.getMessage(), e);
        }
    }
}