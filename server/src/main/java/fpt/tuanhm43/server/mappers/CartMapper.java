package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.cart.response.CartItemResponse;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;
import fpt.tuanhm43.server.entities.CartItem;
import fpt.tuanhm43.server.entities.ShoppingCart;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    CartResponse toCartResponse(ShoppingCart cart);

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "variantId", source = "productVariant.id")
    @Mapping(target = "sku", source = "productVariant.sku")
    @Mapping(target = "productName", source = "productVariant.product.name")
    @Mapping(target = "size", source = "productVariant.size")
    @Mapping(target = "color", source = "productVariant.color")
    @Mapping(target = "imageUrl", source = "productVariant.product.imageUrl")
    @Mapping(target = "currentPrice", source = "productVariant.finalPrice")
    CartItemResponse toCartItemResponse(CartItem item);

    @AfterMapping
    default void postMapping(@MappingTarget CartItemResponse response) {
        response.updateStockMessage();
    }
}