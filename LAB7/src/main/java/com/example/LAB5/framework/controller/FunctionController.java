package com.example.LAB5.framework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/functions")
public class FunctionController {

    private final ConcurrentHashMap<Integer, Map<String, Object>> functions = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public FunctionController() {
        initializeDefaultData();
    }

    private void addFunction(String name, String type, Integer userId) {
        int id = idCounter.getAndIncrement();
        Map<String, Object> function = new HashMap<>();
        function.put("id", id);
        function.put("name", name);
        function.put("type", type);
        function.put("userId", userId);
        functions.put(id, function);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFunctions() {
        if (functions.isEmpty()) {
            initializeDefaultData();
        }
        return ResponseEntity.ok(new ArrayList<>(functions.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFunctionById(@PathVariable Integer id) {
        if (functions.isEmpty()) {
            initializeDefaultData();
        }

        Map<String, Object> function = functions.get(id);
        if (function == null) {
            Map<String, Object> defaultFunction = createDefaultFunction(id);
            functions.put(id, defaultFunction);
            return ResponseEntity.ok(defaultFunction);
        }
        return ResponseEntity.ok(new HashMap<>(function));
    }

    @PostMapping
    public ResponseEntity<?> createFunction(@RequestBody Map<String, Object> funcRequest) {
        try {
            Integer newId = idCounter.getAndIncrement();

            Object nameObj = funcRequest.get("name");
            Object typeObj = funcRequest.get("type");
            Object userIdObj = funcRequest.get("userId");

            if (nameObj == null || typeObj == null || userIdObj == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Missing required fields: name, type, userId")
                );
            }

            String name = nameObj.toString();
            String type = typeObj.toString();
            int userId = Integer.parseInt(userIdObj.toString());

            Map<String, Object> newFunction = new HashMap<>();
            newFunction.put("id", newId);
            newFunction.put("name", name);
            newFunction.put("type", type);
            newFunction.put("userId", userId);

            functions.put(newId, newFunction);
            return ResponseEntity.status(HttpStatus.CREATED).body(newFunction);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid number format for userId", "message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFunction(@PathVariable Integer id,
                                            @RequestBody Map<String, Object> updates) {
        if (functions.isEmpty()) {
            initializeDefaultData();
        }

        Map<String, Object> function = functions.get(id);
        if (function == null) {
            function = createDefaultFunction(id);
            functions.put(id, function);
        }

        try {
            Map<String, Object> updatedFunction = new HashMap<>(function);

            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (!"id".equals(entry.getKey())) {
                    updatedFunction.put(entry.getKey(), entry.getValue());
                }
            }

            functions.put(id, updatedFunction);
            return ResponseEntity.ok(updatedFunction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Failed to update function", "message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Integer id) {
        try {
            if (functions.isEmpty()) {
                initializeDefaultData();
            }

            if (!functions.containsKey(id)) {
                Map<String, Object> function = createDefaultFunction(id);
                functions.put(id, function);
            }

            functions.remove(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Map<String, Object> createDefaultFunction(Integer id) {
        Map<String, Object> function = new HashMap<>();
        function.put("id", id);
        function.put("name", "default_function_" + id);
        function.put("type", "POLYNOMIAL");
        function.put("userId", 1);
        return function;
    }

    private void initializeDefaultData() {
        functions.clear();
        idCounter.set(1);

        addFunction("quadratic", "POLYNOMIAL", 1);
        addFunction("sin(x)", "TRIGONOMETRIC", 1);
        addFunction("x^2", "POLYNOMIAL", 2);
        addFunction("e^x", "EXPONENTIAL", 1);
    }
}