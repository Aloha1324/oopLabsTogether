package com.example.LAB5.manual.DAO;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FunctionDAOTest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDAOTest.class);
    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private Long testUserId;
    private static String testPrefix;

    @BeforeAll
    static void setUpAll() {
        testPrefix = "func_test_" + UUID.randomUUID().toString().substring(0, 8) + "_";
        logger.info("Установлен префикс тестов: {}", testPrefix);
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        functionDAO = new FunctionDAO();

        // Создаем тестового пользователя
        String username = uniqueUsername("func_test_user");
        testUserId = createTestUser(username, "test_password_hash");
        logger.info("Используем пользователя с ID: {} для тестов функций", testUserId);
    }

    private String uniqueUsername(String baseName) {
        return testPrefix + baseName + "_" + System.currentTimeMillis();
    }

    private Long createTestUser(String username, String passwordHash) {
        try {
            return userDAO.createUser(username, passwordHash);
        } catch (Exception e) {
            logger.warn("Не удалось создать пользователя {}, ищем существующего", username);
            Map<String, Object> existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                return (Long) existingUser.get("id");
            } else {
                String fallbackUsername = uniqueUsername("fallback_user");
                return userDAO.createUser(fallbackUsername, passwordHash);
            }
        }
    }

    @Test
    @Order(1)
    void testCreateAndFindFunction() {
        String functionName = "quadratic_function";
        String expression = "x^2 + 2*x + 1";

        Long functionId = functionDAO.createFunction(functionName, testUserId, expression);

        assertNotNull(functionId);
        assertTrue(functionId > 0);

        Map<String, Object> foundFunction = functionDAO.findById(functionId);
        assertNotNull(foundFunction);
        assertEquals(functionName, foundFunction.get("name"));
        assertEquals(testUserId, foundFunction.get("user_id"));
        assertEquals(expression, foundFunction.get("expression"));
    }

    @Test
    @Order(2)
    void testFindByUserId() {
        // Создаем несколько функций для тестового пользователя
        functionDAO.createFunction("linear_func", testUserId, "2*x + 3");
        functionDAO.createFunction("quadratic_func", testUserId, "x^2");
        functionDAO.createFunction("cubic_func", testUserId, "x^3");

        // Создаем другого пользователя и функцию для него
        String otherUsername = uniqueUsername("other_user");
        Long otherUserId = createTestUser(otherUsername, "other_pass");
        functionDAO.createFunction("other_func", otherUserId, "sin(x)");

        List<Map<String, Object>> userFunctions = functionDAO.findByUserId(testUserId);

        assertFalse(userFunctions.isEmpty());
        assertTrue(userFunctions.size() >= 3);
        userFunctions.forEach(func ->
                assertEquals(testUserId, func.get("user_id"))
        );
    }

    @Test
    @Order(3)
    void testFindByName() {
        functionDAO.createFunction("linear_function_test", testUserId, "f(x) = x");
        functionDAO.createFunction("exponential_function_test", testUserId, "f(x) = e^x");
        functionDAO.createFunction("logarithmic_func", testUserId, "f(x) = log(x)");

        List<Map<String, Object>> linearFunctions = functionDAO.findByName("linear");
        List<Map<String, Object>> exponentialFunctions = functionDAO.findByName("exponential");

        assertFalse(linearFunctions.isEmpty());
        assertFalse(exponentialFunctions.isEmpty());

        linearFunctions.forEach(func ->
                assertTrue(((String) func.get("name")).toLowerCase().contains("linear"))
        );
        exponentialFunctions.forEach(func ->
                assertTrue(((String) func.get("name")).toLowerCase().contains("exponential"))
        );
    }

    @Test
    @Order(4)
    void testFindByExpression() {
        functionDAO.createFunction("poly1", testUserId, "x^2 + 3*x + 2");
        functionDAO.createFunction("poly2", testUserId, "2*x^2 + x + 1");
        functionDAO.createFunction("trig", testUserId, "sin(x) + cos(x)");

        List<Map<String, Object>> polynomialFunctions = functionDAO.findByExpression("x^2");
        List<Map<String, Object>> trigFunctions = functionDAO.findByExpression("sin");

        assertFalse(polynomialFunctions.isEmpty());
        assertFalse(trigFunctions.isEmpty());
    }

    @Test
    @Order(5)
    void testFunctionNotFound() {
        Map<String, Object> nonExistentFunction = functionDAO.findById(999999L);
        assertNull(nonExistentFunction);
    }

    @Test
    @Order(6)
    void testUpdateFunction() {
        String originalName = "original_function";
        String originalExpression = "f(x) = x";

        Long functionId = functionDAO.createFunction(originalName, testUserId, originalExpression);

        String updatedName = "updated_function";
        String updatedExpression = "f(x) = x^2 + 1";
        boolean updated = functionDAO.updateFunction(functionId, updatedName, testUserId, updatedExpression);

        assertTrue(updated);

        Map<String, Object> foundFunction = functionDAO.findById(functionId);
        assertNotNull(foundFunction);
        assertEquals(updatedName, foundFunction.get("name"));
        assertEquals(updatedExpression, foundFunction.get("expression"));
    }

    @Test
    @Order(7)
    void testUpdateFunctionExpression() {
        String functionName = "expression_test";
        String originalExpression = "x + 1";

        Long functionId = functionDAO.createFunction(functionName, testUserId, originalExpression);

        String updatedExpression = "2*x + 3";
        boolean updated = functionDAO.updateFunctionExpression(functionId, updatedExpression);

        assertTrue(updated);

        Map<String, Object> foundFunction = functionDAO.findById(functionId);
        assertNotNull(foundFunction);
        assertEquals(updatedExpression, foundFunction.get("expression"));
        assertEquals(functionName, foundFunction.get("name")); // Имя не должно измениться
    }

    @Test
    @Order(8)
    void testDeleteFunction() {
        String functionName = "function_to_delete";
        String expression = "f(x) = x^3";

        Long functionId = functionDAO.createFunction(functionName, testUserId, expression);

        boolean deleted = functionDAO.deleteFunction(functionId);
        assertTrue(deleted);

        Map<String, Object> deletedFunction = functionDAO.findById(functionId);
        assertNull(deletedFunction);
    }

    @Test
    @Order(9)
    void testDeleteFunctionsByUserId() {
        // Создаем функции для тестового пользователя
        functionDAO.createFunction("func1", testUserId, "x");
        functionDAO.createFunction("func2", testUserId, "x^2");

        // Создаем другого пользователя и функцию
        String otherUsername = uniqueUsername("user_for_delete_test");
        Long otherUserId = createTestUser(otherUsername, "pass");
        functionDAO.createFunction("other_func", otherUserId, "sin(x)");

        // Удаляем функции тестового пользователя
        boolean deleted = functionDAO.deleteFunctionsByUserId(testUserId);
        assertTrue(deleted);

        // Проверяем, что функции тестового пользователя удалены
        List<Map<String, Object>> remainingFunctions = functionDAO.findByUserId(testUserId);
        assertTrue(remainingFunctions.isEmpty());

        // Проверяем, что функция другого пользователя осталась
        List<Map<String, Object>> otherUserFunctions = functionDAO.findByUserId(otherUserId);
        assertFalse(otherUserFunctions.isEmpty());
    }

    @Test
    @Order(10)
    void testFindAllFunctions() {
        int initialCount = functionDAO.findAll().size();

        functionDAO.createFunction("find_all_1", testUserId, "x + 1");
        functionDAO.createFunction("find_all_2", testUserId, "x^2 + 2");
        functionDAO.createFunction("find_all_3", testUserId, "sin(x)");

        List<Map<String, Object>> allFunctions = functionDAO.findAll();

        assertFalse(allFunctions.isEmpty());
        assertTrue(allFunctions.size() >= initialCount + 3);
        logger.info("Найдено {} функций", allFunctions.size());
    }

    @Test
    @Order(11)
    void testGetFunctionsWithUsername() {
        functionDAO.createFunction("func_with_username", testUserId, "x^2");

        List<Map<String, Object>> functionsWithUsernames = functionDAO.getFunctionsWithUsername();

        assertFalse(functionsWithUsernames.isEmpty());
        functionsWithUsernames.forEach(func -> {
            assertNotNull(func.get("username"));
            assertNotNull(func.get("name"));
        });
    }

    @Test
    @Order(12)
    void testGetFunctionCountByUser() {
        functionDAO.createFunction("count_func1", testUserId, "x");
        functionDAO.createFunction("count_func2", testUserId, "x^2");

        String otherUsername = uniqueUsername("count_test_user");
        Long otherUserId = createTestUser(otherUsername, "pass");
        functionDAO.createFunction("other_count_func", otherUserId, "sin(x)");

        List<Map<String, Object>> functionCounts = functionDAO.getFunctionCountByUser();

        assertFalse(functionCounts.isEmpty());

        // Проверяем, что наш тестовый пользователь есть в статистике
        boolean foundTestUser = functionCounts.stream()
                .anyMatch(count -> testUserId.equals(count.get("user_id")));
        assertTrue(foundTestUser);
    }

    @AfterEach
    void tearDown() {
        try {
            // Очистка тестовых данных
            cleanTestData();
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private void cleanTestData() {
        // Очищаем тестовые функции
        List<Map<String, Object>> allFunctions = functionDAO.findAll();
        int deletedFunctions = 0;

        for (Map<String, Object> function : allFunctions) {
            Long userId = (Long) function.get("user_id");
            Map<String, Object> user = userDAO.findById(userId);
            if (user != null) {
                String username = (String) user.get("username");
                if (username != null && username.startsWith(testPrefix)) {
                    functionDAO.deleteFunction((Long) function.get("id"));
                    deletedFunctions++;
                }
            }
        }

        // Очищаем тестовых пользователей
        List<Map<String, Object>> allUsers = userDAO.findAll();
        int deletedUsers = 0;

        for (Map<String, Object> user : allUsers) {
            String username = (String) user.get("username");
            if (username != null && username.startsWith(testPrefix)) {
                userDAO.deleteUser((Long) user.get("id"));
                deletedUsers++;
            }
        }

        if (deletedFunctions > 0 || deletedUsers > 0) {
            logger.info("Очищено {} функций и {} пользователей", deletedFunctions, deletedUsers);
        }
    }
}