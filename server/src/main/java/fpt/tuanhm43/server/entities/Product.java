package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_active", columnList = "is_active"),
        @Index(name = "idx_product_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Product slug is required")
    @Size(max = 200, message = "Slug must not exceed 200 characters")
    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    public ProductVariant getDefaultVariant() {
        return variants.isEmpty() ? null : variants.get(0);
    }

    public BigDecimal getMinPrice() {
        if (variants.isEmpty()) {
            return basePrice;
        }
        return variants.stream()
                .map(v -> basePrice.add(v.getPriceAdjustment()))
                .min(BigDecimal::compareTo)
                .orElse(basePrice);
    }

    public BigDecimal getMaxPrice() {
        if (variants.isEmpty()) {
            return basePrice;
        }
        return variants.stream()
                .map(v -> basePrice.add(v.getPriceAdjustment()))
                .max(BigDecimal::compareTo)
                .orElse(basePrice);
    }
}