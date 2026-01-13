package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Find by username or email (for login)
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Find user with roles (fetch join)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    /**
     * Find user with roles by username
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Find enabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    java.util.List<User> findEnabledUsers();

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRole(@Param("roleName") String roleName);
}

