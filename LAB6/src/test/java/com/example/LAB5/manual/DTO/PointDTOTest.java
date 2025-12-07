package com.example.LAB5.manual.DTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PointDTO класса
 */
class PointDTOTest {

    @Test
    @DisplayName("PointDTO - создание с ID и базовые геттеры")
    void testPointDTO_CreationWithIdAndGetters() {
        // Given
        Long id = 1L;
        Long functionId = 10L;
        Double xValue = 2.5;
        Double yValue = 6.25;

        // When
        PointDTO pointDTO = new PointDTO(id, functionId, xValue, yValue);

        // Then
        assertAll("PointDTO properties with ID",
                () -> assertEquals(id, pointDTO.getId(), "ID должен совпадать"),
                () -> assertEquals(functionId, pointDTO.getFunctionId(), "FunctionId должен совпадать"),
                () -> assertEquals(xValue, pointDTO.getXValue(), "XValue должен совпадать"),
                () -> assertEquals(yValue, pointDTO.getYValue(), "YValue должен совпадать")
        );
    }

    @Test
    @DisplayName("PointDTO - создание без ID")
    void testPointDTO_CreationWithoutId() {
        // Given
        Long functionId = 10L;
        Double xValue = 2.5;
        Double yValue = 6.25;

        // When
        PointDTO pointDTO = new PointDTO(functionId, xValue, yValue);

        // Then
        assertAll("PointDTO properties without ID",
                () -> assertNull(pointDTO.getId(), "ID должен быть null"),
                () -> assertEquals(functionId, pointDTO.getFunctionId(), "FunctionId должен совпадать"),
                () -> assertEquals(xValue, pointDTO.getXValue(), "XValue должен совпадать"),
                () -> assertEquals(yValue, pointDTO.getYValue(), "YValue должен совпадать")
        );
    }

    @Test
    @DisplayName("PointDTO - создание через пустой конструктор и сеттеры")
    void testPointDTO_EmptyConstructorAndSetters() {
        // Given
        PointDTO pointDTO = new PointDTO();

        // When
        pointDTO.setId(1L);
        pointDTO.setFunctionId(10L);
        pointDTO.setXValue(3.14);
        pointDTO.setYValue(2.71);

        // Then
        assertAll("PointDTO empty constructor and setters",
                () -> assertEquals(1L, pointDTO.getId(), "ID должен обновиться"),
                () -> assertEquals(10L, pointDTO.getFunctionId(), "FunctionId должен обновиться"),
                () -> assertEquals(3.14, pointDTO.getXValue(), 0.001, "XValue должен обновиться"),
                () -> assertEquals(2.71, pointDTO.getYValue(), 0.001, "YValue должен обновиться")
        );
    }

    @Test
    @DisplayName("PointDTO - equals и hashCode корректно работают")
    void testPointDTO_EqualsAndHashCode() {
        // Given
        PointDTO point1 = new PointDTO(1L, 10L, 1.0, 1.0);
        PointDTO point2 = new PointDTO(1L, 10L, 1.0, 1.0);
        PointDTO point3 = new PointDTO(2L, 20L, 2.0, 4.0);
        PointDTO point4 = new PointDTO(10L, 1.0, 1.0); // без ID

        // Then
        assertAll("PointDTO equals and hashCode",
                () -> assertEquals(point1, point2, "Объекты с одинаковыми полями должны быть равны"),
                () -> assertNotEquals(point1, point3, "Объекты с разными полями не должны быть равны"),
                () -> assertEquals(point1.hashCode(), point2.hashCode(), "HashCode должен совпадать для равных объектов"),
                () -> assertNotEquals(point1.hashCode(), point3.hashCode(), "HashCode должен отличаться для разных объектов"),
                () -> assertNotEquals(point1, point4, "Объекты с разными ID не должны быть равны")
        );
    }

    @Test
    @DisplayName("PointDTO - toString содержит важную информацию")
    void testPointDTO_ToStringContainsImportantInfo() {
        // Given
        PointDTO pointDTO = new PointDTO(1L, 10L, 2.5, 6.25);

        // When
        String toString = pointDTO.toString();

        // Then
        assertAll("PointDTO toString",
                () -> assertTrue(toString.contains("id=1"), "toString должен содержать ID"),
                () -> assertTrue(toString.contains("functionId=10"), "toString должен содержать functionId"),
                () -> assertTrue(toString.contains("xValue=2.5"), "toString должен содержать координату X"),
                () -> assertTrue(toString.contains("yValue=6.25"), "toString должен содержать координату Y"),
                () -> assertTrue(toString.startsWith("PointDTO{"), "toString должен начинаться с имени класса"),
                () -> assertTrue(toString.endsWith("}"), "toString должен заканчиваться фигурной скобкой")
        );
    }

