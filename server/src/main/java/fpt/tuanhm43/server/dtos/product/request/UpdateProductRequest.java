package fpt.tuanhm43.server.dtos.product.request;

import jakarta.validation.constraints.DecimalMin;
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
public class UpdateProductRequest {
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    private String slug;

    @Size(max = 2000, message = "Product description must not exceed 2000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    private BigDecimal price;

    private boolean isActive;
}

