package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByUserId(UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);
}
