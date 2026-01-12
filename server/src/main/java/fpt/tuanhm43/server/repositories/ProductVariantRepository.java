package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    Optional<ProductVariant> findBySku(String sku);
}

