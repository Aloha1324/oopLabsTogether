package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.service.TabulatedFunctionFactoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, String>> getFactoryType() {
        return ResponseEntity.ok(Map.of("type", factoryProvider.getCurrentType()));
    }

    @PostMapping
    public ResponseEntity<?> setFactoryType(@RequestBody Map<String, String> request) {
        String type = request.get("type");
        if (!"array".equals(type) && !"linked-list".equals(type)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Недопустимый тип фабрики: " + type));
        }
        factoryProvider.setFactoryType(type);
        return ResponseEntity.ok(Map.of("type", type));
    }
}