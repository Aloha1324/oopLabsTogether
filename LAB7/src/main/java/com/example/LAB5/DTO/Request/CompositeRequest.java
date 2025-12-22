package com.example.LAB5.DTO.Request;

public class CompositeRequest {
    private Long functionAId;
    private Long functionBId;
    private String operation; // "compose", "add", "mul"
    private String name;

    // Геттеры и сеттеры
    public Long getFunctionAId() { return functionAId; }
    public void setFunctionAId(Long functionAId) { this.functionAId = functionAId; }
    public Long getFunctionBId() { return functionBId; }
    public void setFunctionBId(Long functionBId) { this.functionBId = functionBId; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}