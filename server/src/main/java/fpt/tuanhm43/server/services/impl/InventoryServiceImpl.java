package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.InventoryReservation;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.enums.ReservationStatus;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.InventoryRepository;
import fpt.tuanhm43.server.repositories.InventoryReservationRepository;
import fpt.tuanhm43.server.repositories.OrderRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private static final String INVENTORY_RESOURCE = "Inventory";
    private static final String VARIANT_ID_FIELD = "variantId";

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderRepository orderRepository;

    /**
     * Reserve stock for checkout (CRITICAL - uses pessimistic lock)
     *
     * @param sessionId Session ID for guest checkout
     * @param items Items to reserve
     * @param timeoutMinutes Reservation timeout (default 15)
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void reserveStock(String sessionId, List<ReservationItem> items, int timeoutMinutes) {
        log.info("Reserving stock for session: {}, items: {}, timeout: {} min",
                sessionId, items.size(), timeoutMinutes);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(timeoutMinutes);

        for (ReservationItem item : items) {
            Inventory inventory = inventoryRepository
                    .findByVariantIdWithLock(item.variantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            INVENTORY_RESOURCE, VARIANT_ID_FIELD, item.variantId()));

            // 2. Validate stock availability
            if (!inventory.canFulfill(item.quantity())) {
                log.warn("Insufficient stock - Variant: {}, Requested: {}, Available: {}",
                        item.variantId(), item.quantity(), inventory.getQuantityAvailable());

                throw new InsufficientStockException(
                        item.variantId(),
                        item.quantity(),
                        inventory.getQuantityAvailable()
                );
            }

            // 3. Reserve stock (update inventory)
            inventory.reserve(item.quantity());
            inventoryRepository.save(inventory);

            // 4. Create reservation record
            ProductVariant variant = variantRepository.findById(item.variantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ProductVariant", "id", item.variantId()));

            InventoryReservation reservation = InventoryReservation.builder()
                    .productVariant(variant)
                    .sessionId(sessionId)
                    .quantity(item.quantity())
                    .status(ReservationStatus.ACTIVE)
                    .expiresAt(expiresAt)
                    .build();

            reservationRepository.save(reservation);

            log.info("Reserved {} units of variant {} for session {}",
                    item.quantity(), item.variantId(), sessionId);
        }

        log.info("Stock reservation completed for session: {}", sessionId);
    }

    /**
     * Release reservation (timeout or cancel)
     */
    @Override
    @Transactional
    public void releaseReservation(String sessionId) {
        log.info("Releasing reservations for session: {}", sessionId);

        List<InventoryReservation> reservations = reservationRepository
                .findBySessionIdAndStatus(sessionId, ReservationStatus.ACTIVE);

        for (InventoryReservation reservation : reservations) {
            // Get inventory with lock
            Inventory inventory = inventoryRepository
                    .findByVariantIdWithLock(reservation.getProductVariant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            INVENTORY_RESOURCE, VARIANT_ID_FIELD, reservation.getProductVariant().getId()));

            // Release stock
            inventory.releaseReservation(reservation.getQuantity());
            inventoryRepository.save(inventory);

            // Mark reservation as cancelled
            reservation.markCancelled();
            reservationRepository.save(reservation);

            log.info("Released {} units of variant {}",
                    reservation.getQuantity(),
                    reservation.getProductVariant().getId());
        }

        log.info("All reservations released for session: {}", sessionId);
    }

    /**
     * Release reservation by order ID
     */
    @Override
    @Transactional
    public void releaseReservationByOrder(UUID orderId) {
        log.info("Releasing reservations for order: {}", orderId);

        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (InventoryReservation reservation : reservations) {
            if (reservation.canRelease()) {
                Inventory inventory = inventoryRepository
                        .findByVariantIdWithLock(reservation.getProductVariant().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                INVENTORY_RESOURCE, VARIANT_ID_FIELD, reservation.getProductVariant().getId()));

                inventory.releaseReservation(reservation.getQuantity());
                inventoryRepository.save(inventory);

                reservation.markCancelled();
                reservationRepository.save(reservation);
            }
        }

        log.info("Reservations released for order: {}", orderId);
    }

    /**
     * Deduct stock after payment confirmed
     */
    @Override
    @Transactional
    public void deductReservedStock(UUID orderId) {
        log.info("Deducting reserved stock for order: {}", orderId);

        // Verify order exists
        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                // Get inventory with lock
                Inventory inventory = inventoryRepository
                        .findByVariantIdWithLock(reservation.getProductVariant().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                INVENTORY_RESOURCE, VARIANT_ID_FIELD, reservation.getProductVariant().getId()));

                // Deduct reserved quantity
                inventory.deductReserved(reservation.getQuantity());
                inventoryRepository.save(inventory);

                // Mark reservation as completed
                reservation.markCompleted();
                reservationRepository.save(reservation);

                log.info("Deducted {} units of variant {} for order {}",
                        reservation.getQuantity(),
                        reservation.getProductVariant().getId(),
                        orderId);
            }
        }

        log.info("Stock deduction completed for order: {}", orderId);
    }

    /**
     * Add stock (restock)
     */
    @Override
    @Transactional
    public void addStock(UUID variantId, Integer quantity) {
        log.info("Adding stock - Variant: {}, Quantity: {}", variantId, quantity);

        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_RESOURCE, VARIANT_ID_FIELD, variantId));

        inventory.addStock(quantity);
        inventoryRepository.save(inventory);

        log.info("Stock added - Variant: {}, New quantity: {}",
                variantId, inventory.getQuantityAvailable());
    }

    /**
     * Check stock availability
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(UUID variantId, Integer quantity) {
        return inventoryRepository.hasSufficientStock(variantId, quantity);
    }

    /**
     * Get available stock
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableStock(UUID variantId) {
        return inventoryRepository.getAvailableQuantity(variantId)
                .orElse(0);
    }

    /**
     * Cleanup expired reservations (scheduled job)
     *
     * @return Number of cleaned up reservations
     */
    @Override
    @Transactional
    public int cleanupExpiredReservations() {
        log.info("Starting cleanup of expired reservations...");

        LocalDateTime now = LocalDateTime.now();
        List<InventoryReservation> expiredReservations =
                reservationRepository.findExpiredReservations(now);

        int cleanedCount = 0;

        for (InventoryReservation reservation : expiredReservations) {
            try {
                // Get inventory with lock
                Inventory inventory = inventoryRepository
                        .findByVariantIdWithLock(reservation.getProductVariant().getId())
                        .orElse(null);

                if (inventory != null) {
                    // Release stock
                    inventory.releaseReservation(reservation.getQuantity());
                    inventoryRepository.save(inventory);

                    // Mark as expired
                    reservation.markExpired();
                    reservationRepository.save(reservation);

                    cleanedCount++;

                    log.debug("Cleaned up expired reservation: {} - Variant: {}, Quantity: {}",
                            reservation.getId(),
                            reservation.getProductVariant().getId(),
                            reservation.getQuantity());
                }
            } catch (Exception e) {
                log.error("Error cleaning up reservation {}: {}",
                        reservation.getId(), e.getMessage(), e);
            }
        }

        log.info("Cleanup completed - {} expired reservation(s) cleaned", cleanedCount);
        return cleanedCount;
    }
}