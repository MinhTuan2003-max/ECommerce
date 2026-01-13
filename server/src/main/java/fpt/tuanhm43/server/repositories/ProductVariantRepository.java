package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product Variant Repository
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    /**
     * Find by SKU
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Find by product
     */
    List<ProductVariant> findByProductId(UUID productId);

    /**
     * Find active variants by product
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true")
    List<ProductVariant> findActiveByProductId(@Param("productId") UUID productId);

    /**
     * Find variant with inventory
     */
    @Query("SELECT v FROM ProductVariant v LEFT JOIN FETCH v.inventory WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithInventory(@Param("id") UUID id);

    /**
     * Find in-stock variants by product
     */
    @Query("""
        SELECT v FROM ProductVariant v
        LEFT JOIN v.inventory i
        WHERE v.product.id = :productId 
        AND v.isActive = true 
        AND i.quantityAvailable > 0
    """)
    List<ProductVariant> findInStockByProductId(@Param("productId") UUID productId);

    /**
     * Find by size and color
     */
    @Query("""
        SELECT v FROM ProductVariant v 
        WHERE v.product.id = :productId 
        AND v.size = :size 
        AND v.color = :color 
        AND v.isActive = true
    """)
    Optional<ProductVariant> findByProductAndSizeAndColor(
            @Param("productId") UUID productId,
            @Param("size") String size,
            @Param("color") String color
    );

    /**
     * Count by product
     */
    long countByProductId(UUID productId);
}