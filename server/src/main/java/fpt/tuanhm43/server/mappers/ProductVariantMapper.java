package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.product.ProductVariantDTO;
import fpt.tuanhm43.server.entities.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(target = "isActive", ignore = true)
    ProductVariantDTO toDTO(ProductVariant entity);

    @Mapping(source = "productId", target = "product.id")
    @Mapping(target = "isActive", ignore = true)
    ProductVariant toEntity(ProductVariantDTO dto);
}
