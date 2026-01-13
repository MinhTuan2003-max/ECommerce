package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_variant_id"}),
        indexes = {
                @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
                @Index(name = "idx_cart_item_variant", columnList = "product_variant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CartItem extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cart;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Calculate subtotal
     */
    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    /**
     * Update quantity and recalculate
     */
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        calculateSubtotal();
        if (cart != null) {
            cart.recalculateTotals();
        }
    }

    /**
     * Check if item is available in stock
     */
    public boolean isAvailableInStock() {
        if (productVariant == null || productVariant.getInventory() == null) {
            return false;
        }
        return productVariant.getInventory().getQuantityAvailable() >= quantity;
    }

    /**
     * Get available stock
     */
    public Integer getAvailableStock() {
        if (productVariant == null || productVariant.getInventory() == null) {
            return 0;
        }
        return productVariant.getInventory().getQuantityAvailable();
    }

    /**
     * Check if price changed since added to cart
     */
    public boolean hasPriceChanged() {
        if (productVariant == null) {
            return false;
        }
        BigDecimal currentPrice = productVariant.getFinalPrice();
        return currentPrice.compareTo(unitPrice) != 0;
    }

    /**
     * Get price difference
     */
    public BigDecimal getPriceDifference() {
        if (productVariant == null) {
            return BigDecimal.ZERO;
        }
        return productVariant.getFinalPrice().subtract(unitPrice);
    }
}