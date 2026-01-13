package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.cart.response.CartItemResponse;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;
import fpt.tuanhm43.server.entities.CartItem;
import fpt.tuanhm43.server.entities.ShoppingCart;
import fpt.tuanhm43.server.mappers.config.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Cart Mapper
 */
@Mapper(config = MapStructConfig.class)
public interface CartMapper {

    /**
     * Shopping Cart to Response
     */
    @Mapping(target = "items", source = "items")
    @Mapping(target = "hasStockIssues", expression = "java(!cart.validateStock())")
    @Mapping(target = "message", ignore = true)
    CartResponse toResponse(ShoppingCart cart);

    /**
     * Cart Item to Response
     */
    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "variantId", source = "productVariant.id")
    @Mapping(target = "sku", source = "productVariant.sku")
    @Mapping(target = "productName", source = "productVariant.product.name")
    @Mapping(target = "size", source = "productVariant.size")
    @Mapping(target = "color", source = "productVariant.color")
    @Mapping(target = "imageUrl", source = "productVariant.imageUrl")
    @Mapping(target = "availableStock", expression = "java(item.getAvailableStock())")
    @Mapping(target = "inStock", expression = "java(item.isAvailableInStock())")
    @Mapping(target = "stockMessage", expression = "java(getStockMessage(item))")
    @Mapping(target = "priceChanged", expression = "java(item.hasPriceChanged())")
    @Mapping(target = "currentPrice", expression = "java(getCurrentPrice(item))")
    CartItemResponse toItemResponse(CartItem item);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);

    default String getStockMessage(CartItem item) {
        Integer available = item.getAvailableStock();
        Integer quantity = item.getQuantity();

        if (available == null || available == 0) {
            return "Out of stock";
        } else if (quantity > available) {
            return String.format("Only %d item(s) available", available);
        } else if (available < 5) {
            return String.format("Only %d left in stock", available);
        } else {
            return "In stock";
        }
    }

    default java.math.BigDecimal getCurrentPrice(CartItem item) {
        if (item.getProductVariant() == null) {
            return null;
        }
        return item.getProductVariant().getFinalPrice();
    }
}