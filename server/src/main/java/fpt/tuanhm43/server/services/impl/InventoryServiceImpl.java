package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.inventory.ReservationItem;
import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.InventoryReservation;
import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.enums.ReservationStatus;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.InventoryRepository;
import fpt.tuanhm43.server.repositories.InventoryReservationRepository;
import fpt.tuanhm43.server.repositories.OrderRepository;
import fpt.tuanhm43.server.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final OrderRepository orderRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void reserveStock(String sessionId, UUID orderId, List<ReservationItem> items, int timeoutMinutes) {
        log.debug("Reserving stock for session: {}, order: {}, items: {}, timeout: {} min",
                sessionId, orderId, items.size(), timeoutMinutes);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(timeoutMinutes);

        List<ReservationItem> sortedItems = new ArrayList<>(items);
        sortedItems.sort(Comparator.comparing(ReservationItem::variantId));

        for (ReservationItem item : sortedItems) {
            Inventory inventory = inventoryRepository
                    .findByVariantIdWithLock(item.variantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            INVENTORY_RESOURCE, VARIANT_ID_FIELD, item.variantId()));

            if (!inventory.canFulfill(item.quantity())) {
                throw new InsufficientStockException(
                        item.variantId(), item.quantity(), inventory.getQuantityAvailable());
            }

            inventory.reserve(item.quantity());
            inventoryRepository.save(inventory);

            InventoryReservation reservation = InventoryReservation.builder()
                    .productVariant(inventory.getProductVariant())
                    .order(order)
                    .sessionId(sessionId)
                    .quantity(item.quantity())
                    .status(ReservationStatus.ACTIVE)
                    .expiresAt(expiresAt)
                    .build();

            reservationRepository.save(reservation);

            log.debug("Reserved {} units of variant {} for order {}",
                    item.quantity(), item.variantId(), order.getOrderNumber());
        }
    }

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

    @Override
    @Transactional
    public void deductReservedStock(UUID orderId) {
        log.info("Deducting reserved stock for order: {}", orderId);

        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                Inventory inventory = inventoryRepository
                        .findByVariantIdWithLock(reservation.getProductVariant().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                INVENTORY_RESOURCE, VARIANT_ID_FIELD, reservation.getProductVariant().getId()));

                int quantityToDeduct = reservation.getQuantity();

                if (inventory.getQuantityReserved() < quantityToDeduct) {
                    throw new IllegalStateException(
                            String.format("Insufficient reserved stock for variant %s. Reserved: %d, Needed: %d",
                                    reservation.getProductVariant().getId(),
                                    inventory.getQuantityReserved(),
                                    quantityToDeduct));
                }

                inventory.deductReserved(quantityToDeduct);
                inventoryRepository.save(inventory);

                reservation.markCompleted();
                reservationRepository.save(reservation);

                log.info("Deducted {} units of variant {} for order {} | Available: {}, Reserved: {}",
                        quantityToDeduct,
                        reservation.getProductVariant().getId(),
                        orderId,
                        inventory.getQuantityAvailable(),
                        inventory.getQuantityReserved());
            }
        }

        log.info("Stock deduction completed for order: {}", orderId);
    }

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

    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(UUID variantId, Integer quantity) {
        return inventoryRepository.hasSufficientStock(variantId, quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableStock(UUID variantId) {
        return inventoryRepository.getAvailableQuantity(variantId)
                .orElse(0);
    }

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