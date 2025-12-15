package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.*;
import java.util.List;

public class DifferentialRequest {

    @NotNull(message = "ID функции обязателен")
    @Min(value = 1, message = "ID функции должен быть положительным")
    private Long functionId;

    @NotNull(message = "Точка X обязательна")
    @DecimalMin(value = "-10000.0", message = "X не может быть меньше -10000")
    @DecimalMax(value = "10000.0", message = "X не может быть больше 10000")
    private Double x;

    @Min(value = 1, message = "Порядок производной должен быть не менее 1")
    @Max(value = 5, message = "Порядок производной не может быть больше 5")
    private Integer order = 1;

    @Pattern(regexp = "FORWARD_DIFFERENCE|BACKWARD_DIFFERENCE|CENTRAL_DIFFERENCE",
            message = "Метод должен быть: FORWARD_DIFFERENCE, BACKWARD_DIFFERENCE или CENTRAL_DIFFERENCE")
    private String method = "CENTRAL_DIFFERENCE";

    @DecimalMin(value = "0.000001", message = "Шаг должен быть не меньше 0.000001")
    @DecimalMax(value = "1.0", message = "Шаг не может быть больше 1")
    private Double step = 0.001;

    @Size(min = 1, max = 100, message = "Можно вычислить от 1 до 100 точек")
    private List<@DecimalMin("-10000.0") @DecimalMax("10000.0") Double> xValues;

    private Boolean calculateOnInterval = false;

    @DecimalMin(value = "-1000.0", message = "Начало интервала не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Начало интервала не может быть больше 1000")
    private Double intervalFrom;

    @DecimalMin(value = "-1000.0", message = "Конец интервала не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Конец интервала не может быть больше 1000")
    private Double intervalTo;

    @Min(value = 10, message = "Количество точек на интервале должно быть не менее 10")
    @Max(value = 1000, message = "Количество точек на интервале не может быть больше 1000")
    private Integer intervalPoints = 100;

    @AssertTrue(message = "При расчете на интервале необходимо задать границы интервала")
    private boolean isValidInterval() {
        if (Boolean.TRUE.equals(calculateOnInterval)) {
            return intervalFrom != null && intervalTo != null && intervalFrom < intervalTo;
        }
        return true;
    }

    // Геттеры и сеттеры
    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Double getStep() {
        return step;
    }

    public void setStep(Double step) {
        this.step = step;
    }

    public List<Double> getXValues() {
        return xValues;
    }

    public void setXValues(List<Double> xValues) {
        this.xValues = xValues;
    }

    public Boolean getCalculateOnInterval() {
        return calculateOnInterval;
    }

    public void setCalculateOnInterval(Boolean calculateOnInterval) {
        this.calculateOnInterval = calculateOnInterval;
    }

    public Double getIntervalFrom() {
        return intervalFrom;
    }

    public void setIntervalFrom(Double intervalFrom) {
        this.intervalFrom = intervalFrom;
    }

    public Double getIntervalTo() {
        return intervalTo;
    }

    public void setIntervalTo(Double intervalTo) {
        this.intervalTo = intervalTo;
    }

    public Integer getIntervalPoints() {
        return intervalPoints;
    }

    public void setIntervalPoints(Integer intervalPoints) {
        this.intervalPoints = intervalPoints;
    }
}