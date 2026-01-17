package fpt.tuanhm43.server.listener;

import fpt.tuanhm43.server.events.ProductDeletedEvent;
import fpt.tuanhm43.server.events.ProductSavedEvent;
import fpt.tuanhm43.server.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSearchListener {

    private final ProductSearchService productSearchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductSaved(ProductSavedEvent event) {
        log.info("Commited: Updating product {} on Elasticsearch", event.getProductId());
        productSearchService.syncToElasticsearch(event.getProductId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("Commited: Syncing deleted status for product {} on Elasticsearch", event.getProductId());
        productSearchService.syncToElasticsearch(event.getProductId());
    }
}