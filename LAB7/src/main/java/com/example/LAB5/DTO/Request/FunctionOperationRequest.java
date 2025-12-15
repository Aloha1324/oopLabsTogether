package com.example.LAB5.DTO.Request;

import java.util.List;

public class FunctionOperationRequest {
    private Long functionId;
    private Double x;
    private String operationType = "VALUE";
    private String method = "LINEAR";
    private Integer polynomialDegree = 2;
    private List<Double> xValues;

    public Long getFunctionId() { return functionId; }
    public Double getX() { return x; }
    public String getOperationType() { return operationType; }
    public String getMethod() { return method; }
    public Integer getPolynomialDegree() { return polynomialDegree; }
    public List<Double> getXValues() { return xValues; }

    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public void setX(Double x) { this.x = x; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public void setMethod(String method) { this.method = method; }
    public void setPolynomialDegree(Integer polynomialDegree) { this.polynomialDegree = polynomialDegree; }
    public void setXValues(List<Double> xValues) { this.xValues = xValues; }

    public boolean isBatchRequest() {
        return xValues != null && !xValues.isEmpty();
    }
}