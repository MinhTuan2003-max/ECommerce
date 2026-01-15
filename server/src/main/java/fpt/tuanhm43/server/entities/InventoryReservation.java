package fpt.tuanhm43.server.entities;

import fpt.tuanhm43.server.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservations", indexes = {
        @Index(name = "idx_reservation_variant", columnList = "product_variant_id"),
        @Index(name = "idx_reservation_order", columnList = "order_id"),
        @Index(name = "idx_reservation_session", columnList = "session_id"),
        @Index(name = "idx_reservation_status", columnList = "status"),
        @Index(name = "idx_reservation_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventoryReservation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Size(max = 100, message = "Session ID must not exceed 100 characters")
    @Column(name = "session_id", length = 100)
    private String sessionId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Check if reservation is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if reservation is active
     */
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE && !isExpired();
    }

    /**
     * Check if can be released
     */
    public boolean canRelease() {
        return status == ReservationStatus.ACTIVE ||
                status == ReservationStatus.EXPIRED;
    }

    /**
     * Mark as completed
     */
    public void markCompleted() {
        this.status = ReservationStatus.COMPLETED;
    }

    /**
     * Mark as expired
     */
    public void markExpired() {
        this.status = ReservationStatus.EXPIRED;
    }

    /**
     * Mark as cancelled
     */
    public void markCancelled() {
        this.status = ReservationStatus.CANCELLED;
    }

}