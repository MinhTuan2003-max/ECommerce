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
    private String material; // Cotton, Polyester (optional)

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
        return inventory != null && inventory.getQuantityAvailable() > 0;
    }

    public Integer getAvailableStock() {
        return inventory != null ? inventory.getQuantityAvailable() : 0;
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (product != null) {
            sb.append(product.getName());
        }
        if (size != null) {
            sb.append(" - Size ").append(size);
        }
        if (color != null) {
            sb.append(" - ").append(color);
        }
        return sb.toString();
    }

    public String getAttributesString() {
        StringBuilder sb = new StringBuilder();
        if (size != null) {
            sb.append("Size: ").append(size);
        }
        if (color != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("Color: ").append(color);
        }
        if (material != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("Material: ").append(material);
        }
        return sb.toString();
    }
}