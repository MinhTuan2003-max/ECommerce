package fpt.tuanhm43.server.dtos.category.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for updating a category
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCategoryRequest {

    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Parent category ID (null for root categories)
     */
    private UUID parentId;

    /**
     * Display order for sorting
     */
    private Integer displayOrder;

    /**
     * Active status
     */
    private Boolean isActive;
}