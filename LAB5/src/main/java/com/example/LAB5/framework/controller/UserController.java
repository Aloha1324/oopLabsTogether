package com.example.LAB5.framework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ConcurrentHashMap<Integer, Map<String, Object>> users = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    // Конструктор с тестовыми данными
    public UserController() {
        // Инициализация тестовыми данными - используем изменяемые Maps!
        addUser("admin", "admin@example.com", "ADMIN");
        addUser("user1", "user1@example.com", "USER");
        addUser("user2", "user2@example.com", "USER");
    }

    private void addUser(String login, String email, String role) {
        int id = idCounter.getAndIncrement();
        // Используем HashMap вместо Map.of() для изменяемости
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("login", login);
        user.put("email", email);
        user.put("role", role);

        users.put(id, user);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(new ArrayList<>(users.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Integer id) {
        Map<String, Object> user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new HashMap<>(user)); // Возвращаем копию
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userRequest) {
        try {
            Integer newId = idCounter.getAndIncrement();

            // Безопасное получение значений
            Object loginObj = userRequest.get("login");
            Object emailObj = userRequest.get("email");
            Object roleObj = userRequest.get("role");

            if (loginObj == null || emailObj == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Missing required fields: login, email")
                );
            }

            String login = loginObj.toString();
            String email = emailObj.toString();
            String role = roleObj != null ? roleObj.toString() : "USER";

            // Создаем изменяемую Map
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("id", newId);
            newUser.put("login", login);
            newUser.put("email", email);
            newUser.put("role", role);

            users.put(newId, newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid request data", "message", e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> updates) {
        if (!users.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Получаем существующего пользователя
            Map<String, Object> user = users.get(id);

            // Создаем новую Map с обновлениями
            Map<String, Object> updatedUser = new HashMap<>(user);

            // Применяем обновления, но не позволяем менять id
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (!"id".equals(entry.getKey())) { // Защищаем id от изменения
                    updatedUser.put(entry.getKey(), entry.getValue());
                }
            }

            // Обновляем в хранилище
            users.put(id, updatedUser);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Failed to update user", "message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        try {
            if (!users.containsKey(id)) {
                return ResponseEntity.notFound().build();
            }

            // Простое и быстрое удаление
            users.remove(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Логируем ошибку, но возвращаем 500
            System.err.println("Error deleting user " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}