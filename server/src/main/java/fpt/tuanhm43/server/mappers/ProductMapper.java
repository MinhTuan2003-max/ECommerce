package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.product.request.CreateProductRequest;
import fpt.tuanhm43.server.dtos.product.request.UpdateProductRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductDetailResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.dtos.product.response.ProductVariantResponse;
import fpt.tuanhm43.server.entities.Product;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.mappers.config.MapStructConfig;
import org.mapstruct.*;

import java.util.List;

/**
 * Product Mapper
 * MapStruct auto-implementation
 */
@Mapper(config = MapStructConfig.class, uses = {ProductVariantMapper.class})
public interface ProductMapper {

    /**
     * Product Entity to Response DTO
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "variantCount", expression = "java(getVariantCount(product))")
    @Mapping(target = "minPrice", expression = "java(product.getMinPrice())")
    @Mapping(target = "maxPrice", expression = "java(product.getMaxPrice())")
    @Mapping(target = "inStock", expression = "java(isProductInStock(product))")
    @Mapping(target = "totalStock", expression = "java(getTotalStock(product))")
    ProductResponse toResponse(Product product);

    /**
     * Product Entity to Detail Response
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "variants", source = "variants")
    @Mapping(target = "minPrice", expression = "java(product.getMinPrice())")
    @Mapping(target = "maxPrice", expression = "java(product.getMaxPrice())")
    @Mapping(target = "inStock", expression = "java(isProductInStock(product))")
    ProductDetailResponse toDetailResponse(Product product);

    /**
     * Create Request to Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Product toEntity(CreateProductRequest request);

    /**
     * Update entity from request
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    default Integer getVariantCount(Product product) {
        return product.getVariants() != null ? product.getVariants().size() : 0;
    }

    default Boolean isProductInStock(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return false;
        }
        return product.getVariants().stream()
                .anyMatch(variant -> variant.getInventory() != null &&
                        variant.getInventory().getQuantityAvailable() > 0);
    }

    default Integer getTotalStock(Product product) {
        if (product.getVariants() == null) {
            return 0;
        }
        return product.getVariants().stream()
                .filter(v -> v.getInventory() != null)
                .mapToInt(v -> v.getInventory().getQuantityAvailable())
                .sum();
    }
}