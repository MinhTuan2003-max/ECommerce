package fpt.tuanhm43.server.dtos.order.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Order Tracking Response DTO
 * For guest order tracking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderTrackingResponse {

    private OrderDetailResponse order;
    private String message;
    private Boolean found;
}