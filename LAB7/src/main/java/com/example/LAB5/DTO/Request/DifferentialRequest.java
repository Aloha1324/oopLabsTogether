package com.example.LAB5.DTO.Request;

import java.util.List;

public class DifferentialRequest {
    private Long functionId;
    private Double x;
    private Integer order = 1;
    private String method = "CENTRAL_DIFFERENCE";
    private Double step = 0.001;
    private List<Double> xValues;
    private Boolean calculateOnInterval = false;
    private Double intervalFrom;
    private Double intervalTo;
    private Integer intervalPoints = 100;

    public Long getFunctionId() { return functionId; }
    public Double getX() { return x; }
    public Integer getOrder() { return order; }
    public String getMethod() { return method; }
    public Double getStep() { return step; }
    public List<Double> getXValues() { return xValues; }
    public Boolean getCalculateOnInterval() { return calculateOnInterval; }
    public Double getIntervalFrom() { return intervalFrom; }
    public Double getIntervalTo() { return intervalTo; }
    public Integer getIntervalPoints() { return intervalPoints; }

    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public void setX(Double x) { this.x = x; }
    public void setOrder(Integer order) { this.order = order; }
    public void setMethod(String method) { this.method = method; }
    public void setStep(Double step) { this.step = step; }
    public void setXValues(List<Double> xValues) { this.xValues = xValues; }
    public void setCalculateOnInterval(Boolean calculateOnInterval) { this.calculateOnInterval = calculateOnInterval; }
    public void setIntervalFrom(Double intervalFrom) { this.intervalFrom = intervalFrom; }
    public void setIntervalTo(Double intervalTo) { this.intervalTo = intervalTo; }
    public void setIntervalPoints(Integer intervalPoints) { this.intervalPoints = intervalPoints; }
}