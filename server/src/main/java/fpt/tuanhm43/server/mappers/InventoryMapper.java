package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.inventory.InventoryDTO;
import fpt.tuanhm43.server.entities.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mapping(source = "productVariant.id", target = "productVariantId")
    InventoryDTO toDTO(Inventory entity);

    @Mapping(source = "productVariantId", target = "productVariant.id")
    Inventory toEntity(InventoryDTO dto);
}
