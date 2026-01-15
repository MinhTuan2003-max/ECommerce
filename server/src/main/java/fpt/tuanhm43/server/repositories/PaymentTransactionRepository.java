package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * Find latest payment for order
     */
    @Query("""
        SELECT pt FROM PaymentTransaction pt\s
        WHERE pt.order.id = :orderId\s
        ORDER BY pt.createdAt DESC\s
        LIMIT 1
   \s""")
    Optional<PaymentTransaction> findLatestByOrderId(@Param("orderId") UUID orderId);

}
