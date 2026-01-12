package fpt.tuanhm43.server.repositories.cart;

import fpt.tuanhm43.server.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}

