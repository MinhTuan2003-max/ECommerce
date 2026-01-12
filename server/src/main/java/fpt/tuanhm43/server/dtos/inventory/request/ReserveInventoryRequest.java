package fpt.tuanhm43.server.dtos.inventory.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryRequest {
    @NotBlank(message = "Product variant ID is required")
    private String productVariantId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

