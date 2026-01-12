package fpt.tuanhm43.server.mappers;

import fpt.tuanhm43.server.dtos.user.UserDTO;
import fpt.tuanhm43.server.entities.Role;
import fpt.tuanhm43.server.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToStrings")
    UserDTO toDTO(User entity);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "orders", ignore = true)
    User toEntity(UserDTO dto);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}