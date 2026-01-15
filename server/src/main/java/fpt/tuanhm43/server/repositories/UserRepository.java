package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

}

