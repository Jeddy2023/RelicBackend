package com.example.relicv2.util;

import com.example.relicv2.enums.Role;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seed() {
        try {
            seedAdminUser();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void seedAdminUser() {
        Boolean adminUser = userRepository.existsByEmail("admin@gmail.com");

        if (!adminUser) {
            User user = new User();
            user.setFirstname("Admin");
            user.setLastname("User");
            user.setEmail("admin@gmail.com");
            user.setUsername("admin");
            user.setPhonenumber("09046807203");
            user.setPassword(passwordEncoder.encode("@Password123"));
            user.setRole(Role.ADMIN);
            user.setVerified(true);
            user.setBlocked(false);
            userRepository.save(user);
            logger.info("Admin user seeded");
        } else {
            logger.info("Admin user already exists");
        }
    }
}
