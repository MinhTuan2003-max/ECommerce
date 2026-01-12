package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByTrackingId(String trackingId);
    Page<Order> findByUserId(UUID userId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}

