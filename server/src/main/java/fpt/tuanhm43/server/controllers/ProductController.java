package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.response.ProductResponseDTO;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponseDTO>>> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "id") String sort
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(sort));
        var pageResult = productRepository.findAll(pageable);

        var dtoPage = PageResponseDTO.from(pageResult.map(p -> ProductResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .price(p.getPrice())
                .build()));

        return ResponseEntity.ok(ApiResponseDTO.success(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> get(@PathVariable("id") UUID id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        ProductResponseDTO resp = ProductResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .price(p.getPrice())
                .build();
        return ResponseEntity.ok(ApiResponseDTO.success(resp));
    }
}
