package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.*;
import fpt.tuanhm43.server.dtos.product.response.*;
import fpt.tuanhm43.server.dtos.search.request.AdvancedSearchRequest;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.exceptions.*;
import fpt.tuanhm43.server.repositories.*;
import fpt.tuanhm43.server.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT =  "Product";
    private static final String CATEGORY = "Category";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSearchServiceImpl productSearchService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponse> getAllWithFilter(ProductFilterRequest filter) {
        AdvancedSearchRequest advancedRequest = AdvancedSearchRequest.builder()
                .page(filter.getPage())
                .size(filter.getSize())
                .keyword(filter.getKeyword())
                .fuzzy(true)
                .filters(filter.getCategoryId() != null ?
                        Map.of("categoryId", filter.getCategoryId().toString()) : null)
                .ranges(filter.getMinPrice() != null || filter.getMaxPrice() != null ?
                        Map.of("minPrice", createRange(filter.getMinPrice(), filter.getMaxPrice())) : null)
                .build();

        return productSearchService.advancedSearch(advancedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponse> searchByKeyword(String keyword, int page, int size) {
        AdvancedSearchRequest advancedRequest = AdvancedSearchRequest.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .fuzzy(true)
                .build();
        return productSearchService.advancedSearch(advancedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponse> getByCategory(UUID categoryId, int page, int size) {
        AdvancedSearchRequest advancedRequest = AdvancedSearchRequest.builder()
                .filters(Map.of("categoryId", categoryId.toString()))
                .page(page)
                .size(size)
                .build();
        return productSearchService.advancedSearch(advancedRequest);
    }

    // Tiện ích tạo Range object
    private AdvancedSearchRequest.RangeValue createRange(BigDecimal from, BigDecimal to) {
        AdvancedSearchRequest.RangeValue range = new AdvancedSearchRequest.RangeValue();
        if (from != null) range.setFrom(from.toString());
        if (to != null) range.setTo(to.toString());
        return range;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getById(UUID id) {
        log.info("Getting product by id from database: {}", id);

        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new ResourceNotFoundException( PRODUCT, "id", id));

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw new ResourceNotFoundException( PRODUCT, "id", id);
        }

        return mapToProductDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug) {
        log.info("Getting product by slug from database: {}", slug);

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException( PRODUCT, "slug", slug));

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw new ResourceNotFoundException( PRODUCT, "slug", slug);
        }

        return mapToProductDetailResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName()).slug(request.getSlug()).description(request.getDescription())
                .basePrice(request.getBasePrice()).category(category).isActive(true).build();

        Product savedProduct = productRepository.save(product);
        productSearchService.syncToElasticsearch(savedProduct.getId());
        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException( PRODUCT, "id", id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());

        Product updatedProduct = productRepository.save(product);
        productSearchService.syncToElasticsearch(id);
        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException( PRODUCT, "id", id));
        product.setIsActive(false);
        productRepository.save(product);
        productSearchService.syncToElasticsearch(id);
    }

    private ProductResponse mapToProductResponse(Product product) {
        String imageUrl = product.getVariants().stream()
                .map(ProductVariant::getImageUrl)
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
                .inStock(product.getVariants() != null && !product.getVariants().isEmpty())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}