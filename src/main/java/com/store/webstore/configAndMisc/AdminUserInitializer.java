package com.store.webstore.configAndMisc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.store.webstore.Models.User;
import com.store.webstore.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("adminpass"));
            adminUser.setRole("ROLE_ADMIN"); 
            userRepository.save(adminUser);
        } //change setUsername and passwordEncoder values to set admin panel password

        userRepository.findAll().forEach(user -> {
            if (user.getRole() == null || !((String) user.getRole()).startsWith("ROLE_")) {
                user.setRole("ROLE_CUSTOMER"); 
                userRepository.save(user);
            }
        });
    }
}
