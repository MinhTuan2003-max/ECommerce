package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.category.response.CategoryDetailResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryResponse;
import fpt.tuanhm43.server.dtos.category.response.CategoryTreeResponse;
import fpt.tuanhm43.server.entities.Category;
import fpt.tuanhm43.server.mappers.helper.CategoryMapperHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapperHelper.class})
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "productCount", source = "id", qualifiedByName = "countProducts")
    CategoryResponse toResponse(Category category);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "productCount", source = "id", qualifiedByName = "countProducts")
    @Mapping(target = "children", source = "children", qualifiedByName = "mapActiveChildren")
    CategoryDetailResponse toDetailResponse(Category category);

    @Mapping(target = "productCount", source = "id", qualifiedByName = "countProducts")
    @Mapping(target = "children", expression = "java(new java.util.ArrayList<>())")
    CategoryTreeResponse toTreeResponse(Category category);

    @Mapping(target = "productCount", source = "id", qualifiedByName = "countProducts")
    CategoryDetailResponse.CategoryChildResponse toChildResponse(Category child);

    @Named("mapActiveChildren")
    default List<CategoryDetailResponse.CategoryChildResponse> mapActiveChildren(List<Category> children) {
        if (children == null) return new ArrayList<>();
        return children.stream()
                .filter(child -> Boolean.FALSE.equals(child.getIsDeleted()))
                .map(this::toChildResponse)
                .toList();
    }
}