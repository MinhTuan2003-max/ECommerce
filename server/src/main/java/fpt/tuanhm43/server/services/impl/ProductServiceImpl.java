package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.CreateProductRequest;
import fpt.tuanhm43.server.dtos.product.request.ProductFilterRequest;
import fpt.tuanhm43.server.dtos.product.request.UpdateProductRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductDetailResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.dtos.search.AdvancedSearchRequest;
import fpt.tuanhm43.server.entities.Category;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.mappers.ProductMapper;
import fpt.tuanhm43.server.repositories.CategoryRepository;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.services.ProductService;
import fpt.tuanhm43.server.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT = "Product";
    private static final String CATEGORY = "Category";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSearchService productSearchService;

    private final ProductMapper productMapper;

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
        return productSearchService.advancedSearch(AdvancedSearchRequest.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .fuzzy(true)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponse> getByCategory(UUID categoryId, int page, int size) {
        return productSearchService.advancedSearch(AdvancedSearchRequest.builder()
                .filters(Map.of("categoryId", categoryId.toString()))
                .page(page)
                .size(size)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getById(UUID id) {
        log.info("Getting product by id: {}", id);
        return productRepository.findByIdWithVariants(id)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .map(productMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);
        return productRepository.findBySlug(slug)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .map(productMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "slug", slug));
    }

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, "id", request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .category(category)
                .isActive(true)
                .build();

        Product savedProduct = productRepository.save(product);

        // Sync to Elasticsearch async
        productSearchService.syncToElasticsearch(savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", id));

        if (StringUtils.hasText(request.getName())) product.setName(request.getName());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (StringUtils.hasText(request.getDescription())) product.setDescription(request.getDescription());

        Product updatedProduct = productRepository.save(product);

        // Sync to Elasticsearch async
        productSearchService.syncToElasticsearch(id);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", id));

        product.setIsActive(false);
        productRepository.save(product);

        productSearchService.syncToElasticsearch(id);
    }

    private AdvancedSearchRequest.RangeValue createRange(BigDecimal from, BigDecimal to) {
        AdvancedSearchRequest.RangeValue range = new AdvancedSearchRequest.RangeValue();
        if (from != null) range.setFrom(from.toString());
        if (to != null) range.setTo(to.toString());
        return range;
    }
}