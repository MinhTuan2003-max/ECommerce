package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.order.OrderItemDTO;
import fpt.tuanhm43.server.entities.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "productVariant.id", target = "productVariantId")
    OrderItemDTO toDTO(OrderItem entity);

    @Mapping(source = "orderId", target = "order.id")
    @Mapping(source = "productVariantId", target = "productVariant.id")
    OrderItem toEntity(OrderItemDTO dto);
}
