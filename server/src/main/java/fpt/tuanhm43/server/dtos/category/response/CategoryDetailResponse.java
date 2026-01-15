package fpt.tuanhm43.server.dtos.category.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for category with children
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDetailResponse {

    private UUID id;

    private String name;

    private String slug;

    private String description;

    private UUID parentId;

    private String parentName;

    private Integer displayOrder;

    private Boolean isActive;

    private Long productCount;

    @Builder.Default
    private List<CategoryChildResponse> children = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Simple DTO for child categories
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryChildResponse {
        private UUID id;
        private String name;
        private String slug;
        private Integer displayOrder;
        private Boolean isActive;
        private Long productCount;
    }
}