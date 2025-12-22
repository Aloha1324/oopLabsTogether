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

    // Геттеры и сеттеры для всех полей

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getFromX() { return fromX; }
    public void setFromX(Double fromX) { this.fromX = fromX; }

    public Double getToX() { return toX; }
    public void setToX(Double toX) { this.toX = toX; }

    public Integer getPointsCount() { return pointsCount; }
    public void setPointsCount(Integer pointsCount) { this.pointsCount = pointsCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<PointUpdateRequest> getPoints() { return points; }
    public void setPoints(List<PointUpdateRequest> points) { this.points = points; }

    public Double getCoefficientA() { return coefficientA; }
    public void setCoefficientA(Double coefficientA) { this.coefficientA = coefficientA; }

    public Double getCoefficientB() { return coefficientB; }
    public void setCoefficientB(Double coefficientB) { this.coefficientB = coefficientB; }

    public Double getCoefficientC() { return coefficientC; }
    public void setCoefficientC(Double coefficientC) { this.coefficientC = coefficientC; }

    // === ВЛОЖЕННЫЙ КЛАСС ===
    public static class PointUpdateRequest {
        private Double x;
        private Double y;

        // Обязательные геттеры и сеттеры для Jackson
        public Double getX() { return x; }
        public void setX(Double x) { this.x = x; }

        public Double getY() { return y; }
        public void setY(Double y) { this.y = y; }

        // Конструктор по умолчанию (не обязателен, но рекомендуется)
        public PointUpdateRequest() {}

        // Конструктор для удобства (опционально)
        public PointUpdateRequest(Double x, Double y) {
            this.x = x;
            this.y = y;
        }

        // equals/hashCode (опционально, но полезно для тестов)
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PointUpdateRequest that = (PointUpdateRequest) o;
            return java.util.Objects.equals(x, that.x) &&
                    java.util.Objects.equals(y, that.y);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(x, y);
        }
    }
}