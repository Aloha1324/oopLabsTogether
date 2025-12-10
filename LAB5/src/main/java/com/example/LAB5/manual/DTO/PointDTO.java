package com.example.LAB5.manual.DTO;

import java.util.Objects;

public class PointDTO {

    private Long id;
    private Long functionId;
    private Double xValue;
    private Double yValue;

    public PointDTO() {
    }

    public PointDTO(Long functionId, Double xValue, Double yValue) {
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Double getXValue() {
        return xValue;
    }

    public void setXValue(Double xValue) {
        this.xValue = xValue;
    }

    public Double getYValue() {
        return yValue;
    }

    public void setYValue(Double yValue) {
        this.yValue = yValue;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PointDTO)) {
            return false;
        }
        PointDTO that = (PointDTO) other;
        return Objects.equals(id, that.id)
                && Objects.equals(functionId, that.functionId)
                && Objects.equals(xValue, that.xValue)
                && Objects.equals(yValue, that.yValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, functionId, xValue, yValue);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PointDTO{");
        sb.append("id=").append(id);
        sb.append(", functionId=").append(functionId);
        sb.append(", xValue=").append(xValue);
        sb.append(", yValue=").append(yValue);
        sb.append('}');
        return sb.toString();
    }
}
