package fpt.tuanhm43.server.dtos.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new product variant")
public class CreateProductVariantRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    @Schema(description = "Unique SKU for the variant", example = "NIKE-AJ1-CHI-42")
    private String sku;

    @Size(max = 20, message = "Size must not exceed 20 characters")
    @Schema(description = "Shoe size", example = "42")
    private String size;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @Schema(description = "Color variant", example = "Chicago Red")
    private String color;

    @Size(max = 50, message = "Material must not exceed 50 characters")
    @Schema(description = "Material of the variant", example = "Leather")
    private String material;

    @NotNull(message = "Price adjustment is required")
    @Schema(description = "Price difference relative to the base price", example = "500000")
    private BigDecimal priceAdjustment;

    @Schema(description = "Specific image URL for this variant", example = "https://example.com/aj1-42.jpg")
    private String imageUrl;
}