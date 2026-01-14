package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.CreateProductRequest;
import fpt.tuanhm43.server.dtos.product.request.ProductFilterRequest;
import fpt.tuanhm43.server.dtos.product.request.UpdateProductRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductDetailResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;

import java.util.UUID;

/**
 * Product Service Interface
 */
public interface ProductService {

    /**
     * Get all products with pagination
     */
    PageResponseDTO<ProductResponse> getAllWithFilter(ProductFilterRequest filter);

    /**
     * Get product by ID
     */
    ProductDetailResponse getById(UUID id);

    /**
     * Get product by slug
     */
    ProductDetailResponse getBySlug(String slug);

    /**
     * Create product (Admin)
     */
    ProductResponse create(CreateProductRequest request);

    /**
     * Update product (Admin)
     */
    ProductResponse update(UUID id, UpdateProductRequest request);

    /**
     * Delete product (Admin - soft delete)
     */
    void delete(UUID id);

    /**
     * Search products
     */
    PageResponseDTO<ProductResponse> searchByKeyword(String keyword, int page, int size);

    /**
     * Get products by category
     */
    PageResponseDTO<ProductResponse> getByCategory(UUID categoryId, int page, int size);
}
