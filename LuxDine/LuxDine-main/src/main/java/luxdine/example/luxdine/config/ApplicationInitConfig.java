package luxdine.example.luxdine.config;

import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.enums.Role;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class ApplicationInitConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            log.info("Initializing sample users...");
            createSampleUsers();
            log.info("Sample users initialization completed");
        };
    }

    private void createSampleUsers() {
        // Create admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .isActive(true)
                    .password(passwordEncoder.encode("admin"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created with password 'admin'");
        }

        // Create staff user
        if (userRepository.findByUsername("staff").isEmpty()) {
            User staff = User.builder()
                    .username("staff")
                    .isActive(true)
                    .password(passwordEncoder.encode("staff"))
                    .role(Role.STAFF)
                    .build();
            userRepository.save(staff);
            log.info("Staff user created with password 'staff'");
        }

        // Create customer user
        if (userRepository.findByUsername("customer").isEmpty()) {
            User customer = User.builder()
                    .username("customer")
                    .firstName("John")
                    .lastName("Smith")
                    .email("john.smith@email.com")
                    .phoneNumber("+1 (555) 123-4567")
                    .isActive(true)
                    .password(passwordEncoder.encode("customer"))
                    .role(Role.CUSTOMER)
                    .build();
            userRepository.save(customer);
            log.info("Customer user created with password 'customer'");
        }
    }
}
