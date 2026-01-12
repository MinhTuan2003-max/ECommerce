package fpt.tuanhm43.server.config;

import fpt.tuanhm43.server.entities.Role;
import fpt.tuanhm43.server.entities.User;
import fpt.tuanhm43.server.repositories.user.RoleRepository;
import fpt.tuanhm43.server.repositories.user.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin123!}")
    private String adminPassword;

    @PostConstruct
    public void init() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        if (!userRepository.existsByUsername(adminUsername) && !userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .enabled(true)
                    .build();
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole);
            userRepository.save(admin);
            log.info("Created default admin user: {}", adminUsername);
        } else {
            log.info("Admin user already exists or email taken");
        }
    }
}

