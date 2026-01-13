package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find by tracking token (guest tracking)
     */
    Optional<Order> findByTrackingToken(UUID trackingToken);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find by user
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find by customer email (guest orders)
     */
    Page<Order> findByCustomerEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    /**
     * Find by status
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find by payment method
     */
    Page<Order> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * Find by payment status
     */
    Page<Order> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find orders created between dates
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find order with items (fetch join)
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    /**
     * Find order with items and status history
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        LEFT JOIN FETCH o.items 
        LEFT JOIN FETCH o.statusHistory 
        WHERE o.id = :id
    """)
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Find pending payment orders (for reminder)
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.paymentStatus = 'PENDING' 
        AND o.paymentMethod != 'COD' 
        AND o.createdAt > :threshold
    """)
    List<Order> findPendingPaymentOrders(@Param("threshold") LocalDateTime threshold);

    /**
     * Count orders by status
     */
    Long countByStatus(OrderStatus status);

    /**
     * Get total revenue
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
    java.math.BigDecimal getTotalRevenue();

    /**
     * Get revenue by date range
     */
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0) 
        FROM Order o 
        WHERE o.paymentStatus = 'PAID' 
        AND o.createdAt BETWEEN :startDate AND :endDate
    """)
    java.math.BigDecimal getRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find recent orders (dashboard)
     */
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);
}

