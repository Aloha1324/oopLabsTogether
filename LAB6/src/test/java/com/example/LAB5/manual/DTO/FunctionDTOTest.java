package com.example.LAB5.manual.DTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для FunctionDTO класса
 */
class FunctionDTOTest {

    @Test
    @DisplayName("FunctionDTO - создание с ID и базовые геттеры")
    void testFunctionDTO_CreationWithIdAndGetters() {
        // Given
        Long id = 1L;
        Long userId = 10L;
        String name = "test_function";
        String signature = "x^2 + 2*x + 1";

        // When
        FunctionDTO functionDTO = new FunctionDTO(id, userId, name, signature);

        // Then
        assertAll("FunctionDTO properties with ID",
                () -> assertEquals(id, functionDTO.getId(), "ID должен совпадать"),
                () -> assertEquals(userId, functionDTO.getUserId(), "UserId должен совпадать"),
                () -> assertEquals(name, functionDTO.getName(), "Name должен совпадать"),
                () -> assertEquals(signature, functionDTO.getSignature(), "Signature должен совпадать")
        );
    }

    @Test
    @DisplayName("FunctionDTO - создание без ID")
    void testFunctionDTO_CreationWithoutId() {
        // Given
        Long userId = 10L;
        String name = "test_function";
        String signature = "x^2 + 2*x + 1";

        // When
        FunctionDTO functionDTO = new FunctionDTO(userId, name, signature);

        // Then
        assertAll("FunctionDTO properties without ID",
                () -> assertNull(functionDTO.getId(), "ID должен быть null"),
                () -> assertEquals(userId, functionDTO.getUserId(), "UserId должен совпадать"),
                () -> assertEquals(name, functionDTO.getName(), "Name должен совпадать"),
                () -> assertEquals(signature, functionDTO.getSignature(), "Signature должен совпадать")
        );
    }

    @Test
    @DisplayName("FunctionDTO - создание через пустой конструктор и сеттеры")
    void testFunctionDTO_EmptyConstructorAndSetters() {
        // Given
        FunctionDTO functionDTO = new FunctionDTO();

        // When
        functionDTO.setId(2L);
        functionDTO.setUserId(20L);
        functionDTO.setName("new_function");
        functionDTO.setSignature("sin(x) + cos(x)");

        // Then
        assertAll("FunctionDTO empty constructor and setters",
                () -> assertEquals(2L, functionDTO.getId(), "ID должен обновиться"),
                () -> assertEquals(20L, functionDTO.getUserId(), "UserId должен обновиться"),
                () -> assertEquals("new_function", functionDTO.getName(), "Name должен обновиться"),
                () -> assertEquals("sin(x) + cos(x)", functionDTO.getSignature(), "Signature должен обновиться")
        );
    }

    @Test
    @DisplayName("FunctionDTO - equals и hashCode корректно работают")
    void testFunctionDTO_EqualsAndHashCode() {
        // Given
        FunctionDTO function1 = new FunctionDTO(1L, 10L, "function1", "x^2");
        FunctionDTO function2 = new FunctionDTO(1L, 10L, "function1", "x^2");
        FunctionDTO function3 = new FunctionDTO(2L, 20L, "function2", "x^3");
        FunctionDTO function4 = new FunctionDTO(10L, "function1", "x^2"); // без ID

        // Then
        assertAll("FunctionDTO equals and hashCode",
                () -> assertEquals(function1, function2, "Объекты с одинаковыми полями должны быть равны"),
                () -> assertNotEquals(function1, function3, "Объекты с разными полями не должны быть равны"),
                () -> assertEquals(function1.hashCode(), function2.hashCode(), "HashCode должен совпадать для равных объектов"),
                () -> assertNotEquals(function1.hashCode(), function3.hashCode(), "HashCode должен отличаться для разных объектов"),
                () -> assertNotEquals(function1, function4, "Объекты с разными ID не должны быть равны")
        );
    }

    @Test
    @DisplayName("FunctionDTO - toString содержит важную информацию")
    void testFunctionDTO_ToStringContainsImportantInfo() {
        // Given
        FunctionDTO functionDTO = new FunctionDTO(1L, 10L, "test_function", "x^2");

        // When
        String toString = functionDTO.toString();

        // Then
        assertAll("FunctionDTO toString",
                () -> assertTrue(toString.contains("id=1"), "toString должен содержать ID"),
                () -> assertTrue(toString.contains("userId=10"), "toString должен содержать userId"),
                () -> assertTrue(toString.contains("name='test_function'"), "toString должен содержать имя"),
                () -> assertTrue(toString.contains("signature='x^2'"), "toString должен содержать signature"),
                () -> assertTrue(toString.startsWith("FunctionDTO{"), "toString должен начинаться с имени класса"),
                () -> assertTrue(toString.endsWith("}"), "toString должен заканчиваться фигурной скобкой")
        );
    }

