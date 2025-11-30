package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DTO.UserDTO;
import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DTOTransformationServiceTest {

    @Autowired
    private DTOTransformationService transformationService;

    // === TRANSFORM MAP TO DTO TESTS ===

    @Test
    void testTransformUserMapToDTO() {
        // Given
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1L);
        userData.put("login", "testuser");
        userData.put("role", "ADMIN");
        userData.put("password", "password123");

        // When
        UserDTO result = transformationService.transformToUserDTO(userData);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getLogin());
        assertEquals("ADMIN", result.getRole());
        assertEquals("password123", result.getPassword());
    }

    @Test
    void testTransformUserMapToDTO_WithMissingFields() {
        // Given
        Map<String, Object> userData = new HashMap<>();
        userData.put("login", "testuser");
        // id, role, password missing

        // When
        UserDTO result = transformationService.transformToUserDTO(userData);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("testuser", result.getLogin());
        assertNull(result.getRole());
        assertNull(result.getPassword());
    }

    @Test
    void testTransformFunctionMapToDTO() {
        // Given
        Map<String, Object> functionData = new HashMap<>();
        functionData.put("id", 1L);
        functionData.put("userId", 2L);
        functionData.put("name", "testFunction");
        functionData.put("signature", "f(x) = x^2");

        // When
        FunctionDTO result = transformationService.transformToFunctionDTO(functionData);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getUserId());
        assertEquals("testFunction", result.getName());
        assertEquals("f(x) = x^2", result.getSignature());
    }

    @Test
    void testTransformFunctionMapToDTO_WithAlternativeKeys() {
        // Given
        Map<String, Object> functionData = new HashMap<>();
        functionData.put("id", 1L);
        functionData.put("u_id", 2L); // alternative key
        functionData.put("name", "testFunction");
        functionData.put("signature", "f(x) = x^2");

        // When
        FunctionDTO result = transformationService.transformToFunctionDTO(functionData);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getUserId());
        assertEquals("testFunction", result.getName());
        assertEquals("f(x) = x^2", result.getSignature());
    }

    @Test
    void testTransformPointMapToDTO() {
        // Given
        Map<String, Object> pointData = new HashMap<>();
        pointData.put("id", 1L);
        pointData.put("functionId", 2L);
        pointData.put("xValue", 1.5);
        pointData.put("yValue", 2.25);

        // When
        PointDTO result = transformationService.transformToPointDTO(pointData);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getFunctionId());
        assertEquals(1.5, result.getXValue());
        assertEquals(2.25, result.getYValue());
    }

    @Test
    void testTransformPointMapToDTO_WithAlternativeKeys() {
        // Given
        Map<String, Object> pointData = new HashMap<>();
        pointData.put("id", 1L);
        pointData.put("f_id", 2L); // alternative key
        pointData.put("x_value", 1.5); // alternative key
        pointData.put("y_value", 2.25); // alternative key

        // When
        PointDTO result = transformationService.transformToPointDTO(pointData);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getFunctionId());
        assertEquals(1.5, result.getXValue());
        assertEquals(2.25, result.getYValue());
    }

    @Test
    void testTransformPointMapToDTO_WithSpecialDoubleValues() {
        // Given
        Map<String, Object> pointData = new HashMap<>();
        pointData.put("id", 1L);
        pointData.put("functionId", 2L);
        pointData.put("xValue", Double.NaN);
        pointData.put("yValue", Double.POSITIVE_INFINITY);

        // When
        PointDTO result = transformationService.transformToPointDTO(pointData);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getFunctionId());
        assertTrue(Double.isNaN(result.getXValue()));
        assertEquals(Double.POSITIVE_INFINITY, result.getYValue());
    }

    // === TRANSFORM DTO TO MAP TESTS ===

    @Test
    void testTransformUserDTOToMap() {
        // Given
        UserDTO userDTO = new UserDTO(1L, "testuser", "USER", "password123");

        // When
        Map<String, Object> result = transformationService.transformToMap(userDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals("testuser", result.get("login"));
        assertEquals("USER", result.get("role"));
        assertEquals("password123", result.get("password"));
    }

    @Test
    void testTransformUserDTOToMap_WithNullValues() {
        // Given
        UserDTO userDTO = new UserDTO(null, null, null, null);

        // When
        Map<String, Object> result = transformationService.transformToMap(userDTO);

        // Then
        assertNotNull(result);
        assertFalse(result.containsKey("id"));
        assertFalse(result.containsKey("login"));
        assertFalse(result.containsKey("role"));
        assertFalse(result.containsKey("password"));
    }

    @Test
    void testTransformFunctionDTOToMap() {
        // Given
        FunctionDTO functionDTO = new FunctionDTO(1L, 2L, "testFunction", "f(x)=x^2");

        // When
        Map<String, Object> result = transformationService.transformToMap(functionDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals(2L, result.get("userId"));
        assertEquals("testFunction", result.get("name"));
        assertEquals("f(x)=x^2", result.get("signature"));
    }

    @Test
    void testTransformPointDTOToMap() {
        // Given
        PointDTO pointDTO = new PointDTO(1L, 2L, 1.5, 2.25);

        // When
        Map<String, Object> result = transformationService.transformToMap(pointDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals(2L, result.get("functionId"));
        assertEquals(1.5, result.get("xValue"));
        assertEquals(2.25, result.get("yValue"));
    }

    // === TRANSFORM TO MAP WITH ALL FIELDS TESTS ===

    @Test
    void testTransformUserDTOToMapWithAllFields() {
        // Given
        UserDTO userDTO = new UserDTO(null, "testuser", null, "password123");

        // When
        Map<String, Object> result = transformationService.transformToMapWithAllFields(userDTO);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("id"));
        assertEquals("testuser", result.get("login"));
        assertEquals("USER", result.get("role"));
        assertEquals("password123", result.get("password"));
    }

    @Test
    void testTransformFunctionDTOToMapWithAllFields() {
        // Given
        FunctionDTO functionDTO = new FunctionDTO(null, 2L, "testFunction", null);

        // When
        Map<String, Object> result = transformationService.transformToMapWithAllFields(functionDTO);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("id"));
        assertEquals(2L, result.get("userId"));
        assertEquals("testFunction", result.get("name"));
        assertEquals("", result.get("signature"));
    }

    @Test
    void testTransformPointDTOToMapWithAllFields() {
        // Given
        PointDTO pointDTO = new PointDTO(null, 2L, null, 2.25);

        // When
        Map<String, Object> result = transformationService.transformToMapWithAllFields(pointDTO);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("id"));
        assertEquals(2L, result.get("functionId"));
        assertEquals(0.0, result.get("xValue"));
        assertEquals(2.25, result.get("yValue"));
    }

    // === VALIDATION TESTS ===

    @Test
    void testValidUserMap() {
        // Given
        Map<String, Object> validData = Map.of("login", "user", "password", "pass");
        Map<String, Object> invalidData = Map.of("login", "user");
        Map<String, Object> invalidData2 = Map.of("password", "pass");
        Map<String, Object> invalidData3 = new HashMap<>();

        // When & Then
        assertTrue(transformationService.isValidUserMap(validData));
        assertFalse(transformationService.isValidUserMap(invalidData));
        assertFalse(transformationService.isValidUserMap(invalidData2));
        assertFalse(transformationService.isValidUserMap(invalidData3));
        assertFalse(transformationService.isValidUserMap(null));
    }

    @Test
    void testValidFunctionMap() {
        // Given
        Map<String, Object> validData = Map.of("name", "func", "signature", "f(x)", "userId", 1L);
        Map<String, Object> invalidData = Map.of("name", "func", "signature", "f(x)");
        Map<String, Object> invalidData2 = Map.of("name", "func", "userId", 1L);

        // When & Then
        assertTrue(transformationService.isValidFunctionMap(validData));
        assertFalse(transformationService.isValidFunctionMap(invalidData));
        assertFalse(transformationService.isValidFunctionMap(invalidData2));
        assertFalse(transformationService.isValidFunctionMap(null));
    }

    @Test
    void testValidPointMap() {
        // Given
        Map<String, Object> validData = Map.of("xValue", 1.0, "yValue", 2.0, "functionId", 1L);
        Map<String, Object> invalidData = Map.of("xValue", 1.0, "yValue", 2.0);
        Map<String, Object> invalidData2 = Map.of("xValue", 1.0, "functionId", 1L);

        // When & Then
        assertTrue(transformationService.isValidPointMap(validData));
        assertFalse(transformationService.isValidPointMap(invalidData));
        assertFalse(transformationService.isValidPointMap(invalidData2));
        assertFalse(transformationService.isValidPointMap(null));
    }

    // === UTILITY METHODS TESTS ===

    @Test
    void testCreateUserMap() {
        // When
        Map<String, Object> result = transformationService.createUserMap("newuser", "newpass", "ADMIN");

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.get("login"));
        assertEquals("newpass", result.get("password"));
        assertEquals("ADMIN", result.get("role"));
    }

    @Test
    void testCreateUserMap_WithNullRole() {
        // When
        Map<String, Object> result = transformationService.createUserMap("newuser", "newpass", null);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.get("login"));
        assertEquals("newpass", result.get("password"));
        assertEquals("USER", result.get("role"));
    }

    @Test
    void testCreateFunctionMap() {
        // When
        Map<String, Object> result = transformationService.createFunctionMap(1L, "testFunc", "f(x)=x");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("userId"));
        assertEquals("testFunc", result.get("name"));
        assertEquals("f(x)=x", result.get("signature"));
    }

    @Test
    void testCreatePointMap() {
        // When
        Map<String, Object> result = transformationService.createPointMap(1L, 2.5, 6.25);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("functionId"));
        assertEquals(2.5, result.get("xValue"));
        assertEquals(6.25, result.get("yValue"));
    }

    // === EXTRACT SPECIFIC FIELDS TESTS ===

    @Test
    void testExtractLogin() {
        // Given
        Map<String, Object> userData = Map.of("login", "testuser", "password", "pass");

        // When
        String result = transformationService.extractLogin(userData);

        // Then
        assertEquals("testuser", result);
    }

    @Test
    void testExtractLogin_NotFound() {
        // Given
        Map<String, Object> userData = Map.of("username", "testuser", "password", "pass");

        // When
        String result = transformationService.extractLogin(userData);

        // Then
        assertNull(result);
    }

    @Test
    void testExtractFunctionName() {
        // Given
        Map<String, Object> functionData = Map.of("name", "testFunc", "signature", "f(x)");

        // When
        String result = transformationService.extractFunctionName(functionData);

        // Then
        assertEquals("testFunc", result);
    }

    @Test
    void testExtractFunctionId() {
        // Given
        Map<String, Object> pointData = Map.of("functionId", 1L, "xValue", 1.0);

        // When
        Long result = transformationService.extractFunctionId(pointData);

        // Then
        assertEquals(1L, result);
    }

    @Test
    void testExtractFunctionId_NotFound() {
        // Given
        Map<String, Object> pointData = Map.of("f_id", 1L, "xValue", 1.0);

        // When
        Long result = transformationService.extractFunctionId(pointData);

        // Then
        assertNull(result);
    }

    // === EDGE CASES AND ERROR HANDLING ===

    @Test
    void testTransformUserMapToDTO_WithInvalidData() {
        // Given
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("id", "not_a_number"); // Invalid type

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transformationService.transformToUserDTO(invalidData);
        });
    }

    @Test
    void testTransformFunctionMapToDTO_WithInvalidUserId() {
        // Given
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("userId", "invalid_long");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transformationService.transformToFunctionDTO(invalidData);
        });
    }

    @Test
    void testTransformPointMapToDTO_WithInvalidDouble() {
        // Given
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("xValue", "not_a_double");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transformationService.transformToPointDTO(invalidData);
        });
    }

    @Test
    void testTransformEmptyMapToUserDTO() {
        // Given
        Map<String, Object> emptyData = new HashMap<>();

        // When
        UserDTO result = transformationService.transformToUserDTO(emptyData);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getLogin());
        assertNull(result.getRole());
        assertNull(result.getPassword());
    }

    @Test
    void testTransformNullMapToUserDTO() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transformationService.transformToUserDTO(null);
        });
    }

    @Test
    void testTransformUserDTOWithAllNullsToMap() {
        // Given
        UserDTO userDTO = new UserDTO();

        // When
        Map<String, Object> result = transformationService.transformToMap(userDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateUserMapWithEmptyStrings() {
        // When
        Map<String, Object> result = transformationService.createUserMap("", "", "");

        // Then
        assertNotNull(result);
        assertEquals("", result.get("login"));
        assertEquals("", result.get("password"));
        assertEquals("", result.get("role"));
    }

    @Test
    void testCreatePointMapWithSpecialValues() {
        // When
        Map<String, Object> result = transformationService.createPointMap(1L, Double.NaN, Double.POSITIVE_INFINITY);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("functionId"));
        assertTrue(Double.isNaN((Double) result.get("xValue")));
        assertEquals(Double.POSITIVE_INFINITY, result.get("yValue"));
    }
}