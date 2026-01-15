package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.category.request.CategoryFilterRequest;
import fpt.tuanhm43.server.dtos.category.request.CreateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.request.UpdateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.response.CategoryDetailResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryTreeResponse;
import fpt.tuanhm43.server.entities.Category;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.CategoryRepository;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.specifications.CategorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements fpt.tuanhm43.server.services.CategoryService {

    private static final String CATEGORY = "Category";
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponse> getAllWithFilter(CategoryFilterRequest filter) {
        log.info("Getting categories with filter - Name: {}, IsActive: {}, ParentId: {}, Page: {}, Size: {}",
                filter.getName(), filter.getIsActive(), filter.getParentId(),
                filter.getPage(), filter.getSize());

        int pageNumber = filter.getPage() != null ? Math.max(filter.getPage(), 0) : 0;
        int pageSize = filter.getSize() != null ? Math.max(filter.getSize(), 1) : DEFAULT_PAGE_SIZE;

        Sort.Direction direction = "DESC".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, filter.getSortBy() != null ? filter.getSortBy() : "displayOrder");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Build dynamic specification
        Specification<Category> spec = CategorySpecification.buildSpecification(
                filter.getName(),
                filter.getSlug(),
                filter.getIsActive(),
                filter.getParentId(),
                filter.getIncludeDeleted()
        );

        Page<Category> categories = categoryRepository.findAll(spec, pageable);

        return PageResponseDTO.from(categories.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponse getById(UUID id) {
        log.info("Getting category by id: {}", id);

        Category category = categoryRepository.findByIdWithChildren(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", id));

        return mapToDetailResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponse getBySlug(String slug) {
        log.info("Getting category by slug: {}", slug);

        Category category = categoryRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "slug", slug));

        return mapToDetailResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.info("Getting all root categories");

        List<Category> categories = categoryRepository.findAllRootCategories();
        return categories.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        log.info("Getting category tree structure");

        // Get all active, non-deleted categories
        Specification<Category> spec = Specification.where(CategorySpecification.isActive())
                .and(CategorySpecification.isNotDeleted());

        List<Category> allCategories = categoryRepository.findAll(spec);

        // Build tree structure
        Map<UUID, CategoryTreeResponse> categoryMap = new HashMap<>();
        List<CategoryTreeResponse> roots = new ArrayList<>();

        // First pass: create all nodes
        for (Category category : allCategories) {
            CategoryTreeResponse node = mapToTreeResponse(category);
            categoryMap.put(category.getId(), node);
        }

        // Second pass: build tree
        for (Category category : allCategories) {
            CategoryTreeResponse node = categoryMap.get(category.getId());
            if (category.getParent() == null) {
                roots.add(node);
            } else {
                CategoryTreeResponse parent = categoryMap.get(category.getParent().getId());
                if (parent != null) {
                    parent.addChild(node);
                }
            }
        }

        return roots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildrenByParentId(UUID parentId) {
        log.info("Getting children of category: {}", parentId);

        // Verify parent exists
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", parentId));

        List<Category> children = categoryRepository.findChildrenByParentId(parentId);
        return children.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Validate unique name
        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("Category name already exists");
        }

        // Validate unique slug
        if (categoryRepository.existsBySlugAndIsDeletedFalse(request.getSlug())) {
            throw new BadRequestException("Category slug already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .build();

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getParentId()));

            if (Boolean.TRUE.equals(parent.getIsDeleted())) {
                throw new BadRequestException("Cannot set deleted category as parent");
            }

            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());

        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        log.info("Updating category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", id));

        if (Boolean.TRUE.equals(category.getIsDeleted())) {
            throw new BadRequestException("Cannot update deleted category");
        }

        // Validate name uniqueness if changed
        if (request.getName() != null && !category.getName().equals(request.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.getName())) {
                throw new BadRequestException("Category name already exists");
            }
            category.setName(request.getName());
        }

        // Update parent if provided
        if (request.getParentId() != null) {
            // Prevent circular reference
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }

            // Check if new parent is a descendant (would create circular reference)
            List<UUID> descendantIds = categoryRepository.findAllDescendantIds(id);
            if (descendantIds.contains(request.getParentId())) {
                throw new BadRequestException("Cannot set descendant as parent (circular reference)");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getParentId()));

            if (Boolean.TRUE.equals(parent.getIsDeleted())) {
                throw new BadRequestException("Cannot set deleted category as parent");
            }

            category.setParent(parent);
        }

        // Update other fields
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully: {}", id);

        return mapToResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", id));

        if (Boolean.TRUE.equals(category.getIsDeleted())) {
            throw new BadRequestException("Category already deleted");
        }

        // Get all descendants
        List<UUID> descendantIds = categoryRepository.findAllDescendantIds(id);
        descendantIds.add(id); // Include the category itself

        // Soft delete all descendants
        for (UUID categoryId : descendantIds) {
            Category cat = categoryRepository.findById(categoryId).orElse(null);
            if (cat != null && Boolean.FALSE.equals(cat.getIsDeleted())) {
                cat.setIsDeleted(true);
                cat.setDeletedAt(LocalDateTime.now());
                cat.setIsActive(false);
                categoryRepository.save(cat);

                // Soft delete all products in this category
                List<Product> products = cat.getProducts();
                for (Product product : products) {
                    if (Boolean.FALSE.equals(product.getIsDeleted())) {
                        product.setIsDeleted(true);
                        product.setDeletedAt(LocalDateTime.now());
                        product.setIsActive(false);
                        productRepository.save(product);
                    }
                }

                log.info("Soft deleted category and its products: {}", categoryId);
            }
        }

        log.info("Category deleted (soft delete) successfully with all descendants: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProducts(UUID id) {
        return categoryRepository.countProductsByCategoryId(id) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts(UUID id) {
        // Count products in this category and all descendants
        List<UUID> descendantIds = categoryRepository.findAllDescendantIds(id);

        long totalCount = categoryRepository.countProductsByCategoryId(id);
        for (UUID descendantId : descendantIds) {
            totalCount += categoryRepository.countProductsByCategoryId(descendantId);
        }

        return totalCount;
    }

    // Mapping methods
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .productCount(categoryRepository.countProductsByCategoryId(category.getId()))
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private CategoryDetailResponse mapToDetailResponse(Category category) {
        List<CategoryDetailResponse.CategoryChildResponse> children = category.getChildren().stream()
                .filter(child -> Boolean.FALSE.equals(child.getIsDeleted()))
                .map(child -> CategoryDetailResponse.CategoryChildResponse.builder()
                        .id(child.getId())
                        .name(child.getName())
                        .slug(child.getSlug())
                        .displayOrder(child.getDisplayOrder())
                        .isActive(child.getIsActive())
                        .productCount(categoryRepository.countProductsByCategoryId(child.getId()))
                        .build())
                .toList();

        return CategoryDetailResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .productCount(categoryRepository.countProductsByCategoryId(category.getId()))
                .children(children)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private CategoryTreeResponse mapToTreeResponse(Category category) {
        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .productCount(categoryRepository.countProductsByCategoryId(category.getId()))
                .children(new ArrayList<>())
                .build();
    }
}