package com.example.LAB5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity // ← КРИТИЧЕСКИ ВАЖНО для @PreAuthorize
public class LAB5Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(LAB5Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(LAB5Application.class, args);
    }
}
