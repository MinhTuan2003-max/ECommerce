package fpt.tuanhm43.server.dtos.category.request;

import lombok.*;

import java.util.UUID;

/**
 * Request DTO for dynamic category filtering
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFilterRequest {

    /**
     * Filter by category name (partial match, case-insensitive)
     */
    private String name;

    /**
     * Filter by slug (exact match)
     */
    private String slug;

    /**
     * Filter by active status
     */
    private Boolean isActive;

    /**
     * Filter by parent category ID (null for root categories)
     */
    private UUID parentId;

    /**
     * Include soft-deleted categories (admin only)
     */
    private Boolean includeDeleted;

    /**
     * Page number (0-based)
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * Page size
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * Sort field
     */
    @Builder.Default
    private String sortBy = "displayOrder";

    /**
     * Sort direction (ASC/DESC)
     */
    @Builder.Default
    private String sortDirection = "ASC";
}