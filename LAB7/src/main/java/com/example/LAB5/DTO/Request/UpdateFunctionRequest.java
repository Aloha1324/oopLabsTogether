package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class UpdateFunctionRequest {

    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    @Size(max = 500, message = "Выражение не должно превышать 500 символов")
    private String expression;

    @Pattern(regexp = "FROM_ARRAYS|FROM_MATH|FROM_EXPRESSION",
            message = "Тип должен быть: FROM_ARRAYS, FROM_MATH или FROM_EXPRESSION")
    private String type;

    @Min(value = 1, message = "User ID должен быть положительным")
    private Long userId;

    @DecimalMin(value = "-1000.0", message = "Начало интервала не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Начало интервала не может быть больше 1000")
    private Double fromX;

    @DecimalMin(value = "-1000.0", message = "Конец интервала не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Конец интервала не может быть больше 1000")
    private Double toX;

    @Min(value = 2, message = "Минимум 2 точки")
    @Max(value = 1000, message = "Максимум 1000 точек")
    private Integer pointsCount;

    private String description;

    private List<PointUpdateRequest> points;

    private Double coefficientA;
    private Double coefficientB;
    private Double coefficientC;

    @AssertTrue(message = "fromX должен быть меньше toX")
    private boolean isValidInterval() {
        if (fromX != null && toX != null) {
            return fromX < toX;
        }
        return true;
    }

    // Геттеры вручную
    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public String getType() {
        return type;
    }

    public Long getUserId() {
        return userId;
    }

    public Double getFromX() {
        return fromX;
    }

    public Double getToX() {
        return toX;
    }

    public Integer getPointsCount() {
        return pointsCount;
    }

    public String getDescription() {
        return description;
    }

    public List<PointUpdateRequest> getPoints() {
        return points;
    }

    public Double getCoefficientA() {
        return coefficientA;
    }

    public Double getCoefficientB() {
        return coefficientB;
    }

    public Double getCoefficientC() {
        return coefficientC;
    }

    // Сеттеры
    public void setName(String name) {
        this.name = name;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFromX(Double fromX) {
        this.fromX = fromX;
    }

    public void setToX(Double toX) {
        this.toX = toX;
    }

    public void setPointsCount(Integer pointsCount) {
        this.pointsCount = pointsCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPoints(List<PointUpdateRequest> points) {
        this.points = points;
    }

    public void setCoefficientA(Double coefficientA) {
        this.coefficientA = coefficientA;
    }

    public void setCoefficientB(Double coefficientB) {
        this.coefficientB = coefficientB;
    }

    public void setCoefficientC(Double coefficientC) {
        this.coefficientC = coefficientC;
    }

    @Data
    public static class PointUpdateRequest {
        @NotNull(message = "X coordinate is required")
        private Double x;

        @NotNull(message = "Y coordinate is required")
        private Double y;

        public Double getX() {
            return x;
        }

        public Double getY() {
            return y;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public void setY(Double y) {
            this.y = y;
        }
    }
}