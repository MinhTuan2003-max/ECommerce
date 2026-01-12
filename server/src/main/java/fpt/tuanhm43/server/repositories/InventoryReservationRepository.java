package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.InventoryReservation;
import fpt.tuanhm43.server.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
    List<InventoryReservation> findByOrderId(UUID orderId);
    List<InventoryReservation> findByStatusAndProductVariantId(ReservationStatus status, UUID productVariantId);
}

