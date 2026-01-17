package fpt.tuanhm43.server.entities;

import fpt.tuanhm43.server.enums.OrderStatus;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "order_number"),
        @Index(name = "idx_order_tracking", columnList = "tracking_token"),
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_email", columnList = "customer_email"),
        @Index(name = "idx_order_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseEntity {

    @NotBlank(message = "Order number is required")
    @Size(max = 20, message = "Order number must not exceed 20 characters")
    @Column(name = "order_number", unique = true, nullable = false, length = 20)
    private String orderNumber;

    @NotNull
    @Column(name = "tracking_token", unique = true, nullable = false)
    private UUID trackingToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^(\\+84|0)\\d{9}$", message = "Invalid phone number format")
    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod; // COD, SEPAY

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "Shipping fee cannot be negative")
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotBlank
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentTransaction> paymentTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InventoryReservation> inventoryReservations = new ArrayList<>();

    /**
     * Add item to order
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Add status history entry
     */
    public void addStatusHistory(OrderStatusHistory history) {
        statusHistory.add(history);
        history.setOrder(this);
    }

    /**
     * Check if order can be cancelled
     */
    public boolean canCancel() {
        return status == OrderStatus.PENDING ||
                status == OrderStatus.CONFIRMED ||
                (status == OrderStatus.PAID && paymentMethod == PaymentMethod.COD);
    }

    /**
     * Check if status transition is valid
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (this.status == null) return true;
        return this.status.canTransitionTo(newStatus);
    }

    /**
     * Update status with validation
     */
    public void updateStatus(OrderStatus newStatus, String changedBy, String reason) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", status, newStatus)
            );
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .fromStatus(this.status)
                .toStatus(newStatus)
                .changedBy(changedBy)
                .reason(reason)
                .build();

        this.status = newStatus;
        addStatusHistory(history);
    }

    /**
     * Calculate totals
     */
    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal.add(shippingFee != null ? shippingFee : BigDecimal.ZERO);
    }

    /**
     * Get total items count
     */
    public Integer getTotalItemsCount() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Is payment completed
     */
    public boolean isPaymentCompleted() {
        return paymentStatus == PaymentStatus.PAID;
    }

    /**
     * Mark as paid
     */
    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.PAID;
    }
}