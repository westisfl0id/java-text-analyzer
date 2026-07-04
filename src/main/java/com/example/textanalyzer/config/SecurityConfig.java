package com.example.textanalyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures HTTP Basic authentication for the REST API.
 *
 * <p>User credentials are provided through application properties so Docker Compose and local
 * profiles can run without manual environment setup.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Protects every endpoint and disables CSRF because the service exposes a stateless REST API.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(@NonNull HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Creates two in-memory users for the homework API.
     */
    @Bean
    public UserDetailsService userDetailsService(
            @NonNull PasswordEncoder passwordEncoder,
            @Value("${text-analyzer.security.user.username}") String userUsername,
            @Value("${text-analyzer.security.user.password}") String userPassword,
            @Value("${text-analyzer.security.admin.username}") String adminUsername,
            @Value("${text-analyzer.security.admin.password}") String adminPassword
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(userUsername)
                        .password(passwordEncoder.encode(userPassword))
                        .roles("USER")
                        .build(),

                User.withUsername(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build()
        );
    }

    /**
     * Hashes in-memory user passwords before they are passed to Spring Security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}