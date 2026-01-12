package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
}

