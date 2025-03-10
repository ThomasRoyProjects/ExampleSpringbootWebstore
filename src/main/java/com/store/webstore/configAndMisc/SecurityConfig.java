package com.store.webstore.configAndMisc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.store.webstore.Services.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**", "/admin-login", "/admin-products")  
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin-login", "/css/**", "/js/**").permitAll() 
                .requestMatchers("/admin/products/add").hasAuthority("ROLE_ADMIN") 
                .requestMatchers("/add-products").hasAuthority("ROLE_ADMIN") 
                .requestMatchers("/admin-products").authenticated()  

                .anyRequest().authenticated()  
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/admin-login")
                .defaultSuccessUrl("/admin-products", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin-logout")
                .logoutSuccessUrl("/admin-login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/admin/products/add", 
                    "/admin/uploadImage", 
                    "/admin-products",  
                    "/css/**", "/js/**", "/images/**", "/product/image/**", "/uploads/*" 
                )
            );

        return http.build();
    }

    @Bean
    SecurityFilterChain userSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/login", "/register", "/uploadImage", "/cart/add", 
                    "/main", "/images/**", "/product/image/**", "/", 
                    "/product/**", "/products", "/static/**", "/uploads/*", "/admin-products"
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/register", "/login", "/css/**", "/js/**", "/images/**", 
                    "/product/image/**", "/product/**", "/products", "/static/**", 
                    "/main", "/", "/uploads/*", "/admin-products"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/main")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/403")
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> 
                    csp.policyDirectives("script-src 'self'; object-src 'none'; img-src 'self' https: data:;")
                )
            );

        return http.build();
    }

    public MyUserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
