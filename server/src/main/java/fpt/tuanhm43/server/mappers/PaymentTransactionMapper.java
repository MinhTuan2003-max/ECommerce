package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.payment.PaymentTransactionDTO;
import fpt.tuanhm43.server.entities.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {
    @Mapping(source = "order.id", target = "orderId")
    PaymentTransactionDTO toDTO(PaymentTransaction entity);

    @Mapping(source = "orderId", target = "order.id")
    PaymentTransaction toEntity(PaymentTransactionDTO dto);
}
