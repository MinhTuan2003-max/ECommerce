package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Role Repository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find by name
     */
    Optional<Role> findByName(String name);

    /**
     * Check if name exists
     */
    boolean existsByName(String name);
}
