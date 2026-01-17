package fpt.tuanhm43.server.mappers.helper;

import fpt.tuanhm43.server.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryMapperHelper {

    private final CategoryRepository categoryRepository;

    @Named("countProducts")
    public long countProducts(UUID categoryId) {
        return categoryRepository.countProductsByCategoryId(categoryId);
    }
}