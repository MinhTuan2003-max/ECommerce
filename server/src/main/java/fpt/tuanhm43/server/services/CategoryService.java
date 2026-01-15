package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.category.request.CategoryFilterRequest;
import fpt.tuanhm43.server.dtos.category.request.CreateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.request.UpdateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.response.CategoryDetailResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryTreeResponse;

import java.util.List;
import java.util.UUID;

/**
 * Category Service Interface
 */
public interface CategoryService {

    /**
     * Get all categories with dynamic filtering and pagination
     */
    PageResponseDTO<CategoryResponse> getAllWithFilter(CategoryFilterRequest filter);

    /**
     * Get category by ID with children
     */
    CategoryDetailResponse getById(UUID id);

    /**
     * Get category by slug with children
     */
    CategoryDetailResponse getBySlug(String slug);

    /**
     * Get all root categories (no parent)
     */
    List<CategoryResponse> getRootCategories();

    /**
     * Get category tree structure (hierarchical)
     */
    List<CategoryTreeResponse> getCategoryTree();

    /**
     * Get children of a category
     */
    List<CategoryResponse> getChildrenByParentId(UUID parentId);

    /**
     * Create category (Admin only)
     */
    CategoryResponse create(CreateCategoryRequest request);

    /**
     * Update category (Admin only)
     */
    CategoryResponse update(UUID id, UpdateCategoryRequest request);

    /**
     * Delete category (Admin only - soft delete with cascade handling)
     */
    void delete(UUID id);

    /**
     * Check if category has products
     */
    boolean hasProducts(UUID id);

    /**
     * Count products in category (including children)
     */
    long countProducts(UUID id);
}