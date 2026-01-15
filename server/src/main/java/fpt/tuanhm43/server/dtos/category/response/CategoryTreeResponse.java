package fpt.tuanhm43.server.dtos.category.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for hierarchical category tree
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeResponse {

    private UUID id;

    private String name;

    private String slug;

    private String description;

    private Integer displayOrder;

    private Boolean isActive;

    private Long productCount;

    @Builder.Default
    private List<CategoryTreeResponse> children = new ArrayList<>();

    /**
     * Recursively add child to tree
     */
    public void addChild(CategoryTreeResponse child) {
        this.children.add(child);
    }
}