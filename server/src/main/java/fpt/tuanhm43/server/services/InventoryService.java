package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.inventory.ReservationItem;

import java.util.List;
import java.util.UUID;

/**
 * Inventory Service Interface
 * CRITICAL: Handles stock management with concurrency control
 */
public interface InventoryService {

    /**
     * Reserve stock for checkout (CRITICAL - with pessimistic lock)
     */
    void reserveStock(String sessionId, UUID orderId, List<ReservationItem> items, int timeoutMinutes);

    /**
     * Release reservation (timeout or cancel)
     */
    void releaseReservation(String sessionId);

    /**
     * Release reservation by order ID
     */
    void releaseReservationByOrder(UUID orderId);

    /**
     * Deduct stock after payment confirmed
     */
    void deductReservedStock(UUID orderId);

    /**
     * Add stock (restock)
     */
    void addStock(UUID variantId, Integer quantity);

    /**
     * Check stock availability
     */
    boolean checkStockAvailability(UUID variantId, Integer quantity);

    /**
     * Get available stock
     */
    Integer getAvailableStock(UUID variantId);

    /**
     * Cleanup expired reservations (scheduled job)
     */
    int cleanupExpiredReservations();

}