    @Test
    @DisplayName("FunctionDTO - создание с null значениями")
    void testFunctionDTO_CreationWithNullValues() {
        // Given & When
        FunctionDTO functionDTO = new FunctionDTO(null, null, null, null);

        // Then
        assertAll("FunctionDTO with null values",
                () -> assertNull(functionDTO.getId(), "ID должен быть null"),
                () -> assertNull(functionDTO.getUserId(), "UserId должен быть null"),
                () -> assertNull(functionDTO.getName(), "Name должен быть null"),
                () -> assertNull(functionDTO.getSignature(), "Signature должен быть null")
        );
    }

    @Test
    @DisplayName("FunctionDTO - создание с пустыми строками")
    void testFunctionDTO_CreationWithEmptyStrings() {
        // Given & When
        FunctionDTO functionDTO = new FunctionDTO(1L, 10L, "", "");

        // Then
        assertAll("FunctionDTO with empty strings",
                () -> assertEquals("", functionDTO.getName(), "Name должен быть пустой строкой"),
                () -> assertEquals("", functionDTO.getSignature(), "Signature должен быть пустой строкой")
        );
    }

    @Test
    @DisplayName("FunctionDTO - создание с максимальными значениями")
    void testFunctionDTO_CreationWithMaxValues() {
        // Given
        Long maxId = Long.MAX_VALUE;
        Long maxUserId = Long.MAX_VALUE;
        String longName = "a".repeat(255);
        String longSignature = "f(x)=" + "x".repeat(1000);

        // When
        FunctionDTO functionDTO = new FunctionDTO(maxId, maxUserId, longName, longSignature);

        // Then
        assertAll("FunctionDTO with max values",
                () -> assertEquals(maxId, functionDTO.getId(), "Должен поддерживать максимальный Long ID"),
                () -> assertEquals(maxUserId, functionDTO.getUserId(), "Должен поддерживать максимальный Long userId"),
                () -> assertEquals(longName, functionDTO.getName(), "Должен поддерживать длинные имена"),
                () -> assertEquals(longSignature, functionDTO.getSignature(), "Должен поддерживать длинные signature")
        );
    }

    @Test
    @DisplayName("FunctionDTO - equals с null и другим классом")
    void testFunctionDTO_EqualsWithNullAndOtherClass() {
        // Given
        FunctionDTO function = new FunctionDTO(1L, 10L, "test", "x^2");

        // Then
        assertAll("FunctionDTO equals edge cases",
                () -> assertNotEquals(function, null, "Не должен быть равен null"),
                () -> assertNotEquals(function, "string", "Не должен быть равен объекту другого класса"),
                () -> assertEquals(function, function, "Должен быть равен самому себе")
        );
    }

    @Test
    @DisplayName("FunctionDTO - создание с пробелами в строках")
    void testFunctionDTO_CreationWithSpacesInStrings() {
        // Given & When
        FunctionDTO functionDTO = new FunctionDTO(1L, 10L, "  test  function  ", "  x^2 + 2*x + 1  ");

        // Then
        assertAll("FunctionDTO with spaces",
                () -> assertEquals("  test  function  ", functionDTO.getName(), "Должен сохранять пробелы в имени"),
                () -> assertEquals("  x^2 + 2*x + 1  ", functionDTO.getSignature(), "Должен сохранять пробелы в signature")
        );
    }

    @Test
    @DisplayName("FunctionDTO - создание со специальными символами")
    void testFunctionDTO_CreationWithSpecialCharacters() {
        // Given
        String nameWithSpecialChars = "func_αβγ_123";
        String signatureWithSpecialChars = "f(x) = ∫(x² + √x) dx";

        // When
        FunctionDTO functionDTO = new FunctionDTO(1L, 10L, nameWithSpecialChars, signatureWithSpecialChars);

        // Then
        assertAll("FunctionDTO with special characters",
                () -> assertEquals(nameWithSpecialChars, functionDTO.getName(), "Должен поддерживать специальные символы в имени"),
                () -> assertEquals(signatureWithSpecialChars, functionDTO.getSignature(), "Должен поддерживать специальные символы в signature")
        );
    }
}