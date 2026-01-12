package fpt.tuanhm43.server.dtos.order.response;

import fpt.tuanhm43.server.dtos.order.OrderItemDTO;
import fpt.tuanhm43.server.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal totalAmount;
    private String currency;
    private OrderStatus status;
    private String trackingId;
    private String shippingAddress;
    private String notes;
    private Set<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
