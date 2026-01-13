package fpt.tuanhm43.server.dtos.payment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * SePay Webhook Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SepayWebhookRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Status is required")
    private String status;

    private Map<String, Object> data;

    @NotBlank(message = "Signature is required")
    private String signature;
}
