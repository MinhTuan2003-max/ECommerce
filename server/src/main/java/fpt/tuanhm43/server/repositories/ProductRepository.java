package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find by slug
     */
    Optional<Product> findBySlug(String slug);

    /**
     * Find product with variants (fetch join)
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") UUID id);

    /**
     * Find product with category and variants (fetch join)
     */
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.variants " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndVariants(@Param("id") UUID id);

}