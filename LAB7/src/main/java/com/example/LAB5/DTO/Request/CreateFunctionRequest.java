package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateFunctionRequest {

    @NotBlank(message = "Название функции обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    @Size(max = 500, message = "Выражение не должно превышать 500 символов")
    private String expression;

    @NotBlank(message = "Тип функции обязателен")
    @Pattern(regexp = "FROM_ARRAYS|FROM_MATH|FROM_EXPRESSION",
            message = "Тип должен быть: FROM_ARRAYS, FROM_MATH или FROM_EXPRESSION")
    private String type;

    // Для типа FROM_ARRAYS
    @Size(min = 2, message = "Минимум 2 точки")
    private List<@DecimalMin("-10000.0") @DecimalMax("10000.0") Double> xValues;

    @Size(min = 2, message = "Минимум 2 точки")
    private List<@DecimalMin("-10000.0") @DecimalMax("10000.0") Double> yValues;

    // Для типа FROM_MATH
    @Pattern(regexp = "LINEAR|QUADRATIC|SIN|COS|EXP|LOG|POWER|ROOT",
            message = "Допустимые типы математических функций: LINEAR, QUADRATIC, SIN, COS, EXP, LOG, POWER, ROOT")
    private String mathFunctionType;

    @DecimalMin(value = "-1000.0", message = "Начало интервала не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Начало интервала не может быть больше 1000")
    private Double fromX;

    @DecimalMin(value = "-1000.0", message = "Конец интервала не может быть меньше -1000")
    @DecimalMax(value = "1000.0", message = "Конец интервала не может быть больше 1000")
    private Double toX;

    @Min(value = 2, message = "Минимум 2 точки")
    @Max(value = 1000, message = "Максимум 1000 точек")
    private Integer pointsCount;

    @NotNull(message = "User ID обязателен")
    @Min(value = 1, message = "User ID должен быть положительным")
    private Long userId;

    private String description;

    // Дополнительные параметры для математических функций
    private Double coefficientA = 1.0;
    private Double coefficientB = 0.0;
    private Double coefficientC = 0.0;

    @AssertTrue(message = "Для типа FROM_ARRAYS размеры массивов должны совпадать")
    private boolean isValidArrays() {
        if ("FROM_ARRAYS".equals(type)) {
            return xValues != null && yValues != null && xValues.size() == yValues.size();
        }
        return true;
    }

    @AssertTrue(message = "Для типа FROM_MATH fromX должен быть меньше toX")
    private boolean isValidMathInterval() {
        if ("FROM_MATH".equals(type) && fromX != null && toX != null) {
            return fromX < toX;
        }
        return true;
    }

    @AssertTrue(message = "Для типа FROM_EXPRESSION выражение обязательно")
    private boolean isValidExpression() {
        if ("FROM_EXPRESSION".equals(type)) {
            return expression != null && !expression.trim().isEmpty();
        }
        return true;
    }
}