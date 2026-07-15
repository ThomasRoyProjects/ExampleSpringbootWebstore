package com.store.webstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.store.webstore.service.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MyUserDetailsService userDetailsService;

    public SecurityConfig(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();

        SimpleUrlAuthenticationSuccessHandler adminSuccessHandler =
                new SimpleUrlAuthenticationSuccessHandler("/admin/products");
        adminSuccessHandler.setAlwaysUseDefaultTargetUrl(true);

        UsernamePasswordAuthenticationFilter adminLoginFilter = new UsernamePasswordAuthenticationFilter(authenticationManager);
        adminLoginFilter.setRequiresAuthenticationRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/admin/login"));
        adminLoginFilter.setUsernameParameter("email");
        adminLoginFilter.setAuthenticationSuccessHandler(adminSuccessHandler);
        adminLoginFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/admin/login?error"));
        adminLoginFilter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/register",
                    "/login",
                    "/admin/login",
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/static/**",
                    "/webjars/**",
                    "/images/**",
                    "/actuator/health",
                    "/actuator/health/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/products", true)
                .usernameParameter("email")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
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
            )
            .addFilterBefore(adminLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
