package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.order.request.CreateOrderRequest;
import fpt.tuanhm43.server.dtos.order.response.OrderDetailResponse;
import fpt.tuanhm43.server.dtos.order.response.OrderItemResponse;
import fpt.tuanhm43.server.dtos.order.response.OrderResponse;
import fpt.tuanhm43.server.dtos.order.response.OrderStatusHistoryResponse;
import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.entities.OrderItem;
import fpt.tuanhm43.server.entities.OrderStatusHistory;
import fpt.tuanhm43.server.mappers.config.MapStructConfig;
import org.mapstruct.*;

import java.util.List;

/**
 * Order Mapper
 */
@Mapper(config = MapStructConfig.class)
public interface OrderMapper {

    /**
     * Order to Response DTO
     */
    @Mapping(target = "trackingUrl", expression = "java(getTrackingUrl(order))")
    @Mapping(target = "totalItems", expression = "java(order.getTotalItemsCount())")
    OrderResponse toResponse(Order order);

    /**
     * Order to Detail Response DTO
     */
    @Mapping(target = "trackingUrl", expression = "java(getTrackingUrl(order))")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalItems", expression = "java(order.getTotalItemsCount())")
    @Mapping(target = "statusHistory", source = "statusHistory")
    @Mapping(target = "canCancel", expression = "java(order.canCancel())")
    OrderDetailResponse toDetailResponse(Order order);

    /**
     * Create Request to Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true) // Generated in service
    @Mapping(target = "trackingToken", ignore = true) // Auto-generated
    @Mapping(target = "user", ignore = true) // Set in service if logged in
    @Mapping(target = "status", ignore = true) // Set to PENDING
    @Mapping(target = "paymentStatus", ignore = true) // Set to PENDING
    @Mapping(target = "totalAmount", ignore = true) // Calculated
    @Mapping(target = "subtotal", ignore = true) // Calculated
    @Mapping(target = "shippingFee", constant = "0")
    @Mapping(target = "currency", constant = "VND")
    @Mapping(target = "items", ignore = true) // Set from cart
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "paymentTransactions", ignore = true)
    @Mapping(target = "inventoryReservations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Order toEntity(CreateOrderRequest request);

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(target = "variantId", source = "productVariant.id")
    @Mapping(target = "sku", source = "productVariant.sku")
    @Mapping(target = "productName", source = "productVariant.product.name")
    @Mapping(target = "size", source = "productVariant.size")
    @Mapping(target = "color", source = "productVariant.color")
    @Mapping(target = "imageUrl", source = "productVariant.imageUrl")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    OrderStatusHistoryResponse toStatusHistoryResponse(OrderStatusHistory history);

    List<OrderStatusHistoryResponse> toStatusHistoryResponseList(List<OrderStatusHistory> histories);

    default String getTrackingUrl(Order order) {
        if (order.getTrackingToken() == null) {
            return null;
        }
        return "/api/v1/orders/track/" + order.getTrackingToken();
    }
}