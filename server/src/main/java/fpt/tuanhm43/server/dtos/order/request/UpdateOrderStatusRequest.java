package fpt.tuanhm43.server.dtos.order.request;

import fpt.tuanhm43.server.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Order Status Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    private String changedBy;
}