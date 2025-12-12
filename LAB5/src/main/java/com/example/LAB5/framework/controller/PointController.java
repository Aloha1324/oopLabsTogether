package com.example.LAB5.framework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/points")
public class PointController {

    private final ConcurrentHashMap<Integer, Map<String, Object>> points = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public PointController() {
        // Тестовые данные - используем изменяемые Maps!
        addPoint(1.0, 2.0, 1);
        addPoint(2.5, 3.5, 2);
    }

    private void addPoint(double x, double y, int functionId) {
        int id = idCounter.getAndIncrement();
        // Используем HashMap вместо Map.of() для изменяемости
        Map<String, Object> point = new HashMap<>();
        point.put("id", id);
        point.put("x", x);
        point.put("y", y);
        point.put("result", x + y);
        point.put("functionId", functionId);

        points.put(id, point);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllPoints() {
        return ResponseEntity.ok(new ArrayList<>(points.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPointById(@PathVariable Integer id) {
        Map<String, Object> point = points.get(id);
        if (point == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new HashMap<>(point)); // Возвращаем копию
    }

    @PostMapping
    public ResponseEntity<?> createPoint(@RequestBody Map<String, Object> pointRequest) {
        try {
            Integer newId = idCounter.getAndIncrement();

            // Безопасное получение значений
            Object xObj = pointRequest.get("x");
            Object yObj = pointRequest.get("y");
            Object functionIdObj = pointRequest.get("functionId");

            if (xObj == null || yObj == null || functionIdObj == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Missing required fields: x, y, functionId")
                );
            }

            double x = Double.parseDouble(xObj.toString());
            double y = Double.parseDouble(yObj.toString());
            int functionId = Integer.parseInt(functionIdObj.toString());

            // Создаем изменяемую Map
            Map<String, Object> newPoint = new HashMap<>();
            newPoint.put("id", newId);
            newPoint.put("x", x);
            newPoint.put("y", y);
            newPoint.put("result", x + y);
            newPoint.put("functionId", functionId);

            points.put(newId, newPoint);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPoint);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid number format", "message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePoint(@PathVariable Integer id,
                                         @RequestBody Map<String, Object> updates) {
        if (!points.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Получаем существующую точку
            Map<String, Object> point = points.get(id);

            // Создаем новую Map с обновлениями
            Map<String, Object> updatedPoint = new HashMap<>(point);

            // Применяем обновления, но не позволяем менять id
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (!"id".equals(entry.getKey())) { // Защищаем id от изменения
                    updatedPoint.put(entry.getKey(), entry.getValue());
                }
            }

            // Обновляем в хранилище
            points.put(id, updatedPoint);
            return ResponseEntity.ok(updatedPoint);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Failed to update point", "message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Integer id) {
        try {
            if (!points.containsKey(id)) {
                return ResponseEntity.notFound().build();
            }

            // Простое и быстрое удаление
            points.remove(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Логируем ошибку, но возвращаем 500
            System.err.println("Error deleting point " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}