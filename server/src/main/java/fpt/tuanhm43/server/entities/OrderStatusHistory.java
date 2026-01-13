package fpt.tuanhm43.server.entities;

import fpt.tuanhm43.server.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_status_histories", indexes = {
        @Index(name = "idx_status_history_order", columnList = "order_id"),
        @Index(name = "idx_status_history_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderStatusHistory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private OrderStatus fromStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private OrderStatus toStatus;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String reason;

    @NotBlank(message = "Changed by is required")
    @Size(max = 100, message = "Changed by must not exceed 100 characters")
    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    /**
     * Check if this is initial status
     */
    public boolean isInitialStatus() {
        return fromStatus == null;
    }

    /**
     * Get status change description
     */
    public String getChangeDescription() {
        if (isInitialStatus()) {
            return String.format("Order created with status: %s", toStatus);
        }
        return String.format("Status changed from %s to %s", fromStatus, toStatus);
    }
}