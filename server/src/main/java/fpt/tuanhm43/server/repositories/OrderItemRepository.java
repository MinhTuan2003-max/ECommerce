package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Order Item Repository
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * Find by order
     */
    List<OrderItem> findByOrderId(UUID orderId);

    /**
     * Find by variant (for sales reporting)
     */
    List<OrderItem> findByProductVariantId(UUID variantId);

    /**
     * Get total quantity sold for variant
     */
    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0) 
        FROM OrderItem oi 
        WHERE oi.productVariant.id = :variantId
    """)
    Long getTotalQuantitySoldByVariant(@Param("variantId") UUID variantId);

    /**
     * Get best selling variants
     */
    @Query("""
        SELECT oi.productVariant.id, SUM(oi.quantity) as total
        FROM OrderItem oi 
        GROUP BY oi.productVariant.id 
        ORDER BY total DESC
    """)
    List<Object[]> getBestSellingVariants(org.springframework.data.domain.Pageable pageable);
}

