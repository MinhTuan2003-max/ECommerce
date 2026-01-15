package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.InventoryReservation;
import fpt.tuanhm43.server.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
     * Find expired active reservations (for cleanup job)
     */
    @Query("""
        SELECT r FROM InventoryReservation r\s
        WHERE r.status = fpt.tuanhm43.server.enums.ReservationStatus.ACTIVE\s
        AND r.expiresAt < :now
   \s""")
    List<InventoryReservation> findExpiredReservations(@Param("now") LocalDateTime now);

}