package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    // GET /api/points
    @GetMapping
    public List<Point> getAllPoints() {
        return pointService.getAllPoints();
    }

    // GET /api/points/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Point> getPoint(@PathVariable Long id) {
        return pointService.getPointById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/points/by-function/{functionId}
    @GetMapping("/by-function/{functionId}")
    public List<Point> getPointsByFunction(@PathVariable Long functionId) {
        return pointService.getPointsByFunctionId(functionId);
    }

    // GET /api/points/by-user/{userId}
    @GetMapping("/by-user/{userId}")
    public List<Point> getPointsByUser(@PathVariable Long userId) {
        return pointService.getPointsByUserId(userId);
    }

    // POST /api/points
    @PostMapping
    public ResponseEntity<Point> createPoint(@RequestParam Long functionId,
                                             @RequestParam Long userId,
                                             @RequestParam Double x,
                                             @RequestParam Double y) {
        Point point = pointService.createPoint(functionId, userId, x, y);
        return ResponseEntity.ok(point);
    }

    // PUT /api/points/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePoint(@PathVariable Long id,
                                            @RequestParam Double x,
                                            @RequestParam Double y) {
        boolean updated = pointService.updatePoint(id, x, y);
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // DELETE /api/points/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long id) {
        boolean deleted = pointService.deletePoint(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}