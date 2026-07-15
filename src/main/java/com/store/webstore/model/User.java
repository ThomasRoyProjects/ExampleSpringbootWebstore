package com.store.webstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Locale;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User {
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private String role = ROLE_CUSTOMER;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || role.isBlank()) {
            this.role = ROLE_CUSTOMER;
            return;
        }
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        this.role = normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole;
    }
}
