package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String attributes;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
