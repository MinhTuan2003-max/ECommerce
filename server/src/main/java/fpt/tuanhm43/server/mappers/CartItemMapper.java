package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.cart.CartItemDTO;
import fpt.tuanhm43.server.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "productVariant.id", target = "productVariantId")
    CartItemDTO toDTO(CartItem entity);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "productVariantId", target = "productVariant.id")
    CartItem toEntity(CartItemDTO dto);
}
