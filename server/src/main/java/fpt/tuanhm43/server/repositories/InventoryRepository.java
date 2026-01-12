package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProductVariantId(UUID productVariantId);
}

