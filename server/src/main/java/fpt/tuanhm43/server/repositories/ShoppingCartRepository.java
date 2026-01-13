package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.ShoppingCart;
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
     * Check if session cart exists
     */
    boolean existsBySessionId(String sessionId);

    /**
     * Find cart with items (fetch join)
     */
    @Query("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.items WHERE c.sessionId = :sessionId")
    Optional<ShoppingCart> findBySessionIdWithItems(@Param("sessionId") String sessionId);

    /**
     * Find expired carts for cleanup
     */
    @Query("SELECT c FROM ShoppingCart c WHERE c.expiresAt < :now")
    List<ShoppingCart> findExpiredCarts(@Param("now") LocalDateTime now);

    /**
     * Delete expired carts
     */
    @Modifying
    @Query("DELETE FROM ShoppingCart c WHERE c.expiresAt < :now")
    int deleteExpiredCarts(@Param("now") LocalDateTime now);

    /**
     * Count items in cart
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM ShoppingCart c JOIN c.items ci WHERE c.sessionId = :sessionId")
    Integer countItemsBySessionId(@Param("sessionId") String sessionId);

    /**
     * Get total cart value
     */
    @Query("SELECT COALESCE(SUM(ci.subtotal), 0) FROM ShoppingCart c JOIN c.items ci WHERE c.sessionId = :sessionId")
    java.math.BigDecimal getTotalAmountBySessionId(@Param("sessionId") String sessionId);
}