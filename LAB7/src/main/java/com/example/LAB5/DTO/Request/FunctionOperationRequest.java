package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class FunctionOperationRequest {

    @NotNull(message = "ID функции обязателен")
    @Min(value = 1, message = "ID функции должен быть положительным")
    private Long functionId;

    @NotNull(message = "Значение X обязательно")
    @DecimalMin(value = "-10000.0", message = "X не может быть меньше -10000")
    @DecimalMax(value = "10000.0", message = "X не может быть больше 10000")
    private Double x;

    @Pattern(regexp = "VALUE|INTERPOLATE|EXTRAPOLATE",
            message = "Тип операции должен быть: VALUE, INTERPOLATE или EXTRAPOLATE")
    private String operationType = "VALUE";

    @Pattern(regexp = "LINEAR|SPLINE|POLYNOMIAL",
            message = "Метод должен быть: LINEAR, SPLINE или POLYNOMIAL")
    private String method = "LINEAR";

    @Min(value = 1, message = "Порядок полинома должен быть не менее 1")
    @Max(value = 10, message = "Порядок полинома не может быть больше 10")
    private Integer polynomialDegree = 2;

    @Size(min = 1, max = 100, message = "Можно вычислить от 1 до 100 точек")
    private List<@DecimalMin("-10000.0") @DecimalMax("10000.0") Double> xValues;

    public boolean isBatchRequest() {
        return xValues != null && !xValues.isEmpty();
    }

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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getPolynomialDegree() {
        return polynomialDegree;
    }

    public void setPolynomialDegree(Integer polynomialDegree) {
        this.polynomialDegree = polynomialDegree;
    }

    public List<Double> getXValues() {
        return xValues;
    }

    public void setXValues(List<Double> xValues) {
        this.xValues = xValues;
    }
}