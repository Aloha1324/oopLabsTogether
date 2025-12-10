package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    // GET /api/points — все точки
    @GetMapping
    public List<Point> getAllPoints() {
        logger.info("GET /api/points – запрос всех точек");
        List<Point> points = pointService.getAllPoints();
        logger.debug("Найдено {} точек", points.size());
        return points;
    }

    // GET /api/points/{id} — точка по id
    @GetMapping("/{id}")
    public ResponseEntity<Point> getPoint(@PathVariable Long id) {
        logger.info("GET /api/points/{} – запрос точки по id", id);
        return pointService.getPointById(id)
                .map(point -> {
                    logger.debug("Точка с id {} найдена", id);
                    return ResponseEntity.ok(point);
                })
                .orElseGet(() -> {
                    logger.warn("Точка с id {} не найдена", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // GET /api/points/by-function/{functionId} — точки функции
    @GetMapping("/by-function/{functionId}")
    public List<Point> getPointsByFunction(@PathVariable Long functionId) {
        logger.info("GET /api/points/by-function/{} – запрос точек функции", functionId);
        List<Point> points = pointService.getPointsByFunctionId(functionId);
        logger.debug("Для функции {} найдено {} точек", functionId, points.size());
        return points;
    }

    // GET /api/points/by-user/{userId} — точки пользователя
    @GetMapping("/by-user/{userId}")
    public List<Point> getPointsByUser(@PathVariable Long userId) {
        logger.info("GET /api/points/by-user/{} – запрос точек пользователя", userId);
        List<Point> points = pointService.getPointsByUserId(userId);
        logger.debug("Для пользователя {} найдено {} точек", userId, points.size());
        return points;
    }

    // POST /api/points — создать точку (functionId + userId + x + y)
    @PostMapping
    public ResponseEntity<Point> createPoint(@RequestParam Long functionId,
                                             @RequestParam Long userId,
                                             @RequestParam Double x,
                                             @RequestParam Double y) {
        logger.info("POST /api/points – создание точки: functionId={}, userId={}, x={}, y={}",
                functionId, userId, x, y);
        try {
            Point created = pointService.createPoint(functionId, userId, x, y);
            logger.info("Создана точка с id={} для функции {} и пользователя {}",
                    created.getId(), functionId, userId);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            logger.warn("Ошибка при создании точки: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/points/{id} — обновить x,y
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePoint(@PathVariable Long id,
                                            @RequestParam Double x,
                                            @RequestParam Double y) {
        logger.info("PUT /api/points/{} – обновление точки", id);
        boolean updated = pointService.updatePoint(id, x, y);
        if (!updated) {
            logger.warn("Точка с id {} не найдена для обновления", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Точка с id {} успешно обновлена", id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/points/{id} — удалить точку
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long id) {
        logger.info("DELETE /api/points/{} – удаление точки", id);
        boolean deleted = pointService.deletePoint(id);
        if (!deleted) {
            logger.warn("Точка с id {} не найдена для удаления", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Точка с id {} успешно удалена", id);
        return ResponseEntity.noContent().build();
    }
}
