package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Inventory Controller
 * Handles stock management and inventory operations
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Check stock availability for a product variant
     * Public endpoint - no authentication required
     */
    @GetMapping("/{variantId}/check")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkStockAvailability(
            @PathVariable("variantId") UUID variantId,
            @RequestParam("quantity") Integer quantity) {
        log.info("Checking stock availability for variant: {}, quantity: {}", variantId, quantity);
        boolean available = inventoryService.checkStockAvailability(variantId, quantity);
        return ResponseEntity.ok(ApiResponseDTO.success(available));
    }

    /**
     * Get available stock for a product variant
     * Public endpoint
     */
    @GetMapping("/{variantId}/available")
    public ResponseEntity<ApiResponseDTO<Integer>> getAvailableStock(
            @PathVariable("variantId") UUID variantId) {
        log.info("Fetching available stock for variant: {}", variantId);
        Integer available = inventoryService.getAvailableStock(variantId);
        return ResponseEntity.ok(ApiResponseDTO.success(available));
    }

    /**
     * Add stock (restock) for a product variant
     * Admin only
     */
    @PostMapping("/{variantId}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> addStock(
            @PathVariable("variantId") UUID variantId,
            @RequestParam("quantity") Integer quantity) {
        log.info("Adding stock for variant: {}, quantity: {}", variantId, quantity);
        inventoryService.addStock(variantId, quantity);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Stock added successfully"));
    }

    /**
     * Release inventory reservation by order ID
     * Called when order is cancelled or payment fails
     * Admin only
     */
    @PostMapping("/release/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> releaseReservationByOrder(
            @PathVariable("orderId") UUID orderId) {
        log.info("Releasing inventory reservation for order: {}", orderId);
        inventoryService.releaseReservationByOrder(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Inventory reservation released successfully"));
    }

    /**
     * Release inventory reservation by session ID
     * Called when cart is cleared or session expires
     * Internal use only
     */
    @PostMapping("/release/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> releaseReservation(
            @PathVariable("sessionId") String sessionId) {
        log.info("Releasing inventory reservation for session: {}", sessionId);
        inventoryService.releaseReservation(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Session inventory reservation released successfully"));
    }

    /**
     * Cleanup expired reservations
     * Should be called periodically by scheduler
     * Admin only
     */
    @PostMapping("/cleanup/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Integer>> cleanupExpiredReservations() {
        log.info("Cleaning up expired inventory reservations");
        int cleaned = inventoryService.cleanupExpiredReservations();
        return ResponseEntity.ok(ApiResponseDTO.success(cleaned, "Cleaned up " + cleaned + " expired reservations"));
    }
}

