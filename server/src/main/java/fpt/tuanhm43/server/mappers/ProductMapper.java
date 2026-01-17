package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.product.response.ProductDetailResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.entities.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.util.StringUtils;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "imageUrl", source = ".", qualifiedByName = "resolveDisplayImage")
    ProductResponse toResponse(Product product);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "inStock", expression = "java(product.getVariants() != null && !product.getVariants().isEmpty())")
    ProductDetailResponse toDetailResponse(Product product);


    @Named("resolveDisplayImage")
    default String resolveDisplayImage(Product product) {
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            return product.getVariants().stream()
                    .map(ProductVariant::getImageUrl)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(product.getImageUrl());
        }
        return product.getImageUrl();
    }
}