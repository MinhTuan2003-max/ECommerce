package fpt.tuanhm43.server.dtos.payment;

import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
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
public class PaymentTransactionDTO {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private String paymentDetails;
    private String failureReason;
    private String providerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

