package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.CreateProductRequest;
import fpt.tuanhm43.server.dtos.product.request.UpdateProductRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductDetailResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.dtos.search.AdvancedSearchRequest;
import fpt.tuanhm43.server.services.ProductSearchService;
import fpt.tuanhm43.server.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "Endpoints for managing products, variants, and inventory in the Hypebeast catalog")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Fetch full product details including variants (size/color) and images by its UUID.")
    public ResponseEntity<ApiResponseDTO<ProductDetailResponse>> getProductById(
            @Parameter(description = "UUID of the product", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("id") UUID id) {
        log.info("Fetching product by ID: {}", id);
        ProductDetailResponse response = productService.getById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Retrieve product details using an SEO-friendly URL slug (e.g., 'nike-air-jordan-1').")
    public ResponseEntity<ApiResponseDTO<ProductDetailResponse>> getProductBySlug(
            @Parameter(description = "SEO friendly slug", example = "nike-air-jordan-1-retro")
            @PathVariable("slug") String slug) {
        log.info("Fetching product by slug: {}", slug);
        ProductDetailResponse response = productService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PostMapping("/search")
    @Operation(summary = "Advanced Dynamic Search", description = "Gộp chung Search và Filter sử dụng Elasticsearch")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponse>>> advancedSearch(
            @RequestBody AdvancedSearchRequest request) {
        log.info("Advanced search request: {}", request);
        PageResponseDTO<ProductResponse> response = productSearchService.advancedSearch(request);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PostMapping
    @Operation(
            summary = "Create new product",
            description = "Registers a new product. Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("Creating product: {}", request);
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Product created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product",
            description = "Update existing product details. Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ProductResponse>> updateProduct(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("Updating product: {}", id);
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete product",
            description = "Soft delete a product from the catalog. Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @PathVariable("id") UUID id) {
        log.info("Deleting product: {}", id);
        productService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Product deleted successfully"));
    }

}