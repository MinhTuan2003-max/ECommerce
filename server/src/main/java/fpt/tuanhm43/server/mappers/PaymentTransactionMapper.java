package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.payment.response.PaymentResponse;
import fpt.tuanhm43.server.entities.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Payment Mapper
 */
@Mapper(config = MapStructConfig.class)
public interface PaymentTransactionMapper {

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toResponse(PaymentTransaction payment);

    List<PaymentResponse> toResponseList(List<PaymentTransaction> payments);
}