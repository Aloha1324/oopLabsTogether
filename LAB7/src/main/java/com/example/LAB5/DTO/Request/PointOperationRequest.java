package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class PointOperationRequest {

    @NotNull(message = "ID функции обязателен")
    @Min(value = 1, message = "ID функции должен быть положительным")
    private Long functionId;

    // Операция над точками
    @Pattern(regexp = "ADD|UPDATE|DELETE|CLEAR|FILTER",
            message = "Операция должна быть: ADD, UPDATE, DELETE, CLEAR или FILTER")
    private String operation = "ADD";

    // Для ADD/UPDATE операций
    @Size(min = 1, max = 1000, message = "Можно добавить от 1 до 1000 точек")
    private List<PointData> points;

    // Для DELETE операции
    @Size(min = 1, max = 1000, message = "Можно удалить от 1 до 1000 точек")
    private List<@Min(1) Long> pointIds;

    // Для FILTER операции
    @DecimalMin(value = "-10000.0", message = "Минимальное X не может быть меньше -10000")
    private Double minX;

    @DecimalMax(value = "10000.0", message = "Максимальное X не может быть больше 10000")
    private Double maxX;

    @DecimalMin(value = "-10000.0", message = "Минимальное Y не может быть меньше -10000")
    private Double minY;

    @DecimalMax(value = "10000.0", message = "Максимальное Y не может быть больше 10000")
    private Double maxY;

    // Валидация в зависимости от операции
    @AssertTrue(message = "Для операций ADD и UPDATE требуются точки")
    private boolean isValidPointsOperation() {
        if ("ADD".equals(operation) || "UPDATE".equals(operation)) {
            return points != null && !points.isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Для операции DELETE требуются ID точек")
    private boolean isValidDeleteOperation() {
        if ("DELETE".equals(operation)) {
            return pointIds != null && !pointIds.isEmpty();
        }
        return true;
    }

    // Вложенный класс для данных точки
    @Data
    public static class PointData {

        @DecimalMin(value = "-10000.0", message = "X не может быть меньше -10000")
        @DecimalMax(value = "10000.0", message = "X не может быть больше 10000")
        private Double x;

        @DecimalMin(value = "-10000.0", message = "Y не может быть меньше -10000")
        @DecimalMax(value = "10000.0", message = "Y не может быть больше 10000")
        private Double y;

        private Integer orderIndex;

        // Для UPDATE операции
        @Min(value = 1, message = "ID точки должен быть положительным")
        private Long id;
    }
}