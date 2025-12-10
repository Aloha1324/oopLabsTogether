package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.service.UserService;
import com.example.LAB5.framework.service.UserService.UserStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users — список пользователей
    @GetMapping
    public List<User> getAllUsers() {
        logger.info("GET /api/users – запрос списка пользователей");
        List<User> users = userService.getAllUsers();
        logger.debug("Найдено {} пользователей", users.size());
        return users;
    }

    // GET /api/users/{id} — один пользователь по id
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        logger.info("GET /api/users/{} – запрос пользователя по id", id);
        User user = userService.getUserByIdOrNull(id);
        if (user == null) {
            logger.warn("Пользователь с id {} не найден", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // POST /api/users — создать пользователя
    @PostMapping
    public ResponseEntity<User> createUser(@RequestParam String username,
                                           @RequestParam String password) {
        logger.info("POST /api/users – создание пользователя '{}'", username);
        User created = userService.createUser(username, password);
        logger.info("Пользователь '{}' создан с id={}", created.getUsername(), created.getId());
        return ResponseEntity.ok(created);
    }

    // PUT /api/users/{id} — изменить логин/пароль
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestParam String username,
                                           @RequestParam String password) {
        logger.info("PUT /api/users/{} – обновление пользователя", id);
        User updated = userService.updateUser(id, username, password);
        if (updated == null) {
            logger.warn("Пользователь с id {} не найден для обновления", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Пользователь с id {} успешно обновлён", id);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/users/{id} — удалить пользователя и все его функции/точки
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} – удаление пользователя и связанных данных", id);
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            logger.warn("Пользователь с id {} не найден для удаления", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Пользователь с id {} успешно удалён", id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/users/{id}/stats — статистика по пользователю (функции + точки)
    @GetMapping("/{id}/stats")
    public ResponseEntity<UserStatistics> getUserStats(@PathVariable Long id) {
        logger.info("GET /api/users/{}/stats – запрос статистики пользователя", id);
        UserStatistics stats = userService.getUserStatistics(id);
        if (stats == null) {
            logger.warn("Статистика для пользователя с id {} не найдена (пользователь отсутствует)", id);
            return ResponseEntity.notFound().build();
        }
        logger.debug("Статистика пользователя {}: functions={}, points={}",
                stats.getUsername(), stats.getFunctionsCount(), stats.getPointsCount());
        return ResponseEntity.ok(stats);
    }
}
