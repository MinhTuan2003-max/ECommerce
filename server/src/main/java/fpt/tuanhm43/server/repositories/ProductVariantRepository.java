package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Product Variant Repository
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Find by product
     */
    List<ProductVariant> findByProductId(UUID productId);

}