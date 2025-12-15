package com.example.LAB5.DTO.Request;

import java.util.Objects;

public class IntegrationRequest {
    private Long functionId;
    private Double lowerBound;
    private Double upperBound;
    private String method = "SIMPSON";
    private Integer partitions = 1000;
    private Integer monteCarloTrials = 10000;
    private Boolean doubleIntegration = false;
    private Double lowerBoundY;
    private Double upperBoundY;
    private Double precision = 0.0001;

    public Long getFunctionId() { return functionId; }
    public Double getLowerBound() { return lowerBound; }
    public Double getUpperBound() { return upperBound; }
    public String getMethod() { return method; }
    public Integer getPartitions() { return partitions; }
    public Integer getMonteCarloTrials() { return monteCarloTrials; }
    public Boolean getDoubleIntegration() { return doubleIntegration; }
    public Double getLowerBoundY() { return lowerBoundY; }
    public Double getUpperBoundY() { return upperBoundY; }
    public Double getPrecision() { return precision; }

    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public void setLowerBound(Double lowerBound) { this.lowerBound = lowerBound; }
    public void setUpperBound(Double upperBound) { this.upperBound = upperBound; }
    public void setMethod(String method) { this.method = method; }
    public void setPartitions(Integer partitions) { this.partitions = partitions; }
    public void setMonteCarloTrials(Integer monteCarloTrials) { this.monteCarloTrials = monteCarloTrials; }
    public void setDoubleIntegration(Boolean doubleIntegration) { this.doubleIntegration = doubleIntegration; }
    public void setLowerBoundY(Double lowerBoundY) { this.lowerBoundY = lowerBoundY; }
    public void setUpperBoundY(Double upperBoundY) { this.upperBoundY = upperBoundY; }
    public void setPrecision(Double precision) { this.precision = precision; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrationRequest that = (IntegrationRequest) o;
        return Objects.equals(functionId, that.functionId) &&
                Objects.equals(lowerBound, that.lowerBound) &&
                Objects.equals(upperBound, that.upperBound) &&
                Objects.equals(method, that.method) &&
                Objects.equals(partitions, that.partitions) &&
                Objects.equals(monteCarloTrials, that.monteCarloTrials) &&
                Objects.equals(doubleIntegration, that.doubleIntegration) &&
                Objects.equals(lowerBoundY, that.lowerBoundY) &&
                Objects.equals(upperBoundY, that.upperBoundY) &&
                Objects.equals(precision, that.precision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionId, lowerBound, upperBound, method, partitions, monteCarloTrials, doubleIntegration, lowerBoundY, upperBoundY, precision);
    }

    @Override
    public String toString() {
        return "IntegrationRequest{" +
                "functionId=" + functionId +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ", method='" + method + '\'' +
                ", partitions=" + partitions +
                ", monteCarloTrials=" + monteCarloTrials +
                ", doubleIntegration=" + doubleIntegration +
                ", lowerBoundY=" + lowerBoundY +
                ", upperBoundY=" + upperBoundY +
                ", precision=" + precision +
                '}';
    }
}