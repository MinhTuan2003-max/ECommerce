package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.category.request.CategoryFilterRequest;
import fpt.tuanhm43.server.dtos.category.request.CreateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.request.UpdateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.response.CategoryDetailResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryTreeResponse;
import fpt.tuanhm43.server.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "Endpoints for managing product categories with hierarchical structure. Public can view, only admins can modify.")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories with filters", description = "Retrieve paginated list of categories with dynamic filtering by name, slug, active status, parent category, and soft-deleted status.")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<CategoryResponse>>> getAllCategories(
            @Parameter(description = "Filter by category name (partial match, case-insensitive)") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by category slug (exact match)") @RequestParam(required = false) String slug,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Filter by parent category ID (null for root categories)") @RequestParam(required = false) UUID parentId,
            @Parameter(description = "Include soft-deleted categories (admin only)") @RequestParam(required = false) Boolean includeDeleted,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "displayOrder") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("GET /api/v1/categories - Filtering categories");

        CategoryFilterRequest filter = CategoryFilterRequest.builder()
                .name(name)
                .slug(slug)
                .isActive(isActive)
                .parentId(parentId)
                .includeDeleted(includeDeleted)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PageResponseDTO<CategoryResponse> response = categoryService.getAllWithFilter(filter);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve detailed information about a specific category including its children and product count.")
    public ResponseEntity<ApiResponseDTO<CategoryDetailResponse>> getCategoryById(
            @Parameter(description = "UUID of the category") @PathVariable UUID id
    ) {
        log.info("GET /api/v1/categories/{} - Getting category by ID", id);

        CategoryDetailResponse response = categoryService.getById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieve detailed information about a category using its unique slug identifier, including children and product count.")
    public ResponseEntity<ApiResponseDTO<CategoryDetailResponse>> getCategoryBySlug(
            @Parameter(description = "Unique slug of the category") @PathVariable String slug
    ) {
        log.info("GET /api/v1/categories/slug/{} - Getting category by slug", slug);

        CategoryDetailResponse response = categoryService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/roots")
    @Operation(summary = "Get all root categories", description = "Retrieve all top-level categories that have no parent (e.g., Shoes, Clothing, Accessories).")
    public ResponseEntity<ApiResponseDTO<List<CategoryResponse>>> getRootCategories() {
        log.info("GET /api/v1/categories/roots - Getting root categories");

        List<CategoryResponse> response = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree structure", description = "Retrieve the complete hierarchical tree of all active categories, organized from parent to children recursively.")
    public ResponseEntity<ApiResponseDTO<List<CategoryTreeResponse>>> getCategoryTree() {
        log.info("GET /api/v1/categories/tree - Getting category tree");

        List<CategoryTreeResponse> response = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Get category children", description = "Retrieve all direct child categories of a specific parent category (e.g., children of 'Shoes' could be 'Running Shoes', 'Formal Shoes').")
    public ResponseEntity<ApiResponseDTO<List<CategoryResponse>>> getCategoryChildren(
            @Parameter(description = "UUID of the parent category") @PathVariable UUID id
    ) {
        log.info("GET /api/v1/categories/{}/children - Getting children", id);

        List<CategoryResponse> response = categoryService.getChildrenByParentId(id);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/{id}/products/count")
    @Operation(summary = "Count products in category", description = "Get the total number of products in a category and all its descendant categories.")
    public ResponseEntity<ApiResponseDTO<Long>> countProducts(
            @Parameter(description = "UUID of the category") @PathVariable UUID id
    ) {
        log.info("GET /api/v1/categories/{}/products/count", id);

        long count = categoryService.countProducts(id);
        return ResponseEntity.ok(ApiResponseDTO.success(count));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new category", description = "Create a new product category. Can be a root category or a child of an existing category. Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    public ResponseEntity<ApiResponseDTO<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        log.info("POST /api/v1/categories - Creating category: {}", request.getName());

        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Update an existing category's information including name, description, parent, display order, or active status. Validates against circular references. Requires ADMIN role.")
    public ResponseEntity<ApiResponseDTO<CategoryResponse>> updateCategory(
            @Parameter(description = "UUID of the category to update") @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        log.info("PUT /api/v1/categories/{} - Updating category", id);

        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Soft delete a category along with all its descendant categories and products. This operation cascades to all children and marks associated products as deleted. Requires ADMIN role.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCategory(
            @Parameter(description = "UUID of the category to delete") @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/categories/{} - Deleting category", id);

        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }
}