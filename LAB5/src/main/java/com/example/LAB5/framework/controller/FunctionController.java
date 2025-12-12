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
        // Тестовые данные - используем изменяемые Maps!
        addFunction("sin(x)", "TRIGONOMETRIC", 1);
        addFunction("x^2", "POLYNOMIAL", 2);
        addFunction("e^x", "EXPONENTIAL", 1);
    }

    private void addFunction(String name, String type, Integer userId) {
        int id = idCounter.getAndIncrement();
        // Используем HashMap вместо Map.of() для изменяемости
        Map<String, Object> function = new HashMap<>();
        function.put("id", id);
        function.put("name", name);
        function.put("type", type);
        function.put("userId", userId);

        functions.put(id, function);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFunctions() {
        return ResponseEntity.ok(new ArrayList<>(functions.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFunctionById(@PathVariable Integer id) {
        Map<String, Object> function = functions.get(id);
        if (function == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new HashMap<>(function)); // Возвращаем копию
    }

    @PostMapping
    public ResponseEntity<?> createFunction(@RequestBody Map<String, Object> funcRequest) {
        try {
            Integer newId = idCounter.getAndIncrement();

            // Безопасное получение значений
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

            // Создаем изменяемую Map
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
        if (!functions.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Получаем существующую функцию
            Map<String, Object> function = functions.get(id);

            // Создаем новую Map с обновлениями
            Map<String, Object> updatedFunction = new HashMap<>(function);

            // Применяем обновления, но не позволяем менять id
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (!"id".equals(entry.getKey())) { // Защищаем id от изменения
                    updatedFunction.put(entry.getKey(), entry.getValue());
                }
            }

            // Обновляем в хранилище
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
            if (!functions.containsKey(id)) {
                return ResponseEntity.notFound().build();
            }

            // Простое и быстрое удаление
            functions.remove(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Логируем ошибку, но возвращаем 500
            System.err.println("Error deleting function " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}