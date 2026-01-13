package fpt.tuanhm43.server.dtos.product.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal minPrice; // Lowest variant price
    private BigDecimal maxPrice; // Highest variant price
    private String imageUrl;
    private Boolean isActive;

    // Category info
    private UUID categoryId;
    private String categoryName;
    private String categorySlug;

    // Variant count
    private Integer variantCount;

    // Stock info (aggregated from variants)
    private Boolean inStock;
    private Integer totalStock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}