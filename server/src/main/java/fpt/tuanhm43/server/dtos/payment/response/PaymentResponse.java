package fpt.tuanhm43.server.dtos.payment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private String transactionId;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String providerName;
    private String failureReason;
    private LocalDateTime createdAt;
}