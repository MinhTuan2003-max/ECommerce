package fpt.tuanhm43.server.dtos.order.request;

import fpt.tuanhm43.server.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {
    @NotNull(message = "Order status is required")
    private OrderStatus status;

    private String reason;

    private String changedBy;
}

