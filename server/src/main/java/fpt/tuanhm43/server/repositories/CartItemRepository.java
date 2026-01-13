package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cart Item Repository
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find by cart and variant
     */
    Optional<CartItem> findByCartIdAndProductVariantId(UUID cartId, UUID variantId);

    /**
     * Find all items in cart
     */
    List<CartItem> findByCartId(UUID cartId);

    /**
     * Delete by cart and variant
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productVariant.id = :variantId")
    int deleteByCartIdAndVariantId(@Param("cartId") UUID cartId, @Param("variantId") UUID variantId);

    /**
     * Delete all items in cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    int deleteByCartId(@Param("cartId") UUID cartId);

    /**
     * Count items in cart
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer countItemsByCartId(@Param("cartId") UUID cartId);

    /**
     * Check if variant exists in cart
     */
    boolean existsByCartIdAndProductVariantId(UUID cartId, UUID variantId);

    /**
     * Find items with stock issues
     */
    @Query("""
        SELECT ci FROM CartItem ci
        LEFT JOIN ci.productVariant v
        LEFT JOIN v.inventory i
        WHERE ci.cart.id = :cartId 
        AND (i IS NULL OR i.quantityAvailable < ci.quantity)
    """)
    List<CartItem> findItemsWithStockIssues(@Param("cartId") UUID cartId);
}