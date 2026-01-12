package fpt.tuanhm43.server.dtos.order;

import fpt.tuanhm43.server.enums.OrderStatus;
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
public class OrderDTO {
    private UUID id;
    private UUID userId;
    private BigDecimal totalAmount;
    private String currency;
    private OrderStatus status;
    private String trackingId;
    private String shippingAddress;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

