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
@Builder
public class ProductVariantResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String sku;
    private String size;
    private String color;
    private String material;
    private BigDecimal originalPrice;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice;
    private String imageUrl;
    private Boolean isActive;
    private Integer quantity;
}
