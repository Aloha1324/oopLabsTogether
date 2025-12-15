package com.example.LAB5.DTO.Request;

import java.util.List;

public class CreateFunctionRequest {
    private String name;
    private String expression;
    private String type;
    private List<Double> xValues;
    private List<Double> yValues;
    private String mathFunctionType;
    private Double fromX;
    private Double toX;
    private Integer pointsCount;
    private Long userId;
    private String description;
    private Double coefficientA = 1.0;
    private Double coefficientB = 0.0;
    private Double coefficientC = 0.0;

    public String getName() { return name; }
    public String getExpression() { return expression; }
    public String getType() { return type; }
    public List<Double> getXValues() { return xValues; }
    public List<Double> getYValues() { return yValues; }
    public String getMathFunctionType() { return mathFunctionType; }
    public Double getFromX() { return fromX; }
    public Double getToX() { return toX; }
    public Integer getPointsCount() { return pointsCount; }
    public Long getUserId() { return userId; }
    public String getDescription() { return description; }
    public Double getCoefficientA() { return coefficientA; }
    public Double getCoefficientB() { return coefficientB; }
    public Double getCoefficientC() { return coefficientC; }

    public void setName(String name) { this.name = name; }
    public void setExpression(String expression) { this.expression = expression; }
    public void setType(String type) { this.type = type; }
    public void setXValues(List<Double> xValues) { this.xValues = xValues; }
    public void setYValues(List<Double> yValues) { this.yValues = yValues; }
    public void setMathFunctionType(String mathFunctionType) { this.mathFunctionType = mathFunctionType; }
    public void setFromX(Double fromX) { this.fromX = fromX; }
    public void setToX(Double toX) { this.toX = toX; }
    public void setPointsCount(Integer pointsCount) { this.pointsCount = pointsCount; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setDescription(String description) { this.description = description; }
    public void setCoefficientA(Double coefficientA) { this.coefficientA = coefficientA; }
    public void setCoefficientB(Double coefficientB) { this.coefficientB = coefficientB; }
    public void setCoefficientC(Double coefficientC) { this.coefficientC = coefficientC; }
}