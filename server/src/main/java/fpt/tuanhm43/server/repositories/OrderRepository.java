package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find by tracking token (guest tracking)
     */
    Optional<Order> findByTrackingToken(UUID trackingToken);

    /**
     * Find by user
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find by status
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find by payment status
     */
    Page<Order> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find orders created between dates
     */
    @Query("""
        SELECT o FROM Order o\s
        WHERE o.createdAt BETWEEN :startDate AND :endDate\s
        ORDER BY o.createdAt DESC
   \s""")
    Page<Order> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Count orders created after a specific date
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);
}

