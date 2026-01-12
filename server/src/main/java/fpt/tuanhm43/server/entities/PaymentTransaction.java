package fpt.tuanhm43.server.entities;

import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(unique = true, nullable = false)
    private String transactionId;

    @Column(columnDefinition = "TEXT")
    private String paymentDetails;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(nullable = false)
    private String providerName;
}
