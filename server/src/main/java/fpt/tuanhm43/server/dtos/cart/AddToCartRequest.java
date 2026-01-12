package fpt.tuanhm43.server.dtos.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity = 1;
}

