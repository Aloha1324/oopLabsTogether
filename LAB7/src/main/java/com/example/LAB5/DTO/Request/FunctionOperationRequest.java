// src/main/java/com/example/LAB5/DTO/Request/FunctionOperationRequest.java

package com.example.LAB5.DTO.Request;

import java.util.List;

public class FunctionOperationRequest {

    // Для операций с одной функцией (apply, дифференцирование, интеграл)
    private Long functionId;
    private Double x;
    private String operationType = "VALUE";
    private String method = "LINEAR";
    private Integer polynomialDegree = 2;
    private List<Double> xValues;

    //
    private Long functionAId;
    private Long functionBId;

    // Для создания составной функции
    private String mathFunctionType;
    private Double fromX;
    private Double toX;
    private Integer pointsCount;

    // Конструкторы (по желанию)
    public FunctionOperationRequest() {}

    // Геттеры
    public Long getFunctionId() { return functionId; }
    public Double getX() { return x; }
    public String getOperationType() { return operationType; }
    public String getMethod() { return method; }
    public Integer getPolynomialDegree() { return polynomialDegree; }
    public List<Double> getXValues() { return xValues; }

    //
    public Long getFunctionAId() { return functionAId; }
    public Long getFunctionBId() { return functionBId; }
    public String getMathFunctionType() { return mathFunctionType; }
    public Double getFromX() { return fromX; }
    public Double getToX() { return toX; }
    public Integer getPointsCount() { return pointsCount; }

    // Сеттеры
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public void setX(Double x) { this.x = x; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public void setMethod(String method) { this.method = method; }
    public void setPolynomialDegree(Integer polynomialDegree) { this.polynomialDegree = polynomialDegree; }
    public void setXValues(List<Double> xValues) { this.xValues = xValues; }

    //
    public void setFunctionAId(Long functionAId) { this.functionAId = functionAId; }
    public void setFunctionBId(Long functionBId) { this.functionBId = functionBId; }
    public void setMathFunctionType(String mathFunctionType) { this.mathFunctionType = mathFunctionType; }
    public void setFromX(Double fromX) { this.fromX = fromX; }
    public void setToX(Double toX) { this.toX = toX; }
    public void setPointsCount(Integer pointsCount) { this.pointsCount = pointsCount; }

    // Утилита
    public boolean isBatchRequest() {
        return xValues != null && !xValues.isEmpty();
    }
}