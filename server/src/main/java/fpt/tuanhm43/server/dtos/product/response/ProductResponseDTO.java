package fpt.tuanhm43.server.dtos.product.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
}

