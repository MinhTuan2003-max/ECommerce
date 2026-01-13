package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_slug", columnList = "slug"),
        @Index(name = "idx_category_parent", columnList = "parent_id"),
        @Index(name = "idx_category_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Category extends BaseEntity {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;

}