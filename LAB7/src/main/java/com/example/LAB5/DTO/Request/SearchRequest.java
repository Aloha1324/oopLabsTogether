package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SearchRequest {

    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @Pattern(regexp = "FROM_ARRAYS|FROM_MATH|FROM_EXPRESSION|",
            message = "Тип должен быть: FROM_ARRAYS, FROM_MATH или FROM_EXPRESSION")
    private String type;

    @Min(value = 1, message = "User ID должен быть положительным")
    private Long userId;

    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    private String username;

    // По диапазону значений
    @DecimalMin(value = "-10000.0", message = "Минимальное значение X не может быть меньше -10000")
    private Double minX;

    @DecimalMax(value = "10000.0", message = "Максимальное значение X не может быть больше 10000")
    private Double maxX;

    @DecimalMin(value = "-10000.0", message = "Минимальное значение Y не может быть меньше -10000")
    private Double minY;

    @DecimalMax(value = "10000.0", message = "Максимальное значение Y не может быть больше 10000")
    private Double maxY;

    @Min(value = 2, message = "Минимальное количество точек должно быть не меньше 2")
    private Integer minPoints;

    @Min(value = 2, message = "Максимальное количество точек должно быть не меньше 2")
    private Integer maxPoints;

    // По датам
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$|^$",
            message = "Дата должна быть в формате YYYY-MM-DD")
    private String createdAfter;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$|^$",
            message = "Дата должна быть в формате YYYY-MM-DD")
    private String createdBefore;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$|^$",
            message = "Дата должна быть в формате YYYY-MM-DD")
    private String updatedAfter;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$|^$",
            message = "Дата должна быть в формате YYYY-MM-DD")
    private String updatedBefore;

    // По выражению (LIKE поиск)
    @Size(max = 100, message = "Выражение для поиска не должно превышать 100 символов")
    private String expressionContains;

    // Пагинация и сортировка
    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private Integer page = 0;

    @Min(value = 1, message = "Размер страницы должен быть не меньше 1")
    @Max(value = 100, message = "Размер страницы не может превышать 100")
    private Integer size = 10;

    @Pattern(regexp = "id|name|createdAt|updatedAt|pointCount|minX|maxX|minY|maxY",
            message = "Сортировка возможна по: id, name, createdAt, updatedAt, pointCount, minX, maxX, minY, maxY")
    private String sortBy = "createdAt";

    @Pattern(regexp = "ASC|DESC", message = "Направление сортировки должно быть ASC или DESC")
    private String sortDirection = "DESC";

    // Валидационные методы
    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    private boolean isValidCreatedDateRange() {
        if (createdAfter != null && !createdAfter.isEmpty() &&
                createdBefore != null && !createdBefore.isEmpty()) {
            return createdAfter.compareTo(createdBefore) <= 0;
        }
        return true;
    }

    @AssertTrue(message = "Минимальное X не может быть больше максимального X")
    private boolean isValidXRange() {
        if (minX != null && maxX != null) {
            return minX <= maxX;
        }
        return true;
    }

    @AssertTrue(message = "Минимальное Y не может быть больше максимального Y")
    private boolean isValidYRange() {
        if (minY != null && maxY != null) {
            return minY <= maxY;
        }
        return true;
    }

    @AssertTrue(message = "Минимальное количество точек не может быть больше максимального")
    private boolean isValidPointsRange() {
        if (minPoints != null && maxPoints != null) {
            return minPoints <= maxPoints;
        }
        return true;
    }
}