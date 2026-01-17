package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.product.request.CreateProductVariantRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductVariantResponse;
import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.mappers.ProductVariantMapper;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.services.ProductSearchService;
import fpt.tuanhm43.server.services.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductSearchService productSearchService;

    private final ProductVariantMapper variantMapper;

    @Override
    public ProductVariantResponse createVariant(UUID productId, CreateProductVariantRequest request) {
        log.info("Creating variant for product: {}, SKU: {}", productId, request.getSku());

        if (variantRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU " + request.getSku() + " already exists");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .size(request.getSize())
                .color(request.getColor())
                .material(request.getMaterial())
                .priceAdjustment(request.getPriceAdjustment())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();

        Inventory inventory = Inventory.builder()
                .productVariant(variant)
                .quantityAvailable(request.getInitialQuantity() != null ? request.getInitialQuantity() : 0)
                .quantityReserved(0)
                .build();

        variant.setInventory(inventory);

        ProductVariant savedVariant = variantRepository.save(variant);

        productSearchService.syncToElasticsearch(productId);

        return variantMapper.toResponse(savedVariant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProductId(UUID productId) {
        log.debug("Getting variants for product: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        return variantMapper.toResponseList(variants);
    }

}