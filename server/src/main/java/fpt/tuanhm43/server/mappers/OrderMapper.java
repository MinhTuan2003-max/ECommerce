package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.order.OrderDTO;
import fpt.tuanhm43.server.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "user.id", target = "userId")
    OrderDTO toDTO(Order entity);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    Order toEntity(OrderDTO dto);
}