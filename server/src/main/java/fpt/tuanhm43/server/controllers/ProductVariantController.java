package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.CreateProductVariantRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductVariantResponse;
import fpt.tuanhm43.server.services.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Variant", description = "Endpoints for managing product sizes, colors, and inventory variants")
public class ProductVariantController {

    private final ProductVariantService variantService;

    @GetMapping
    @Operation(summary = "Get all variants of a product", description = "Retrieve a list of all sizes and colors available for a specific product ID.")
    public ResponseEntity<ApiResponseDTO<List<ProductVariantResponse>>> getVariants(
            @PathVariable UUID productId) {
        log.info("Fetching all variants for product ID: {}", productId);
        List<ProductVariantResponse> response = variantService.getVariantsByProductId(productId);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new product variant",
            description = "Add a new size/color variant to an existing product. Required role: ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseDTO<ProductVariantResponse>> createVariant(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateProductVariantRequest request) {

        log.info("Creating new variant for product ID: {} with SKU: {}", productId, request.getSku());
        ProductVariantResponse response = variantService.createVariant(productId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Product variant created successfully!"));
    }
}