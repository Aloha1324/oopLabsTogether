package com.example.LAB5.DTO.Response;

import java.util.List;

public class CalculationResponse {
    private boolean success;
    private String message;
    private Double result;
    private String operation;
    private Long functionId;
    private Double inputValue;
    private Double x;
    private List<Double> xValues;
    private List<Double> results;
    private String operationType;
    private String method;
    private Long calculationTime;
    private Integer threadsUsed;
    private Integer polynomialDegree;

    public CalculationResponse() {}

    public CalculationResponse(boolean success, String message, Double result, String operation, Long functionId, Double inputValue) {
        this.success = success;
        this.message = message;
        this.result = result;
        this.operation = operation;
        this.functionId = functionId;
        this.inputValue = inputValue;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Double getResult() { return result; }
    public String getOperation() { return operation; }
    public Long getFunctionId() { return functionId; }
    public Double getInputValue() { return inputValue; }
    public Double getX() { return x; }
    public List<Double> getXValues() { return xValues; }
    public List<Double> getResults() { return results; }
    public String getOperationType() { return operationType; }
    public String getMethod() { return method; }
    public Long getCalculationTime() { return calculationTime; }
    public Integer getThreadsUsed() { return threadsUsed; }
    public Integer getPolynomialDegree() { return polynomialDegree; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setResult(Double result) { this.result = result; }
    public void setOperation(String operation) { this.operation = operation; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public void setInputValue(Double inputValue) { this.inputValue = inputValue; }
    public void setX(Double x) { this.x = x; }
    public void setXValues(List<Double> xValues) { this.xValues = xValues; }
    public void setResults(List<Double> results) { this.results = results; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public void setMethod(String method) { this.method = method; }
    public void setCalculationTime(Long calculationTime) { this.calculationTime = calculationTime; }
    public void setThreadsUsed(Integer threadsUsed) { this.threadsUsed = threadsUsed; }
    public void setPolynomialDegree(Integer polynomialDegree) { this.polynomialDegree = polynomialDegree; }

    public static CalculationResponse success(Double result, Long functionId, Double x, String operationType) {
        CalculationResponse response = new CalculationResponse();
        response.setSuccess(true);
        response.setResult(result);
        response.setFunctionId(functionId);
        response.setX(x);
        response.setOperationType(operationType);
        response.setMessage("Вычисление успешно выполнено");
        return response;
    }

    public static CalculationResponse batchSuccess(List<Double> results, Long functionId, List<Double> xValues, String operationType) {
        CalculationResponse response = new CalculationResponse();
        response.setSuccess(true);
        response.setResults(results);
        response.setFunctionId(functionId);
        response.setXValues(xValues);
        response.setOperationType(operationType);
        response.setMessage(String.format("Пакетное вычисление успешно: %d точек", xValues.size()));
        return response;
    }

    public static CalculationResponse error(String message, Long functionId) {
        CalculationResponse response = new CalculationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setFunctionId(functionId);
        return response;
    }
}