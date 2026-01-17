package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.dtos.search.AdvancedSearchRequest;

import java.util.UUID;

public interface ProductSearchService {

    void reindexAll();

    PageResponseDTO<ProductResponse> advancedSearch(AdvancedSearchRequest request);

    void syncToElasticsearch(UUID productId);
}
