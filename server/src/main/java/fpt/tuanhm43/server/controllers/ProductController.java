package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.CreateProductRequest;
import fpt.tuanhm43.server.dtos.product.request.ProductFilterRequest;
import fpt.tuanhm43.server.dtos.product.request.UpdateProductRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductDetailResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Product Controller
 * Handles product listing, creation, and management
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products with filtering and pagination
     * Public endpoint - no authentication required
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponse>>> getAllProducts(
            @Valid ProductFilterRequest filter) {
        log.info("Fetching products with filter: {}", filter);
        PageResponseDTO<ProductResponse> response = productService.getAllProducts(filter);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Get product by ID with full details
     * Public endpoint
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductDetailResponse>> getProductById(
            @PathVariable("id") UUID id) {
        log.info("Fetching product by ID: {}", id);
        ProductDetailResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Get product by slug (SEO friendly URL)
     * Public endpoint
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponseDTO<ProductDetailResponse>> getProductBySlug(
            @PathVariable("slug") String slug) {
        log.info("Fetching product by slug: {}", slug);
        ProductDetailResponse response = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Search products by keyword
     * Public endpoint
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponse>>> searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Searching products with keyword: {}", keyword);
        PageResponseDTO<ProductResponse> response = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Get products by category
     * Public endpoint
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponse>>> getProductsByCategory(
            @PathVariable("categoryId") UUID categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Fetching products by category: {}", categoryId);
        PageResponseDTO<ProductResponse> response = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Create new product
     * Admin only
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("Creating product: {}", request);
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Product created successfully"));
    }

    /**
     * Update product
     * Admin only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ProductResponse>> updateProduct(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("Updating product: {}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Product updated successfully"));
    }

    /**
     * Delete product (soft delete)
     * Admin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @PathVariable("id") UUID id) {
        log.info("Deleting product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Product deleted successfully"));
    }
}
