package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "inventories", uniqueConstraints = @UniqueConstraint(columnNames = {"product_variant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;
}
