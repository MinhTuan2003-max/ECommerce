package fpt.tuanhm43.server.dtos.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotBlank(message = "Tracking ID is required")
    private String trackingId;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String notes;

    @Builder.Default
    private String currency = "USD";

    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        private String productVariantId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        private BigDecimal unitPrice;
    }
}
