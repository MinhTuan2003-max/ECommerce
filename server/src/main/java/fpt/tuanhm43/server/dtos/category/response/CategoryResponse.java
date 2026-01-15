package fpt.tuanhm43.server.dtos.category.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for category basic information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private UUID id;

    private String name;

    private String slug;

    private String description;

    private UUID parentId;

    private String parentName;

    private Integer displayOrder;

    private Boolean isActive;

    private Long productCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}