package fpt.tuanhm43.server.config;

import fpt.tuanhm43.server.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class SearchDataInitializer implements CommandLineRunner {
    private final ProductSearchService productSearchService;

    @Override
    public void run(String... args) {
        productSearchService.reindexAll();
    }
}