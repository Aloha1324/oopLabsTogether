package com.example.LAB5.manual.DTO;

import java.util.Objects;

public class PointDTO {
    private Long id;
    private Long functionId;
    private Double xValue;
    private Double yValue;

    public PointDTO() {}

    public PointDTO(Long functionId, Double xValue, Double yValue) {
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public PointDTO(Long id, Long functionId, Double xValue, Double yValue) {
        this.id = id;
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }

    public Double getXValue() { return xValue; }
    public void setXValue(Double xValue) { this.xValue = xValue; }

    public Double getYValue() { return yValue; }
    public void setYValue(Double yValue) { this.yValue = yValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointDTO pointDTO = (PointDTO) o;
        return Objects.equals(id, pointDTO.id) &&
                Objects.equals(functionId, pointDTO.functionId) &&
                Objects.equals(xValue, pointDTO.xValue) &&
                Objects.equals(yValue, pointDTO.yValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, functionId, xValue, yValue);
    }

    @Override
    public String toString() {
        return "PointDTO{" +
                "id=" + id +
                ", functionId=" + functionId +
                ", xValue=" + xValue +
                ", yValue=" + yValue +
                '}';
    }
}