package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "Endpoints for stock tracking, restocking, and managing inventory reservations for product variants.")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{variantId}/check")
    @Operation(summary = "Check stock availability", description = "Verify if a specific product variant has enough units in stock for the requested quantity.")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkStockAvailability(
            @Parameter(description = "UUID of the product variant") @PathVariable("variantId") UUID variantId,
            @Parameter(description = "Quantity to check", example = "2") @RequestParam("quantity") Integer quantity) {
        log.info("Checking stock availability for variant: {}, quantity: {}", variantId, quantity);
        boolean available = inventoryService.checkStockAvailability(variantId, quantity);
        return ResponseEntity.ok(ApiResponseDTO.success(available));
    }

    @GetMapping("/{variantId}/available")
    @Operation(summary = "Get current stock level", description = "Retrieve the total available units for a specific product variant.")
    public ResponseEntity<ApiResponseDTO<Integer>> getAvailableStock(
            @Parameter(description = "UUID of the product variant") @PathVariable("variantId") UUID variantId) {
        log.info("Fetching available stock for variant: {}", variantId);
        Integer available = inventoryService.getAvailableStock(variantId);
        return ResponseEntity.ok(ApiResponseDTO.success(available));
    }

    @PostMapping("/{variantId}/add")
    @Operation(
            summary = "Add stock (Restock)",
            description = "Increase the stock count for a variant. Used for restocking. Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> addStock(
            @PathVariable("variantId") UUID variantId,
            @Parameter(description = "Quantity to add", example = "50") @RequestParam("quantity") Integer quantity) {
        log.info("Adding stock for variant: {}, quantity: {}", variantId, quantity);
        inventoryService.addStock(variantId, quantity);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Stock added successfully"));
    }

    @PostMapping("/release/order/{orderId}")
    @Operation(
            summary = "Release reservation by Order",
            description = "Unlocks stock reserved for an order when it is cancelled or the payment period expires. Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> releaseReservationByOrder(
            @Parameter(description = "UUID of the cancelled order") @PathVariable("orderId") UUID orderId) {
        log.info("Releasing inventory reservation for order: {}", orderId);
        inventoryService.releaseReservationByOrder(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Inventory reservation released successfully"));
    }

    @PostMapping("/release/session/{sessionId}")
    @Operation(
            summary = "Release reservation by Session",
            description = "Frees up stock reserved by a specific user session (e.g., when a cart is cleared). Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> releaseReservation(
            @Parameter(description = "ID of the web session") @PathVariable("sessionId") String sessionId) {
        log.info("Releasing inventory reservation for session: {}", sessionId);
        inventoryService.releaseReservation(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Session inventory reservation released successfully"));
    }

    @PostMapping("/cleanup/expired")
    @Operation(
            summary = "Cleanup expired reservations",
            description = "Maintenance task to automatically release stock from reservations that have timed out. Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Integer>> cleanupExpiredReservations() {
        log.info("Cleaning up expired inventory reservations");
        int cleaned = inventoryService.cleanupExpiredReservations();
        return ResponseEntity.ok(ApiResponseDTO.success(cleaned, "Cleaned up " + cleaned + " expired reservations"));
    }
}