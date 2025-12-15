package com.example.LAB5.DTO.Request;

import java.util.List;

public class UpdateFunctionRequest {
    private String name;
    private String expression;
    private String type;
    private Long userId;
    private Double fromX;
    private Double toX;
    private Integer pointsCount;
    private String description;
    private List<PointUpdateRequest> points;
    private Double coefficientA;
    private Double coefficientB;
    private Double coefficientC;

    public String getName() { return name; }
    public String getExpression() { return expression; }
    public String getType() { return type; }
    public Long getUserId() { return userId; }
    public Double getFromX() { return fromX; }
    public Double getToX() { return toX; }
    public Integer getPointsCount() { return pointsCount; }
    public String getDescription() { return description; }
    public List<PointUpdateRequest> getPoints() { return points; }
    public Double getCoefficientA() { return coefficientA; }
    public Double getCoefficientB() { return coefficientB; }
    public Double getCoefficientC() { return coefficientC; }

    public void setName(String name) { this.name = name; }
    public void setExpression(String expression) { this.expression = expression; }
    public void setType(String type) { this.type = type; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setFromX(Double fromX) { this.fromX = fromX; }
    public void setToX(Double toX) { this.toX = toX; }
    public void setPointsCount(Integer pointsCount) { this.pointsCount = pointsCount; }
    public void setDescription(String description) { this.description = description; }
    public void setPoints(List<PointUpdateRequest> points) { this.points = points; }
    public void setCoefficientA(Double coefficientA) { this.coefficientA = coefficientA; }
    public void setCoefficientB(Double coefficientB) { this.coefficientB = coefficientB; }
    public void setCoefficientC(Double coefficientC) { this.coefficientC = coefficientC; }

    public static class PointUpdateRequest {
        private Double x;
        private Double y;

        public Double getX() { return x; }
        public Double getY() { return y; }

        public void setX(Double x) { this.x = x; }
        public void setY(Double y) { this.y = y; }
    }
}