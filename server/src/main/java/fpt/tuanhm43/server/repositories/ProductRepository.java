package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
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
     * Check if slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Find active products
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);

    /**
     * Find by category
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    /**
     * Find by category slug
     */
    @Query("SELECT p FROM Product p WHERE p.category.slug = :categorySlug AND p.isActive = true")
    Page<Product> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    /**
     * Search by name (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isActive = true")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find by price range
     */
    @Query("SELECT p FROM Product p WHERE p.basePrice BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    Page<Product> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Find products with variants in stock
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.variants v
        LEFT JOIN v.inventory i
        WHERE i.quantityAvailable > 0 AND p.isActive = true
    """)
    Page<Product> findInStockProducts(Pageable pageable);

    /**
     * Find product with variants (fetch join)
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") UUID id);

    /**
     * Find featured products (example: newest)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findFeaturedProducts(Pageable pageable);

    /**
     * Count by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Long countByCategoryId(@Param("categoryId") UUID categoryId);
}