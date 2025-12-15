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
    private String type;
    private String implementation;
    private int pointCount;
    private double leftBound;
    private double rightBound;
    private Double minY;
    private Double maxY;
    private Double averageY;
    private Long userId;
    private String username;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<PointDTO> points = new ArrayList<>();

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

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getExpression() { return expression; }
    public String getType() { return type; }
    public String getImplementation() { return implementation; }
    public int getPointCount() { return pointCount; }
    public double getLeftBound() { return leftBound; }
    public double getRightBound() { return rightBound; }
    public Double getMinY() { return minY; }
    public Double getMaxY() { return maxY; }
    public Double getAverageY() { return averageY; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<PointDTO> getPoints() { return points; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setExpression(String expression) { this.expression = expression; }
    public void setType(String type) { this.type = type; }
    public void setImplementation(String implementation) { this.implementation = implementation; }
    public void setPointCount(int pointCount) { this.pointCount = pointCount; }
    public void setLeftBound(double leftBound) { this.leftBound = leftBound; }
    public void setRightBound(double rightBound) { this.rightBound = rightBound; }
    public void setMinY(Double minY) { this.minY = minY; }
    public void setMaxY(Double maxY) { this.maxY = maxY; }
    public void setAverageY(Double averageY) { this.averageY = averageY; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setPoints(List<PointDTO> points) { this.points = points; }

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