    @Test
    @DisplayName("PointDTO - создание с null значениями")
    void testPointDTO_CreationWithNullValues() {
        // Given & When
        PointDTO pointDTO = new PointDTO(null, null, null, null);

        // Then
        assertAll("PointDTO with null values",
                () -> assertNull(pointDTO.getId(), "ID должен быть null"),
                () -> assertNull(pointDTO.getFunctionId(), "FunctionId должен быть null"),
                () -> assertNull(pointDTO.getXValue(), "XValue должен быть null"),
                () -> assertNull(pointDTO.getYValue(), "YValue должен быть null")
        );
    }

    @Test
    @DisplayName("PointDTO - создание с отрицательными координатами")
    void testPointDTO_CreationWithNegativeCoordinates() {
        // Given & When
        PointDTO pointDTO = new PointDTO(1L, 10L, -5.5, -10.2);

        // Then
        assertAll("PointDTO with negative coordinates",
                () -> assertEquals(-5.5, pointDTO.getXValue(), 0.001, "Должен поддерживать отрицательные X"),
                () -> assertEquals(-10.2, pointDTO.getYValue(), 0.001, "Должен поддерживать отрицательные Y")
        );
    }

    @Test
    @DisplayName("PointDTO - создание с нулевыми координатами")
    void testPointDTO_CreationWithZeroCoordinates() {
        // Given & When
        PointDTO pointDTO = new PointDTO(1L, 10L, 0.0, 0.0);

        // Then
        assertAll("PointDTO with zero coordinates",
                () -> assertEquals(0.0, pointDTO.getXValue(), 0.001, "Должен поддерживать нулевой X"),
                () -> assertEquals(0.0, pointDTO.getYValue(), 0.001, "Должен поддерживать нулевой Y")
        );
    }

    @Test
    @DisplayName("PointDTO - создание с максимальными значениями")
    void testPointDTO_CreationWithMaxValues() {
        // Given
        Long maxId = Long.MAX_VALUE;
        Long maxFunctionId = Long.MAX_VALUE;
        Double maxDouble = Double.MAX_VALUE;

        // When
        PointDTO pointDTO = new PointDTO(maxId, maxFunctionId, maxDouble, maxDouble);

        // Then
        assertAll("PointDTO with max values",
                () -> assertEquals(maxId, pointDTO.getId(), "Должен поддерживать максимальный Long ID"),
                () -> assertEquals(maxFunctionId, pointDTO.getFunctionId(), "Должен поддерживать максимальный Long functionId"),
                () -> assertEquals(maxDouble, pointDTO.getXValue(), "Должен поддерживать максимальный Double X"),
                () -> assertEquals(maxDouble, pointDTO.getYValue(), "Должен поддерживать максимальный Double Y")
        );
    }

    @Test
    @DisplayName("PointDTO - создание с минимальными значениями Double")
    void testPointDTO_CreationWithMinDoubleValues() {
        // Given
        Double minDouble = Double.MIN_VALUE;

        // When
        PointDTO pointDTO = new PointDTO(1L, 10L, minDouble, minDouble);

        // Then
        assertAll("PointDTO with min double values",
                () -> assertEquals(minDouble, pointDTO.getXValue(), "Должен поддерживать минимальный Double X"),
                () -> assertEquals(minDouble, pointDTO.getYValue(), "Должен поддерживать минимальный Double Y")
        );
    }

    @Test
    @DisplayName("PointDTO - специальные значения Double")
    void testPointDTO_SpecialDoubleValues() {
        // Given & When
        PointDTO point1 = new PointDTO(1L, 10L, Double.NaN, Double.NaN);
        PointDTO point2 = new PointDTO(2L, 20L, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);

        // Then
        assertAll("PointDTO special double values",
                () -> assertTrue(Double.isNaN(point1.getXValue()), "Должен поддерживать NaN для X"),
                () -> assertTrue(Double.isNaN(point1.getYValue()), "Должен поддерживать NaN для Y"),
                () -> assertEquals(Double.POSITIVE_INFINITY, point2.getXValue(), "Должен поддерживать POSITIVE_INFINITY"),
                () -> assertEquals(Double.NEGATIVE_INFINITY, point2.getYValue(), "Должен поддерживать NEGATIVE_INFINITY")
        );
    }

    @Test
    @DisplayName("PointDTO - equals с null и другим классом")
    void testPointDTO_EqualsWithNullAndOtherClass() {
        // Given
        PointDTO point = new PointDTO(1L, 10L, 1.0, 1.0);

        // Then
        assertAll("PointDTO equals edge cases",
                () -> assertNotEquals(point, null, "Не должен быть равен null"),
                () -> assertNotEquals(point, "string", "Не должен быть равен объекту другого класса"),
                () -> assertEquals(point, point, "Должен быть равен самому себе")
        );
    }
}