package fpt.tuanhm43.server.dtos.order.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Detail Response DTO
 * With items and status history
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {

    private UUID id;
    private String orderNumber;
    private UUID trackingToken;
    private String trackingUrl;

    // Customer
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Shipping
    private String shippingAddress;
    private String notes;

    // Payment
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    // Status
    private OrderStatus status;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String currency;

    // Items
    private List<OrderItemResponse> items;
    private Integer totalItems;

    // Status history
    private List<OrderStatusHistoryResponse> statusHistory;

    // Actions
    private Boolean canCancel;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}