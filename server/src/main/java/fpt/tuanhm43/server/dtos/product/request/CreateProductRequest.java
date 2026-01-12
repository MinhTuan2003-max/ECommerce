package fpt.tuanhm43.server.dtos.product.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Product slug is required")
    private String slug;

    @Size(max = 2000, message = "Product description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    private BigDecimal price;

    @Builder.Default
    private boolean isActive = true;
}
