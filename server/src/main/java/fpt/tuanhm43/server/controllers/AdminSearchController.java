package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/search")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSearchController {
    private final ProductSearchService searchService;

    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        searchService.reindexAll();
        return ResponseEntity.ok("Reindex process started successfully");
    }
}
