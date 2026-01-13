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
    PageResponseDTO<ProductResponse> getAllProducts(ProductFilterRequest filter);

    /**
     * Get product by ID
     */
    ProductDetailResponse getProductById(UUID id);

    /**
     * Get product by slug
     */
    ProductDetailResponse getProductBySlug(String slug);

    /**
     * Create product (Admin)
     */
    ProductResponse createProduct(CreateProductRequest request);

    /**
     * Update product (Admin)
     */
    ProductResponse updateProduct(UUID id, UpdateProductRequest request);

    /**
     * Delete product (Admin - soft delete)
     */
    void deleteProduct(UUID id);

    /**
     * Search products
     */
    PageResponseDTO<ProductResponse> searchProducts(String keyword, int page, int size);

    /**
     * Get products by category
     */
    PageResponseDTO<ProductResponse> getProductsByCategory(UUID categoryId, int page, int size);
}
