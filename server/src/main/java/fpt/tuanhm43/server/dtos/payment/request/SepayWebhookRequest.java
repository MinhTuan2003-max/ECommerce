package fpt.tuanhm43.server.dtos.payment.request;

import fpt.tuanhm43.server.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SepayWebhookRequest {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("message")
    private String message;
}

