package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.*;
import java.util.Objects;

public class IntegrationRequest {

    @NotNull(message = "ID функции обязателен")
    @Min(value = 1, message = "ID функции должен быть положительным")
    private Long functionId;

    @NotNull(message = "Нижний предел интегрирования обязателен")
    @DecimalMin(value = "-1000.0", message = "Нижний предел не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Нижний предел не может быть больше 1000")
    private Double lowerBound;

    @NotNull(message = "Верхний предел интегрирования обязателен")
    @DecimalMin(value = "-1000.0", message = "Верхний предел не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Верхний предел не может быть больше 1000")
    private Double upperBound;

    @Pattern(regexp = "RECTANGLE|TRAPEZOID|SIMPSON|MONTE_CARLO",
            message = "Метод должен быть: RECTANGLE, TRAPEZOID, SIMPSON или MONTE_CARLO")
    private String method = "SIMPSON";

    @Min(value = 10, message = "Количество разбиений должно быть не менее 10")
    @Max(value = 1000000, message = "Количество разбиений не может быть больше 1000000")
    private Integer partitions = 1000;

    @Min(value = 1000, message = "Количество испытаний должно быть не менее 1000")
    @Max(value = 1000000, message = "Количество испытаний не может быть больше 1000000")
    private Integer monteCarloTrials = 10000;

    private Boolean doubleIntegration = false;

    @DecimalMin(value = "-1000.0", message = "Нижний предел по Y не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Нижний предел по Y не может быть больше 1000")
    private Double lowerBoundY;

    @DecimalMin(value = "-1000.0", message = "Верхний предел по Y не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Верхний предел по Y не может быть больше 1000")
    private Double upperBoundY;

    @DecimalMin(value = "0.000001", message = "Точность должна быть не меньше 0.000001")
    @DecimalMax(value = "0.1", message = "Точность не может быть больше 0.1")
    private Double precision = 0.0001;

    @AssertTrue(message = "Нижний предел должен быть меньше верхнего")
    private boolean isValidBounds() {
        return lowerBound < upperBound;
    }

    @AssertTrue(message = "Для двойного интегрирования требуются пределы по Y")
    private boolean isValidDoubleIntegration() {
        if (Boolean.TRUE.equals(doubleIntegration)) {
            return lowerBoundY != null && upperBoundY != null && lowerBoundY < upperBoundY;
        }
        return true;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(Double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Double upperBound) {
        this.upperBound = upperBound;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getPartitions() {
        return partitions;
    }

    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    public Integer getMonteCarloTrials() {
        return monteCarloTrials;
    }

    public void setMonteCarloTrials(Integer monteCarloTrials) {
        this.monteCarloTrials = monteCarloTrials;
    }

    public Boolean getDoubleIntegration() {
        return doubleIntegration;
    }

    public void setDoubleIntegration(Boolean doubleIntegration) {
        this.doubleIntegration = doubleIntegration;
    }

    public Double getLowerBoundY() {
        return lowerBoundY;
    }

    public void setLowerBoundY(Double lowerBoundY) {
        this.lowerBoundY = lowerBoundY;
    }

    public Double getUpperBoundY() {
        return upperBoundY;
    }

    public void setUpperBoundY(Double upperBoundY) {
        this.upperBoundY = upperBoundY;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

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