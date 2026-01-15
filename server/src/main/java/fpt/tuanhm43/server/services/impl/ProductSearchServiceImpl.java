package fpt.tuanhm43.server.services.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import fpt.tuanhm43.server.documents.ProductSearchDocument;
import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.dtos.search.request.AdvancedSearchRequest;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.repositories.ProductSearchRepository;
import fpt.tuanhm43.server.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ElasticsearchOperations esOps;
    private final ProductSearchRepository searchRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public void reindexAll() {
        log.info("Starting Reindex All products to Elasticsearch...");

        searchRepository.deleteAll();

        List<Product> products = productRepository.findAll();

        List<ProductSearchDocument> documents = products.stream()
                .map(this::mapToDocument)
                .toList();

        searchRepository.saveAll(documents);
        log.info("Successfully reindexed {} products", documents.size());
    }

    public PageResponseDTO<ProductResponse> advancedSearch(AdvancedSearchRequest request) {
        int page = request.getPage() != null ? Math.max(request.getPage(), 0) : 0;
        int size = request.getSize() != null ? Math.max(request.getSize(), 1) : 20;

        Sort.Direction direction = "Desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "createdAt";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        boolQuery.filter(f -> f.term(t -> t.field("isActive").value(true)));

        if (StringUtils.hasText(request.getKeyword())) {
            List<String> fields = (request.getSearchableFields() != null && !request.getSearchableFields().isEmpty())
                    ? request.getSearchableFields() : List.of("name^3", "description");

            boolQuery.must(m -> m.multiMatch(mm -> {
                mm.fields(fields).query(request.getKeyword());
                if (request.isFuzzy()) mm.fuzziness("AUTO");
                if (request.isPhrase()) mm.type(TextQueryType.Phrase);
                return mm;
            }));
        }

        if (request.getFilters() != null) {
            request.getFilters().forEach((field, value) -> {
                if (StringUtils.hasText(value)) {
                    boolQuery.filter(f -> f.term(t -> t.field(field).value(value)));
                }
            });
        }

        if (request.getRanges() != null) {
            request.getRanges().forEach((field, range) ->
                    boolQuery.filter(f -> f.range(r -> {
                        if (StringUtils.hasText(range.getFrom())) r.gte(JsonData.of(range.getFrom()));
                        if (StringUtils.hasText(range.getTo())) r.lte(JsonData.of(range.getTo()));
                        return r.field(field);
                    }))
            );
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()))
                .withPageable(pageable)
                .build();

        SearchHits<ProductSearchDocument> hits = esOps.search(query, ProductSearchDocument.class);
        return mapToPageResponse(hits, pageable);
    }

    @Async
    public void syncToElasticsearch(UUID productId) {
        productRepository.findByIdWithVariants(productId).ifPresent(product -> {
            ProductSearchDocument doc = ProductSearchDocument.builder()
                    .id(product.getId().toString())
                    .name(product.getName())
                    .slug(product.getSlug())
                    .description(product.getDescription())
                    .minPrice(product.getMinPrice().doubleValue())
                    .categoryId(product.getCategory().getId().toString())
                    .categoryName(product.getCategory().getName())
                    .isActive(product.getIsActive())
                    .createdAt(product.getCreatedAt())
                    .build();
            searchRepository.save(doc);
            log.info("Synced product to Elasticsearch: {}", product.getName());
        });
    }

    private ProductSearchDocument mapToDocument(Product product) {
        return ProductSearchDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .minPrice(product.getMinPrice() != null ? product.getMinPrice().doubleValue() : 0.0)
                .categoryId(product.getCategory().getId().toString())
                .categoryName(product.getCategory().getName())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private PageResponseDTO<ProductResponse> mapToPageResponse(SearchHits<ProductSearchDocument> hits, Pageable pageable) {
        List<ProductResponse> content = hits.stream().map(hit -> {
            ProductSearchDocument doc = hit.getContent();
            return ProductResponse.builder()
                    .id(UUID.fromString(doc.getId()))
                    .name(doc.getName())
                    .slug(doc.getSlug())
                    .minPrice(BigDecimal.valueOf(doc.getMinPrice()))
                    .categoryName(doc.getCategoryName())
                    .isActive(doc.getIsActive())
                    .createdAt(doc.getCreatedAt())
                    .build();
        }).toList();

        return PageResponseDTO.<ProductResponse>builder()
                .content(content)
                .totalElements(hits.getTotalHits())
                .totalPages((int) Math.ceil((double) hits.getTotalHits() / pageable.getPageSize()))
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .build();
    }
}