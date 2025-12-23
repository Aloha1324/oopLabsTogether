// src/main/java/com/example/LAB5/framework/controller/FactoryController.java

package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.service.TabulatedFunctionFactoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/factory")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FactoryController {

    private final TabulatedFunctionFactoryProvider factoryProvider;

    @Autowired
    public FactoryController(TabulatedFunctionFactoryProvider factoryProvider) {
        this.factoryProvider = factoryProvider;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getFactoryType(Authentication auth) {
        String username = getCurrentUsername(auth);
        String type = factoryProvider.getCurrentTypeForUser(username);
        return ResponseEntity.ok(Map.of("type", type));
    }

    @PostMapping
    public ResponseEntity<?> setFactoryType(@RequestBody Map<String, String> request, Authentication auth) {
        String type = request.get("type");
        if (!"array".equals(type) && !"linked-list".equals(type)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Недопустимый тип фабрики: " + type));
        }

        String username = getCurrentUsername(auth);
        factoryProvider.setFactoryForUser(username, type);

        return ResponseEntity.ok(Map.of("type", type));
    }

    private String getCurrentUsername(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }
        return auth.getName();
    }
}