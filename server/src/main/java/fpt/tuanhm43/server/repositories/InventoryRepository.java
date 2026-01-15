package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Inventory Repository
 * CRITICAL: Handles stock management with pessimistic locking
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    /**
     * Find by variant ID
     */
    Optional<Inventory> findByProductVariantId(UUID variantId);

    /**
     * Find by variant ID WITH PESSIMISTIC LOCK
     * Use this for reservation to prevent race conditions
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productVariant.id = :variantId")
    Optional<Inventory> findByVariantIdWithLock(@Param("variantId") UUID variantId);

    /**
     * Check if variant has sufficient stock
     */
    @Query("""
        SELECT CASE WHEN i.quantityAvailable >= :quantity THEN true ELSE false END
        FROM Inventory i\s
        WHERE i.productVariant.id = :variantId
   \s""")
    Boolean hasSufficientStock(@Param("variantId") UUID variantId, @Param("quantity") Integer quantity);

    /**
     * Get available quantity
     */
    @Query("SELECT i.quantityAvailable FROM Inventory i WHERE i.productVariant.id = :variantId")
    Optional<Integer> getAvailableQuantity(@Param("variantId") UUID variantId);

}