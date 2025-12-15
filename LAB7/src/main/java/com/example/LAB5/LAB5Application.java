package com.example.LAB5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class LAB5Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // Этот метод вызывается Tomcat при деплое WAR
        return builder.sources(LAB5Application.class);
    }

    public static void main(String[] args) {
        // Этот метод работает только при запуске из JAR
        SpringApplication.run(LAB5Application.class, args);
    }
}