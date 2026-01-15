package fpt.tuanhm43.server.specifications;

import fpt.tuanhm43.server.entities.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategorySpecification {

    private CategorySpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Category> buildSpecification(
            String name,
            String slug,
            Boolean isActive,
            UUID parentId,
            Boolean includeDeleted
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (includeDeleted == null || !includeDeleted) {
                predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));
            }

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (slug != null && !slug.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("slug"), slug));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            if (parentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("parent").get("id"), parentId));
            }

            if (query != null) {
                query.orderBy(
                        criteriaBuilder.asc(root.get("displayOrder")),
                        criteriaBuilder.asc(root.get("name"))
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Category> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isActive"));
    }

    public static Specification<Category> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("isDeleted"));
    }

}