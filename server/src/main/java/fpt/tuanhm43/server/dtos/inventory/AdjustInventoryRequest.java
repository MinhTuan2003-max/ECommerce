package fpt.tuanhm43.server.dtos.inventory;

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
public class AdjustInventoryRequest {
    @NotBlank(message = "Product variant ID is required")
    private String productVariantId;

    @Min(value = 0, message = "Quantity available must be non-negative")
    private Integer quantityAvailable;

    @Min(value = 0, message = "Quantity reserved must be non-negative")
    private Integer quantityReserved;
}

