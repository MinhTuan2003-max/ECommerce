package fpt.tuanhm43.server.dtos.order.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fpt.tuanhm43.server.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order Status History Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusHistoryResponse {

    private UUID id;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime createdAt;
}