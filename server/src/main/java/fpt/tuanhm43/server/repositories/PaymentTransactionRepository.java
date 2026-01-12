package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.PaymentTransaction;
import fpt.tuanhm43.server.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByTransactionId(String transactionId);
    List<PaymentTransaction> findByOrderId(UUID orderId);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
}

