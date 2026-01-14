package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.product.response.ProductVariantResponse;
import fpt.tuanhm43.server.entities.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface ProductVariantMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "finalPrice", expression = "java(variant.getFinalPrice())")
    @Mapping(target = "quantityAvailable", source = "inventory.quantityAvailable")
    @Mapping(target = "quantityReserved", source = "inventory.quantityReserved")
    @Mapping(target = "inStock", expression = "java(variant.isInStock())")
    @Mapping(target = "stockStatus", expression = "java(getStockStatus(variant))")
    ProductVariantResponse toResponse(ProductVariant variant);

    List<ProductVariantResponse> toResponseList(List<ProductVariant> variants);

    default String getStockStatus(ProductVariant variant) {
        if (variant.getInventory() == null) {
            return "Out of stock";
        }
        return variant.getInventory().getStockStatus();
    }
}