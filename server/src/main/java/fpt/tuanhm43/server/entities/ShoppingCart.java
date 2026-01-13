package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_carts", indexes = {
        @Index(name = "idx_cart_session", columnList = "session_id"),
        @Index(name = "idx_cart_user", columnList = "user_id"),
        @Index(name = "idx_cart_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShoppingCart extends BaseEntity {

    @Size(max = 100, message = "Session ID must not exceed 100 characters")
    @Column(name = "session_id", unique = true, length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Min(value = 0)
    @Column(name = "total_items", nullable = false)
    @Builder.Default
    private Integer totalItems = 0;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Add item to cart
     */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        recalculateTotals();
    }

    /**
     * Remove item from cart
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        recalculateTotals();
    }

    /**
     * Clear all items
     */
    public void clear() {
        items.clear();
        recalculateTotals();
    }

    /**
     * Find item by variant ID
     */
    public CartItem findItemByVariantId(UUID variantId) {
        return items.stream()
                .filter(item -> item.getProductVariant().getId().equals(variantId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if cart has items
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * Check if cart is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Extend expiration time
     */
    public void extendExpiration(int hours) {
        this.expiresAt = LocalDateTime.now().plusHours(hours);
    }

    /**
     * Recalculate totals
     */
    public void recalculateTotals() {
        this.totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        this.totalAmount = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validate stock availability for all items
     */
    public boolean validateStock() {
        return items.stream()
                .allMatch(item -> {
                    Inventory inventory = item.getProductVariant().getInventory();
                    if (inventory == null) return false;
                    return inventory.getQuantityAvailable() >= item.getQuantity();
                });
    }

    /**
     * Get items with stock issues
     */
    public List<CartItem> getItemsWithStockIssues() {
        return items.stream()
                .filter(item -> {
                    Inventory inventory = item.getProductVariant().getInventory();
                    if (inventory == null) return true;
                    return inventory.getQuantityAvailable() < item.getQuantity();
                })
                .toList();
    }

    /**
     * Is guest cart
     */
    public boolean isGuestCart() {
        return user == null && sessionId != null;
    }

    /**
     * Is user cart
     */
    public boolean isUserCart() {
        return user != null;
    }
}