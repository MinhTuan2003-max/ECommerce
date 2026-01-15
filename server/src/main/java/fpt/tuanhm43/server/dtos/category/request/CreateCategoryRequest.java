package fpt.tuanhm43.server.dtos.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for creating a new category
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    private String slug;

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
     * Active status (default: true)
     */
    @Builder.Default
    private Boolean isActive = true;
}