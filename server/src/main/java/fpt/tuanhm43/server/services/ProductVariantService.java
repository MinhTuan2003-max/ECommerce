package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.product.request.CreateProductVariantRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductVariantResponse;

import java.util.List;
import java.util.UUID;

public interface ProductVariantService {

    /**
     * Creates a new product variant and associates it with a parent product.
     *
     * @param productId The UUID of the parent product to which the variant belongs.
     * @param request The data transfer object containing variant details like SKU, size,
     * color, and price adjustment.
     * @return A {@link ProductVariantResponse} containing the created variant's details
     */
    ProductVariantResponse createVariant(UUID productId, CreateProductVariantRequest request);

    /**
     * Retrieves all variants associated with a specific product.
     *
     * @param productId The UUID of the product whose variants are to be retrieved.
     * @return A list of {@link ProductVariantResponse} objects representing all
     * variants of the product
     */
    List<ProductVariantResponse> getVariantsByProductId(UUID productId);

}
