package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.product.ProductDTO;
import fpt.tuanhm43.server.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "isActive", ignore = true)
    ProductDTO toDTO(Product entity);

    @Mapping(target = "isActive", ignore = true)
    Product toEntity(ProductDTO dto);
}
