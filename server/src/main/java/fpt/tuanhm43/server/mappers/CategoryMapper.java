package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.category.CategoryDTO;
import fpt.tuanhm43.server.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(source = "parent.id", target = "parentId")
    CategoryDTO toDTO(Category entity);

    @Mapping(source = "parentId", target = "parent.id")
    Category toEntity(CategoryDTO dto);
}