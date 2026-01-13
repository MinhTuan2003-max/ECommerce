package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "inventories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_variant_id"}),
        indexes = {
                @Index(name = "idx_inventory_variant", columnList = "product_variant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Inventory extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false, unique = true)
    private ProductVariant productVariant;

    @NotNull(message = "Quantity available is required")
    @Min(value = 0, message = "Quantity available cannot be negative")
    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @NotNull(message = "Quantity reserved is required")
    @Min(value = 0, message = "Quantity reserved cannot be negative")
    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 5;

    public Integer getTotalStock() {
        return quantityAvailable + quantityReserved;
    }

    public boolean isInStock() {
        return quantityAvailable > 0;
    }

    public boolean isLowStock() {
        return quantityAvailable > 0 && quantityAvailable <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return quantityAvailable == 0;
    }

    public boolean canFulfill(Integer requestedQuantity) {
        return quantityAvailable >= requestedQuantity;
    }

    public void reserve(Integer quantity) {
        if (!canFulfill(quantity)) {
            throw new IllegalStateException(
                    String.format("Cannot reserve %d units. Only %d available",
                            quantity, quantityAvailable)
            );
        }
        this.quantityAvailable -= quantity;
        this.quantityReserved += quantity;
    }

    public void releaseReservation(Integer quantity) {
        if (quantityReserved < quantity) {
            throw new IllegalStateException(
                    String.format("Cannot release %d units. Only %d reserved",
                            quantity, quantityReserved)
            );
        }
        this.quantityReserved -= quantity;
        this.quantityAvailable += quantity;
    }

    public void deductReserved(Integer quantity) {
        if (quantityReserved < quantity) {
            throw new IllegalStateException(
                    String.format("Cannot deduct %d units. Only %d reserved",
                            quantity, quantityReserved)
            );
        }
        this.quantityReserved -= quantity;
    }

    public void addStock(Integer quantity) {
        this.quantityAvailable += quantity;
    }

    public String getStockStatus() {
        if (isOutOfStock()) {
            return "Out of stock";
        } else if (isLowStock()) {
            return String.format("Only %d left in stock", quantityAvailable);
        } else {
            return "In stock";
        }
    }
}