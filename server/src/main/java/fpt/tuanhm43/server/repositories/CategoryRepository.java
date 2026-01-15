package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Category Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

}
