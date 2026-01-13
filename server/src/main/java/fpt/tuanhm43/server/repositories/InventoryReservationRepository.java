package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.InventoryReservation;
import fpt.tuanhm43.server.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inventory Reservation Repository
 */
@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    /**
     * Find by session ID (guest checkout)
     */
    List<InventoryReservation> findBySessionIdAndStatus(String sessionId, ReservationStatus status);

    /**
     * Find by order
     */
    List<InventoryReservation> findByOrderId(UUID orderId);

    /**
     * Find active reservations by variant
     */
    @Query("""
        SELECT r FROM InventoryReservation r WHERE r.productVariant.id = :variantId AND r.status = 'ACTIVE'
    """)
    List<InventoryReservation> findActiveByVariantId(@Param("variantId") UUID variantId);

    /**
     * Find expired active reservations (for cleanup job)
     */
    @Query("""
        SELECT r FROM InventoryReservation r 
        WHERE r.status = 'ACTIVE' 
        AND r.expiresAt < :now
    """)
    List<InventoryReservation> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Find reservations expiring soon (warning)
     */
    @Query("""
        SELECT r FROM InventoryReservation r 
        WHERE r.status = 'ACTIVE' 
        AND r.expiresAt BETWEEN :now AND :threshold
    """)
    List<InventoryReservation> findExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold
    );

    /**
     * Mark expired reservations
     */
    @Modifying
    @Query("""
        UPDATE InventoryReservation r 
        SET r.status = 'EXPIRED' 
        WHERE r.status = 'ACTIVE' 
        AND r.expiresAt < :now
    """)
    int markExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Delete old reservations (cleanup)
     */
    @Modifying
    @Query("""
        DELETE FROM InventoryReservation r 
        WHERE r.status IN ('EXPIRED', 'CANCELLED', 'COMPLETED') 
        AND r.createdAt < :threshold
    """)
    int deleteOldReservations(@Param("threshold") LocalDateTime threshold);

    /**
     * Get total reserved quantity for variant
     */
    @Query("""
        SELECT COALESCE(SUM(r.quantity), 0) 
        FROM InventoryReservation r 
        WHERE r.productVariant.id = :variantId 
        AND r.status = 'ACTIVE'
    """)
    Integer getTotalReservedQuantity(@Param("variantId") UUID variantId);

    /**
     * Check if session has active reservation
     */
    boolean existsBySessionIdAndStatus(String sessionId, ReservationStatus status);
}