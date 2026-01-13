package fpt.tuanhm43.server.dtos.product.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Product Detail Response DTO
 * Includes variants
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private Boolean isActive;

    // Category
    private UUID categoryId;
    private String categoryName;

    // Variants
    private List<ProductVariantResponse> variants;

    // Price range
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Stock
    private Boolean inStock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
