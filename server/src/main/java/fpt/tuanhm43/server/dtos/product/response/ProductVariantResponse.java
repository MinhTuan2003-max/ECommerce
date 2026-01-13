package fpt.tuanhm43.server.dtos.product.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product Variant Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariantResponse {

    private UUID id;
    private UUID productId;
    private String sku;
    private String size;
    private String color;
    private String material;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice;
    private String imageUrl;
    private Boolean isActive;

    // Stock info
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Boolean inStock;
    private String stockStatus; // "In stock", "Low stock", "Out of stock"

    // Product info (for standalone variant display)
    private String productName;
}
