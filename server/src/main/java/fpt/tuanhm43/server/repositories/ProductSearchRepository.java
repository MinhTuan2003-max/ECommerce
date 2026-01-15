package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.documents.ProductSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, String> {
}