package fpt.tuanhm43.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Role Entity
 * User roles (ADMIN, STAFF, CUSTOMER)
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {

    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Column(length = 200)
    private String description;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

}