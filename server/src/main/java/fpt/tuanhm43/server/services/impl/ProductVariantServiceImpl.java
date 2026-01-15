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
import fpt.tuanhm43.server.services.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper variantMapper;

    @Override
    public ProductVariantResponse createVariant(UUID productId, CreateProductVariantRequest request) {
        if (variantRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU " + request.getSku() + " đã tồn tại!");
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
                .quantityAvailable(0)
                .quantityReserved(0)
                .build();

        variant.setInventory(inventory);

        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProductId(UUID productId) {
        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        return variantMapper.toResponseList(variants);
    }
}