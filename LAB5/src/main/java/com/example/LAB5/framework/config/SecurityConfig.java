package com.example.LAB5.framework.config;

import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Конфигурация безопасности: Basic Auth + роли");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // регистрацию пользователя можно оставить публичной
                        .requestMatchers("/api/users", "/api/users/**").permitAll()
                        // все остальное требует аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            log.info("Попытка аутентификации пользователя {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Пользователь {} не найден", username);
                        return new UsernameNotFoundException("User not found: " + username);
                    });

            String role = user.getRole() == null ? "USER" : user.getRole();
            log.info("Пользователь {} найден, роль {}", username, role);

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(role) // превращает "ADMIN" -> ROLE_ADMIN
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // В учебном проекте можно так, в реальном нужен BCryptPasswordEncoder
        return NoOpPasswordEncoder.getInstance();
    }
}
