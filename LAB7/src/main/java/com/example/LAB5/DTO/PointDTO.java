package com.example.LAB5.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO для передачи точки функции
 * Простая структура данных (x, y) с дополнительной информацией
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointDTO {
    private int index;          // Индекс точки в функции
    private double x;           // Значение аргумента
    private double y;           // Значение функции
    private Double derivative;  // Производная в точке (опционально)
    private Double integral;    // Интеграл до точки (опционально)
    private Long functionId;    // ID родительской функции
    private boolean selected;   // Для UI - выбрана ли точка

    // Конструкторы
    public PointDTO() {}

    public PointDTO(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointDTO(int index, double x, double y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public PointDTO(double x, double y, Long functionId) {
        this.x = x;
        this.y = y;
        this.functionId = functionId;
    }

    // Геттеры и сеттеры
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public Double getDerivative() { return derivative; }
    public void setDerivative(Double derivative) { this.derivative = derivative; }

    public Double getIntegral() { return integral; }
    public void setIntegral(Double integral) { this.integral = integral; }

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Вспомогательные методы
    public double distanceTo(PointDTO other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isZero() {
        return Math.abs(y) < 1e-10;
    }

    public boolean isPositive() {
        return y > 0;
    }

    public boolean isNegative() {
        return y < 0;
    }

    @Override
    public String toString() {
        if (derivative != null) {
            return String.format("Point[%d]: (%.2f, %.2f) deriv=%.2f", index, x, y, derivative);
        }
        return String.format("Point[%d]: (%.2f, %.2f)", index, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PointDTO point = (PointDTO) obj;
        return Double.compare(point.x, x) == 0 &&
                Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) * 31 + Double.hashCode(y);
    }
}