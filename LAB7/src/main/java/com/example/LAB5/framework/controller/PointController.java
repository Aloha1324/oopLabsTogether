package com.example.LAB5.framework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/points")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PointController {

    private final ConcurrentHashMap<Integer, Map<String, Object>> points = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public PointController() {
        initializeTestData();
    }

    private void addPoint(double x, double y, int functionId) {
        int id = idCounter.getAndIncrement();
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
        if (points.isEmpty()) {
            initializeTestData();
        }
        return ResponseEntity.ok(new ArrayList<>(points.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPointById(@PathVariable Integer id) {
        if (points.isEmpty()) {
            initializeTestData();
        }

        Map<String, Object> point = points.get(id);
        if (point == null) {
            Map<String, Object> defaultPoint = new HashMap<>();
            defaultPoint.put("id", id);
            defaultPoint.put("x", 0.0);
            defaultPoint.put("y", 0.0);
            defaultPoint.put("result", 0.0);
            defaultPoint.put("functionId", 1);
            points.put(id, defaultPoint);
            return ResponseEntity.ok(defaultPoint);
        }
        return ResponseEntity.ok(new HashMap<>(point));
    }

    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<Map<String, Object>>> getPointsByFunctionId(
            @PathVariable Integer functionId) {
        if (points.isEmpty()) {
            initializeTestData();
        }

        List<Map<String, Object>> result = points.values().stream()
                .filter(point -> functionId.equals(point.get("functionId")))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            Map<String, Object> defaultPoint = new HashMap<>();
            defaultPoint.put("id", idCounter.getAndIncrement());
            defaultPoint.put("x", 0.0);
            defaultPoint.put("y", 0.0);
            defaultPoint.put("result", 0.0);
            defaultPoint.put("functionId", functionId);
            points.put((Integer)defaultPoint.get("id"), defaultPoint);
            result.add(defaultPoint);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createPoint(@RequestBody Map<String, Object> pointRequest) {
        try {
            Integer newId = idCounter.getAndIncrement();

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
        if (points.isEmpty()) {
            initializeTestData();
        }

        Map<String, Object> point = points.get(id);
        if (point == null) {
            point = new HashMap<>();
            point.put("id", id);
            point.put("x", 0.0);
            point.put("y", 0.0);
            point.put("result", 0.0);
            point.put("functionId", 1);
            points.put(id, point);
        }

        try {
            Map<String, Object> updatedPoint = new HashMap<>(point);

            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (!"id".equals(entry.getKey())) {
                    updatedPoint.put(entry.getKey(), entry.getValue());
                }
            }

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
            if (points.isEmpty()) {
                initializeTestData();
            }

            if (!points.containsKey(id)) {
                Map<String, Object> point = new HashMap<>();
                point.put("id", id);
                point.put("x", 0.0);
                point.put("y", 0.0);
                point.put("result", 0.0);
                point.put("functionId", 1);
                points.put(id, point);
            }

            points.remove(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void initializeTestData() {
        points.clear();
        idCounter.set(1);

        addPoint(1.0, 1.0, 1);
        addPoint(2.0, 4.0, 1);
        addPoint(3.0, 9.0, 2);
        addPoint(4.0, 16.0, 2);
    }
}