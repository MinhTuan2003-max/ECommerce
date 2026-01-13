package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Category Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find by slug
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Find active categories
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find root categories (no parent)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findRootCategories();

    /**
     * Find children of category
     */
    List<Category> findByParentIdAndIsActiveTrue(UUID parentId);

    /**
     * Find category with children
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") UUID id);

    /**
     * Find category with products
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(@Param("id") UUID id);
}
