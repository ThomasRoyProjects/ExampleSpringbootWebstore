package com.store.webstore.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.store.webstore.model.User;
import com.store.webstore.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        String email = User.normalizeEmail(registrationForm.getEmail());
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email already exists.");
            return "register";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registrationForm.getPassword()));
        user.setRole(User.ROLE_CUSTOMER);
        user.setActive(true);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", "Email already exists.");
            return "register";
        }

        return "redirect:/login?registered=true";
    }

    public static class RegistrationForm {
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 254, message = "Email must be 254 characters or fewer.")
        private String email;

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = User.normalizeEmail(email);
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
