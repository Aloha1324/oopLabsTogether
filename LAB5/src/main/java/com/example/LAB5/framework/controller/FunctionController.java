package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    private final FunctionService functionService;
    private final UserService userService;

    public FunctionController(FunctionService functionService, UserService userService) {
        this.functionService = functionService;
        this.userService = userService;
    }

    // GET /api/functions — все функции
    @GetMapping
    public List<Function> getAllFunctions() {
        logger.info("GET /api/functions – запрос всех функций");
        List<Function> functions = functionService.getAllFunctions();
        logger.debug("Найдено {} функций", functions.size());
        return functions;
    }

    // GET /api/functions/{id} — функция по id
    @GetMapping("/{id}")
    public ResponseEntity<Function> getFunction(@PathVariable Long id) {
        logger.info("GET /api/functions/{} – запрос функции по id", id);
        Function function = functionService.getFunctionByIdOrNull(id);
        if (function == null) {
            logger.warn("Функция с id {} не найдена", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(function);
    }

    // GET /api/functions/by-user/{userId} — функции конкретного пользователя
    @GetMapping("/by-user/{userId}")
    public List<Function> getFunctionsByUser(@PathVariable Long userId) {
        logger.info("GET /api/functions/by-user/{} – запрос функций пользователя", userId);
        List<Function> functions = functionService.getFunctionsByUserId(userId);
        logger.debug("Для пользователя {} найдено {} функций", userId, functions.size());
        return functions;
    }

    // POST /api/functions — создать функцию для пользователя
    @PostMapping
    public ResponseEntity<Function> createFunction(@RequestParam Long userId,
                                                   @RequestParam String name,
                                                   @RequestParam String expression) {
        logger.info("POST /api/functions – создание функции '{}' для пользователя {}", name, userId);
        User user = userService.getUserByIdOrNull(userId);
        if (user == null) {
            logger.warn("Невозможно создать функцию – пользователь {} не найден", userId);
            return ResponseEntity.badRequest().build();
        }
        Function created = functionService.createFunction(user, name, expression);
        logger.info("Создана функция '{}' с id={} для пользователя {}", created.getName(), created.getId(), userId);
        return ResponseEntity.ok(created);
    }

    // PUT /api/functions/{id} — обновить имя/выражение
    @PutMapping("/{id}")
    public ResponseEntity<Function> updateFunction(@PathVariable Long id,
                                                   @RequestParam String name,
                                                   @RequestParam String expression) {
        logger.info("PUT /api/functions/{} – обновление функции", id);
        Function updated = functionService.updateFunction(id, name, expression);
        if (updated == null) {
            logger.warn("Функция с id {} не найдена для обновления", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Функция с id {} успешно обновлена", id);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/functions/{id} — удалить функцию и её точки
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        logger.info("DELETE /api/functions/{} – удаление функции и её точек", id);
        boolean deleted = functionService.deleteFunction(id);
        if (!deleted) {
            logger.warn("Функция с id {} не найдена для удаления", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Функция с id {} успешно удалена", id);
        return ResponseEntity.noContent().build();
    }
}
