package fpt.tuanhm43.server.dtos.inventory;

import java.util.UUID;

/**
 * Reservation Item DTO
 */
public record ReservationItem(UUID variantId, Integer quantity) {
    // No additional methods needed for this simple DTO
}