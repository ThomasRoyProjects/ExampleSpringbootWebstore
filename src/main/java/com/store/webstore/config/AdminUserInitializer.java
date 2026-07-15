package com.store.webstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.store.webstore.model.User;
import com.store.webstore.repository.UserRepository;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapEnabled;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap-enabled:false}") boolean bootstrapEnabled,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEnabled = bootstrapEnabled;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!bootstrapEnabled) {
            return;
        }

        String email = User.normalizeEmail(adminEmail);
        String password = adminPassword == null ? "" : adminPassword;
        if (email == null || email.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap requires nonblank app.admin.email and app.admin.password when enabled.");
        }

        if (userRepository.findByEmail(email).isEmpty()) {
            User adminUser = new User();
            adminUser.setEmail(email);
            adminUser.setPassword(passwordEncoder.encode(password));
            adminUser.setRole(User.ROLE_ADMIN);
            adminUser.setActive(true);
            userRepository.save(adminUser);
        }
    }
}
