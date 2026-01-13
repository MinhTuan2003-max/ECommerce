package fpt.tuanhm43.server.entities;

import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentTransaction extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;

    @NotBlank(message = "Provider name is required")
    @Size(max = 50, message = "Provider name must not exceed 50 characters")
    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Type(JsonType.class)
    @Column(name = "provider_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> providerData = new HashMap<>();

    @Size(max = 500, message = "Failure reason must not exceed 500 characters")
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    /**
     * Mark as completed
     */
    public void markAsCompleted() {
        this.status = PaymentStatus.PAID;
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    /**
     * Check if payment is completed
     */
    public boolean isCompleted() {
        return status == PaymentStatus.PAID;
    }

    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING;
    }

    /**
     * Check if payment is failed
     */
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    /**
     * Add provider data
     */
    public void addProviderData(String key, Object value) {
        if (providerData == null) {
            providerData = new HashMap<>();
        }
        providerData.put(key, value);
    }

    /**
     * Get provider data
     */
    public Object getProviderData(String key) {
        if (providerData == null) {
            return null;
        }
        return providerData.get(key);
    }
}