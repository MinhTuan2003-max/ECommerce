package fpt.tuanhm43.server.dtos.order.request;

import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Order Filter Request DTO
 * For admin order list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFilterRequest {

    private String orderNumber;
    private String customerEmail;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Min(value = 0, message = "Page cannot be negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;

    private String sortBy; // created_at, total_amount
    private String sortDirection; // asc, desc
}