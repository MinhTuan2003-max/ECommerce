package fpt.tuanhm43.server.dtos.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private UUID id;
    private UUID userId;
    private UUID productVariantId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

