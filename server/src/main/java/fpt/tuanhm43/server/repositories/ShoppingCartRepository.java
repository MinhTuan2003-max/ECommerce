package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Shopping Cart Repository
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    /**
     * Find by session ID (guest cart)
     */
    Optional<ShoppingCart> findBySessionId(String sessionId);

    /**
     * Find by user ID (logged-in user cart)
     */
    Optional<ShoppingCart> findByUserId(UUID userId);

    /**
     * Find cart with items (fetch join)
     */
    @Query("""
    SELECT DISTINCT c FROM ShoppingCart c
    LEFT JOIN FETCH c.items ci
    LEFT JOIN FETCH ci.productVariant pv
    LEFT JOIN FETCH pv.product p
    LEFT JOIN FETCH pv.inventory i
    WHERE c.sessionId = :sessionId
    AND c.isDeleted = false
""")
    Optional<ShoppingCart> findBySessionIdWithItems(@Param("sessionId") String sessionId);

    /**
     * Delete expired carts
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ShoppingCart c WHERE c.expiresAt < :now")
    int deleteExpiredCarts(@Param("now") LocalDateTime now);

    /**
     * Count items in cart
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM ShoppingCart c JOIN c.items ci WHERE c.sessionId = :sessionId")
    Integer countItemsBySessionId(@Param("sessionId") String sessionId);
}