package fpt.tuanhm43.server.dtos.product.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product Filter Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterRequest {

    private UUID categoryId;

    private String keyword; // Search by name

    @Min(value = 0, message = "Min price cannot be negative")
    private BigDecimal minPrice;

    @Min(value = 0, message = "Max price cannot be negative")
    private BigDecimal maxPrice;

    private Boolean isActive;

    private String sortBy; // price_asc, price_desc, newest

    @Min(value = 0, message = "Page cannot be negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;
}