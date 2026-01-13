package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.CreateProductRequest;
import fpt.tuanhm43.server.dtos.product.request.ProductFilterRequest;
import fpt.tuanhm43.server.dtos.product.request.UpdateProductRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductDetailResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.entities.Category;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.CategoryRepository;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    // Service implementation for product management
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String PRODUCT = "Product";
    private static final String CATEGORY = "Category";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        log.info("Getting all products with filter - Category: {}, MinPrice: {}, MaxPrice: {}, Page: {}, Size: {}",
                filter.getCategoryId(), filter.getMinPrice(), filter.getMaxPrice(),
                filter.getPage(), filter.getSize());

        int pageNumber = filter.getPage() != null ? filter.getPage() : 0;
        int pageSize = filter.getSize() != null ? filter.getSize() : DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD));

        Page<Product> products = productRepository.findByIsActiveTrue(pageable);

        // Apply category filter if provided
        if (filter.getCategoryId() != null) {
            products = productRepository.findByCategoryId(filter.getCategoryId(), pageable);
        }

        // Apply price filter if provided
        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            java.math.BigDecimal minPrice = filter.getMinPrice() != null ?
                    java.math.BigDecimal.valueOf(filter.getMinPrice().doubleValue()) : java.math.BigDecimal.ZERO;
            java.math.BigDecimal maxPrice = filter.getMaxPrice() != null ?
                    java.math.BigDecimal.valueOf(filter.getMaxPrice().doubleValue()) : java.math.BigDecimal.valueOf(Double.MAX_VALUE);

            products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        }

        return PageResponseDTO.from(products.map(this::mapToProductResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(UUID id) {
        log.info("Getting product by id: {}", id);

        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", id));

        if (!product.getIsActive()) {
            throw new ResourceNotFoundException(PRODUCT, "id", id);
        }

        return mapToProductDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "slug", slug));

        if (!product.getIsActive()) {
            throw new ResourceNotFoundException(PRODUCT, "slug", slug);
        }

        return mapToProductDetailResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        // Check if product name already exists
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Product name already exists");
        }

        // Check if slug already exists
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Product slug already exists");
        }

        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getCategoryId()));

        // Create product
        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .imageUrl(request.getImageUrl())
                .category(category)
                .isActive(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", id));

        // Check if name exists (excluding current product)
        if (request.getName() != null && !product.getName().equals(request.getName()) &&
            productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Product name already exists");
        }

        // Update category if provided
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getCategoryId()));
            product.setCategory(category);
        }

        // Update fields if provided
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            product.setBasePrice(request.getBasePrice());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", id);

        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", id));

        product.setIsActive(false);
        productRepository.save(product);

        log.info("Product deleted (soft delete) successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponse> searchProducts(String keyword, int page, int size) {
        log.info("Searching products with keyword: {}", keyword);

        int pageNumber = Math.max(page, 0);
        int pageSize = Math.max(size, 1);

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD));

        Page<Product> products = productRepository.searchByName(keyword, pageable);

        return PageResponseDTO.from(products.map(this::mapToProductResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponse> getProductsByCategory(UUID categoryId, int page, int size) {
        log.info("Getting products by category: {}", categoryId);

        // Verify category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", categoryId));

        int pageNumber = Math.max(page, 0);
        int pageSize = Math.max(size, 1);

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD));

        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);

        return PageResponseDTO.from(products.map(this::mapToProductResponse));
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        // Get first image from variants
        String imageUrl = product.getVariants().stream()
                .map(variant -> variant.getImageUrl())
                .filter(img -> img != null && !img.trim().isEmpty())
                .findFirst()
                .orElse(product.getImageUrl());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .minPrice(product.getMinPrice())
                .imageUrl(imageUrl)
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }

    /**
     * Map Product entity to ProductDetailResponse DTO
     */
    private ProductDetailResponse mapToProductDetailResponse(Product product) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .imageUrl(product.getImageUrl())
                .isActive(product.getIsActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .minPrice(product.getMinPrice())
                .maxPrice(product.getMaxPrice())
                .inStock(!product.getVariants().isEmpty())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

