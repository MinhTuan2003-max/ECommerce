package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Category Repository with JpaSpecificationExecutor for dynamic filtering
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    /**
     * Find by slug
     */
    Optional<Category> findBySlugAndIsDeletedFalse(String slug);

    /**
     * Check if slug exists (excluding deleted)
     */
    boolean existsBySlugAndIsDeletedFalse(String slug);

    /**
     * Check if name exists (case-insensitive, excluding deleted)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
            "WHERE LOWER(c.name) = LOWER(:name) AND c.isDeleted = false")
    boolean existsByNameIgnoreCaseAndIsDeletedFalse(@Param("name") String name);

    /**
     * Find all root categories (no parent, active, not deleted)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true AND c.isDeleted = false " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllRootCategories();

    /**
     * Find children by parent id
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isDeleted = false " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findChildrenByParentId(@Param("parentId") UUID parentId);

    /**
     * Find all active categories (not deleted)
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.isDeleted = false " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllActive();

    /**
     * Find category with children
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id AND c.isDeleted = false")
    Optional<Category> findByIdWithChildren(@Param("id") UUID id);

    /**
     * Count products by category (including children)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isDeleted = false")
    long countProductsByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Find all descendants of a category (recursive)
     */
    @Query(value = "WITH RECURSIVE category_tree AS ( " +
            "SELECT id, parent_id FROM categories WHERE id = :categoryId " +
            "UNION ALL " +
            "SELECT c.id, c.parent_id FROM categories c " +
            "INNER JOIN category_tree ct ON c.parent_id = ct.id " +
            ") SELECT id FROM category_tree", nativeQuery = true)
    List<UUID> findAllDescendantIds(@Param("categoryId") UUID categoryId);
}