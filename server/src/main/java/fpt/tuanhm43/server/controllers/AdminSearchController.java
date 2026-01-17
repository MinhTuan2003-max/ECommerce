package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.services.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Admin Search",
        description = "Administrative APIs related to Elasticsearch and search indexing"
)
@ApiResponse(
        responseCode = "200",
        description = "Reindex process successfully started",
        content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "Reindex process started successfully")
        )
)
@SecurityRequirement(name = "bearerAuth")
public class AdminSearchController {

    private final ProductSearchService searchService;

    @Operation(
            summary = "Reindex all product data",
            description = "Rebuilds the entire Product search index by reindexing data from the database into Elasticsearch."
    )
    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        searchService.reindexAll();
        return ResponseEntity.ok("Reindex process started successfully");
    }
}
