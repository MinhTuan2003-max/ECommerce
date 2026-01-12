package fpt.tuanhm43.server.dtos.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private UUID parentId;

    @JsonProperty("isActive")
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

