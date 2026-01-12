package fpt.tuanhm43.server.dtos.inventory;

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
public class InventoryDTO {
    private UUID id;
    private UUID productVariantId;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

