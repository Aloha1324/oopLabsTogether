package com.example.LAB5.framework.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.service.UserService;
import com.example.LAB5.framework.service.UserService.UserStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // Только ADMIN может видеть всех пользователей
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("ADMIN запросил всех пользователей");
        List<User> users = userService.getAllUsers();
        logger.debug("Возвращено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    // Любой аутентифицированный USER может видеть себя
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        // Username берется из SecurityContext (установлен JwtAuthenticationFilter)
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        logger.debug("Пользователь {} запросил свои данные", username);

        userService.findByUsername(username)
                .ifPresentOrElse(
                        user -> logger.info("Данные пользователя {} возвращены", username),
                        () -> logger.warn("Пользователь {} не найден в БД", username)
                );

        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Только ADMIN может создавать пользователей
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User userRequest) {
        logger.info("ADMIN создает пользователя: {}", userRequest.getUsername());

        try {
            // UserService уже использует BCrypt через passwordEncoder
            User createdUser = userService.createUser(
                    userRequest.getUsername(),
                    userRequest.getPassword()
            );
            logger.info("Создан пользователь ID={} {}", createdUser.getId(), createdUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка создания: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Любой аутентифицированный может обновить себя (но не роль)
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        logger.info("Пользователь {} пытается обновить ID={}", currentUsername, id);

        // Только свои данные или ADMIN
        if (!currentUsername.equals(userService.getUserByUsername(currentUsername).get().getUsername())
                && !"ADMIN".equals(userService.getUserByUsername(currentUsername).get().getRole())) {
            return ResponseEntity.status(403).build();
        }

        User updated = userService.updateUser(id, userUpdate.getUsername(), userUpdate.getPassword());
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    // Только ADMIN может удалять
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("ADMIN удаляет пользователя ID={}", id);

        boolean deleted = userService.deleteUser(id);
        return deleted ?
                ResponseEntity.noContent().build() :
                ResponseEntity.notFound().build();
    }

    // Статистика только для своих данных или ADMIN
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
    @GetMapping("/{id}/stats")
    public ResponseEntity<UserStatistics> getUserStats(@PathVariable Long userId) {
        logger.debug("Запрос статистики для ID={}", userId);
        UserStatistics stats = userService.getUserStatistics(userId);
        return stats != null ?
                ResponseEntity.ok(stats) :
                ResponseEntity.notFound().build();
    }
}
