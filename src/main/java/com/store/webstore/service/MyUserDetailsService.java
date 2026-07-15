package com.store.webstore.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.store.webstore.repository.UserRepository;

import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.store.webstore.model.User user = userRepository.findByEmail(com.store.webstore.model.User.normalizeEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));

        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isActive())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }

}
