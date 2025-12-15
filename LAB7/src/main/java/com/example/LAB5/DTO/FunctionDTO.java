package com.example.LAB5.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class FunctionDTO {
    private Long id;
    private String name;
    private String expression;
    private String type; // "POLYNOMIAL", "TRIGONOMETRIC", "EXPONENTIAL", "CUSTOM"
    private String implementation; // "ARRAY", "LINKED_LIST"

    // Основные характеристики
    private int pointCount;
    private double leftBound;
    private double rightBound;
    private Double minY;
    private Double maxY;
    private Double averageY;

    // Информация о владельце
    private Long userId;
    private String username;

    // Временные метки
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Точки функции (опционально, загружаются по требованию)
    private List<PointDTO> points = new ArrayList<>();

    // Конструкторы
    public FunctionDTO() {}

    public FunctionDTO(Long id, String name, String expression, String type) {
        this.id = id;
        this.name = name;
        this.expression = expression;
        this.type = type;
    }

    public FunctionDTO(Long id, String name, String expression, String type,
                       int pointCount, double leftBound, double rightBound,
                       Long userId, String username) {
        this.id = id;
        this.name = name;
        this.expression = expression;
        this.type = type;
        this.pointCount = pointCount;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.userId = userId;
        this.username = username;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getImplementation() { return implementation; }
    public void setImplementation(String implementation) { this.implementation = implementation; }

    public int getPointCount() { return pointCount; }
    public void setPointCount(int pointCount) { this.pointCount = pointCount; }

    public double getLeftBound() { return leftBound; }
    public void setLeftBound(double leftBound) { this.leftBound = leftBound; }

    public double getRightBound() { return rightBound; }
    public void setRightBound(double rightBound) { this.rightBound = rightBound; }

    public Double getMinY() { return minY; }
    public void setMinY(Double minY) { this.minY = minY; }

    public Double getMaxY() { return maxY; }
    public void setMaxY(Double maxY) { this.maxY = maxY; }

    public Double getAverageY() { return averageY; }
    public void setAverageY(Double averageY) { this.averageY = averageY; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PointDTO> getPoints() { return points; }
    public void setPoints(List<PointDTO> points) { this.points = points; }

    // Вспомогательные методы
    public void addPoint(PointDTO point) {
        if (this.points == null) {
            this.points = new ArrayList<>();
        }
        this.points.add(point);
    }

    public boolean hasPoints() {
        return points != null && !points.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("FunctionDTO{id=%d, name='%s', type='%s', points=%d}",
                id, name, type, pointCount);
    }
}