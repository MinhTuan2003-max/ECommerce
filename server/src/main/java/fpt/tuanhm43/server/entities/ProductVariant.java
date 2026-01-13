package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_sku", columnList = "sku"),
        @Index(name = "idx_variant_product", columnList = "product_id"),
        @Index(name = "idx_variant_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Size(max = 20, message = "Size must not exceed 20 characters")
    @Column(length = 20)
    private String size;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @Column(length = 50)
    private String color;

    @Size(max = 50, message = "Material must not exceed 50 characters")
    @Column(length = 50)
    private String material;

    @NotNull(message = "Price adjustment is required")
    @Column(name = "price_adjustment", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToOne(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Inventory inventory;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public BigDecimal getFinalPrice() {
        if (product == null) {
            return priceAdjustment;
        }
        return product.getBasePrice().add(priceAdjustment);
    }

    public boolean isInStock() {
        if (inventory == null) {
            return false;
        }
        return inventory.getQuantityAvailable() != null && inventory.getQuantityAvailable() > 0;
    }

}