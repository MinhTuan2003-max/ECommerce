package fpt.tuanhm43.server.dtos.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private UUID id;
    private UUID orderId;
    private UUID productVariantId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

