package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.category.request.CategoryFilterRequest;
import fpt.tuanhm43.server.dtos.category.request.CreateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.request.UpdateCategoryRequest;
import fpt.tuanhm43.server.dtos.category.response.CategoryDetailResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryTreeResponse;
import fpt.tuanhm43.server.entities.Category;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.mappers.CategoryMapper;
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
    private final CategoryMapper categoryMapper; // Inject Mapper

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponse> getAllWithFilter(CategoryFilterRequest filter) {
        int pageNumber = filter.getPage() != null ? Math.max(filter.getPage(), 0) : 0;
        int pageSize = filter.getSize() != null ? Math.max(filter.getSize(), 1) : DEFAULT_PAGE_SIZE;

        Sort.Direction direction = "DESC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, filter.getSortBy() != null ? filter.getSortBy() : "displayOrder");
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Category> spec = CategorySpecification.buildSpecification(
                filter.getName(), filter.getSlug(), filter.getIsActive(), filter.getParentId(), filter.getIncludeDeleted()
        );

        Page<Category> categories = categoryRepository.findAll(spec, pageable);
        // Sử dụng Mapper
        return PageResponseDTO.from(categories.map(categoryMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponse getById(UUID id) {
        return categoryRepository.findByIdWithChildren(id)
                .map(categoryMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponse getBySlug(String slug) {
        return categoryRepository.findBySlugAndIsDeletedFalse(slug)
                .map(categoryMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "slug", slug));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findAllRootCategories().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        Specification<Category> spec = Specification.where(CategorySpecification.isActive())
                .and(CategorySpecification.isNotDeleted());

        List<Category> allCategories = categoryRepository.findAll(spec);
        Map<UUID, CategoryTreeResponse> categoryMap = new HashMap<>();
        List<CategoryTreeResponse> roots = new ArrayList<>();

        // Bước 1: Map tất cả sang node phẳng
        for (Category category : allCategories) {
            categoryMap.put(category.getId(), categoryMapper.toTreeResponse(category));
        }

        // Bước 2: Xây dựng cấu trúc cây
        for (Category category : allCategories) {
            CategoryTreeResponse node = categoryMap.get(category.getId());
            if (category.getParent() == null) {
                roots.add(node);
            } else {
                CategoryTreeResponse parent = categoryMap.get(category.getParent().getId());
                if (parent != null) parent.addChild(node);
            }
        }
        return roots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildrenByParentId(UUID parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException(CATEGORY, "id", parentId);
        }
        return categoryRepository.findChildrenByParentId(parentId).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("Category name already exists");
        }
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

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getParentId()));
            if (Boolean.TRUE.equals(parent.getIsDeleted())) throw new BadRequestException("Parent deleted");
            category.setParent(parent);
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", id));

        if (Boolean.TRUE.equals(category.getIsDeleted())) throw new BadRequestException("Deleted category");

        if (request.getName() != null && !category.getName().equals(request.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.getName())) {
                throw new BadRequestException("Name exists");
            }
            category.setName(request.getName());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) throw new BadRequestException("Self parent");
            List<UUID> descendantIds = categoryRepository.findAllDescendantIds(id);
            if (descendantIds.contains(request.getParentId())) throw new BadRequestException("Circular reference");

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getParentId()));
            category.setParent(parent);
        }

        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        List<UUID> descendantIds = categoryRepository.findAllDescendantIds(id);
        descendantIds.add(id);

        for (UUID categoryId : descendantIds) {
            categoryRepository.findById(categoryId).ifPresent(cat -> {
                if (Boolean.FALSE.equals(cat.getIsDeleted())) {
                    cat.setIsDeleted(true);
                    cat.setDeletedAt(LocalDateTime.now());
                    cat.setIsActive(false);
                    categoryRepository.save(cat);

                    cat.getProducts().forEach(product -> {
                        if (Boolean.FALSE.equals(product.getIsDeleted())) {
                            product.setIsDeleted(true);
                            product.setDeletedAt(LocalDateTime.now());
                            product.setIsActive(false);
                            productRepository.save(product);
                        }
                    });
                }
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProducts(UUID id) {
        return categoryRepository.countProductsByCategoryId(id) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts(UUID id) {
        List<UUID> descendantIds = categoryRepository.findAllDescendantIds(id);
        long totalCount = categoryRepository.countProductsByCategoryId(id);
        for (UUID descendantId : descendantIds) {
            totalCount += categoryRepository.countProductsByCategoryId(descendantId);
        }
        return totalCount;
    }
}