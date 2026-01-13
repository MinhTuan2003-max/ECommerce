package fpt.tuanhm43.server.dtos.payment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fpt.tuanhm43.server.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment Status Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentStatusResponse {

    private String transactionId;
    private PaymentStatus status;
    private String message;
    private String qrCodeUrl;
    private String paymentUrl;
}