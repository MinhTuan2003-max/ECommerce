package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.PaymentTransaction;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Transaction Repository
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    /**
     * Find by transaction ID (from payment provider)
     */
    Optional<PaymentTransaction> findByTransactionId(String transactionId);

    /**
     * Check if transaction ID exists (for idempotency)
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Find by order
     */
    List<PaymentTransaction> findByOrderIdOrderByCreatedAtDesc(UUID orderId);

    /**
     * Find latest payment for order
     */
    @Query("""
        SELECT pt FROM PaymentTransaction pt 
        WHERE pt.order.id = :orderId 
        ORDER BY pt.createdAt DESC 
        LIMIT 1
    """)
    Optional<PaymentTransaction> findLatestByOrderId(@Param("orderId") UUID orderId);

    /**
     * Find by status
     */
    List<PaymentTransaction> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    /**
     * Find by method
     */
    List<PaymentTransaction> findByMethodOrderByCreatedAtDesc(PaymentMethod method);

    /**
     * Find pending payments (for reconciliation)
     */
    @Query("""
        SELECT pt FROM PaymentTransaction pt 
        WHERE pt.status IN ('PENDING', 'PROCESSING') 
        AND pt.createdAt < :threshold
    """)
    List<PaymentTransaction> findPendingPayments(@Param("threshold") LocalDateTime threshold);

    /**
     * Find failed payments
     */
    List<PaymentTransaction> findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(
            PaymentStatus status,
            LocalDateTime since
    );

    /**
     * Get total amount by method
     */
    @Query("""
        SELECT COALESCE(SUM(pt.amount), 0) 
        FROM PaymentTransaction pt 
        WHERE pt.method = :method 
        AND pt.status = 'PAID'
    """)
    java.math.BigDecimal getTotalAmountByMethod(@Param("method") PaymentMethod method);

    /**
     * Find by provider reference
     */
    Optional<PaymentTransaction> findByProviderReference(String providerReference);
}
