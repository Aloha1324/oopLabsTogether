package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DAO.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FunctionServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionServiceTest.class);
    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointDAO pointDAO;
    private FunctionService functionService;
    private UserService userService;
    private Long testUserId;
    private String uniqueTestLogin;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        functionDAO = new FunctionDAO();
        pointDAO = new PointDAO();
        functionService = new FunctionService(functionDAO, userDAO, pointDAO);
        userService = new UserService(userDAO);

        uniqueTestLogin = "func_service_test_" + UUID.randomUUID().toString().substring(0, 8);
        testUserId = userService.createUser(uniqueTestLogin, "test_pass");
        logger.info("Создан тестовый пользователь с ID: {} и логином: {}", testUserId, uniqueTestLogin);
    }

    @AfterEach
    void tearDown() {
        if (testUserId != null) {
            try {
                userService.deleteUser(testUserId);
                logger.info("Удален тестовый пользователь с ID: {}", testUserId);
            } catch (Exception e) {
                logger.warn("Не удалось удалить тестового пользователя: {}", e.getMessage());
            }
        }
    }

    @Test
    void testCreateFunction() {
        String functionName = "test_function_" + UUID.randomUUID().toString().substring(0, 8);
        String functionExpression = "f(x) = x^2";

        Long functionId = functionService.createFunction(functionName, testUserId, functionExpression);

        assertNotNull(functionId, "ID функции не должен быть null");
        assertTrue(functionId > 0, "ID функции должен быть положительным числом");

        Map<String, Object> createdFunction = functionService.getFunctionById(functionId);
        assertNotNull(createdFunction, "Функция должна быть найдена в базе данных");
        assertEquals(functionName, createdFunction.get("name"), "Имя функции должно совпадать");
        assertEquals(functionExpression, createdFunction.get("expression"), "Выражение функции должно совпадать");
        assertEquals(testUserId, createdFunction.get("user_id"), "ID пользователя должно совпадать");
    }

    @Test
    void testGetFunctionById() {
        String functionName = "get_by_id_test_" + UUID.randomUUID().toString().substring(0, 8);
        Long functionId = functionService.createFunction(functionName, testUserId, "f(x) = x^3");

        Map<String, Object> function = functionService.getFunctionById(functionId);

        assertNotNull(function, "Функция должна быть найдена");
        assertEquals(functionId, function.get("id"), "ID функции должно совпадать");
        assertEquals(functionName, function.get("name"), "Имя функции должно совпадать");
        assertEquals(testUserId, function.get("user_id"), "ID пользователя должно совпадать");
    }

    @Test
    void testGetFunctionByIdNotFound() {
        Long nonExistentFunctionId = -1L;

        Map<String, Object> result = functionService.getFunctionById(nonExistentFunctionId);
        assertNull(result, "Для несуществующего ID функция не должна быть найдена");
    }

    @Test
    void testGetFunctionsByUserId() {
        String functionName1 = "user_func_1_" + UUID.randomUUID().toString().substring(0, 8);
        String functionName2 = "user_func_2_" + UUID.randomUUID().toString().substring(0, 8);

        functionService.createFunction(functionName1, testUserId, "f(x) = x");
        functionService.createFunction(functionName2, testUserId, "f(x) = x^2");

        List<Map<String, Object>> functions = functionService.getFunctionsByUserId(testUserId);

        assertNotNull(functions, "Список функций не должен быть null");
        assertTrue(functions.size() >= 2, "Должно быть найдено как минимум 2 функции");

        functions.forEach(func -> {
            assertEquals(testUserId, func.get("user_id"), "Все функции должны принадлежать тестовому пользователю");
        });
    }

    @Test
    void testGetFunctionsByName() {
        String baseName = "search_func_" + UUID.randomUUID().toString().substring(0, 8);
        String functionName1 = baseName + "_linear";
        String functionName2 = baseName + "_quadratic";
        String functionName3 = "other_func_" + UUID.randomUUID().toString().substring(0, 8);

        functionService.createFunction(functionName1, testUserId, "f(x) = x");
        functionService.createFunction(functionName2, testUserId, "f(x) = x^2");
        functionService.createFunction(functionName3, testUserId, "f(x) = x^3");

        List<Map<String, Object>> functions = functionService.getFunctionsByName(baseName);

        assertNotNull(functions, "Список функций не должен быть null");
        assertEquals(2, functions.size(), "Должно быть найдено 2 функции с указанным паттерном");

        functions.forEach(func -> {
            String name = (String) func.get("name");
            assertTrue(name.contains(baseName), "Имя функции должно содержать паттерн поиска");
        });
    }

    @Test
    void testGetAllFunctions() {
        int initialCount = functionService.getAllFunctions().size();

        String functionName1 = "all_funcs_1_" + UUID.randomUUID().toString().substring(0, 8);
        String functionName2 = "all_funcs_2_" + UUID.randomUUID().toString().substring(0, 8);

        functionService.createFunction(functionName1, testUserId, "f(x) = x");
        functionService.createFunction(functionName2, testUserId, "f(x) = x^2");

        List<Map<String, Object>> allFunctions = functionService.getAllFunctions();

        assertNotNull(allFunctions, "Список всех функций не должен быть null");
        assertTrue(allFunctions.size() >= initialCount + 2, "Должно быть найдено как минимум на 2 функции больше");
    }

    @Test
    void testGetFunctionsWithPointCount() {
        String functionName = "point_count_test_" + UUID.randomUUID().toString().substring(0, 8);
        Long functionId = functionService.createFunction(functionName, testUserId, "f(x) = x");

        // Создаем точки для функции
        PointService pointService = new PointService(pointDAO, functionDAO);
        pointService.createPoint(functionId, 1.0, 1.0);
        pointService.createPoint(functionId, 2.0, 2.0);

        List<Map<String, Object>> functions = functionService.getFunctionsWithPointCount();

        assertNotNull(functions, "Список функций со статистикой не должен быть null");
        assertFalse(functions.isEmpty(), "Список функций не должен быть пустым");
    }

    @Test
    void testUpdateFunction() {
        String originalName = "to_update_func_" + UUID.randomUUID().toString().substring(0, 8);
        Long functionId = functionService.createFunction(originalName, testUserId, "f(x) = x");

        String updatedName = "updated_func_" + UUID.randomUUID().toString().substring(0, 8);
        String updatedExpression = "f(x) = x^3";
        boolean updated = functionService.updateFunction(functionId, updatedName, testUserId, updatedExpression);

        assertTrue(updated, "Функция должна быть успешно обновлена");

        Map<String, Object> updatedFunction = functionService.getFunctionById(functionId);
        assertNotNull(updatedFunction, "Обновленная функция должна быть найдена");
        assertEquals(updatedName, updatedFunction.get("name"), "Имя функции должно быть обновлено");
        assertEquals(updatedExpression, updatedFunction.get("expression"), "Выражение функции должно быть обновлено");
    }

    @Test
    void testUpdateFunctionExpression() {
        String functionName = "expr_update_test_" + UUID.randomUUID().toString().substring(0, 8);
        Long functionId = functionService.createFunction(functionName, testUserId, "f(x) = x");

        String newExpression = "f(x) = sin(x)";
        boolean updated = functionService.updateFunctionExpression(functionId, newExpression);

        assertTrue(updated, "Выражение функции должно быть успешно обновлено");

        Map<String, Object> updatedFunction = functionService.getFunctionById(functionId);
        assertNotNull(updatedFunction, "Функция должна быть найдена после обновления");
        assertEquals(newExpression, updatedFunction.get("expression"), "Выражение функции должно быть обновлено");
    }

    @Test
    void testDeleteFunction() {
        String functionName = "to_delete_func_" + UUID.randomUUID().toString().substring(0, 8);
        Long functionId = functionService.createFunction(functionName, testUserId, "f(x) = x");

        // Создаем точки для проверки каскадного удаления
        PointService pointService = new PointService(pointDAO, functionDAO);
        pointService.createPoint(functionId, 1.0, 1.0);

        boolean deleted = functionService.deleteFunction(functionId);
        assertTrue(deleted, "Функция должна быть успешно удалена");

        Map<String, Object> deletedFunction = functionService.getFunctionById(functionId);
        assertNull(deletedFunction, "Функция не должна быть найдена после удаления");

        // Проверяем, что точки тоже удалены
        List<Map<String, Object>> points = pointService.getPointsByFunctionId(functionId);
        assertTrue(points.isEmpty(), "Точки функции должны быть удалены");
    }

    @Test
    void testDeleteFunctionsByUserId() {
        String functionName1 = "user_del_1_" + UUID.randomUUID().toString().substring(0, 8);
        String functionName2 = "user_del_2_" + UUID.randomUUID().toString().substring(0, 8);

        functionService.createFunction(functionName1, testUserId, "f(x) = x");
        functionService.createFunction(functionName2, testUserId, "f(x) = x^2");

        int deletedCount = functionService.deleteFunctionsByUserId(testUserId);
        assertTrue(deletedCount >= 2, "Должно быть удалено как минимум 2 функции");

        List<Map<String, Object>> remainingFunctions = functionService.getFunctionsByUserId(testUserId);
        assertTrue(remainingFunctions.isEmpty(), "У пользователя не должно остаться функций");
    }

    @Test
    void testValidateFunctionName() {
        String functionName = "unique_test_" + UUID.randomUUID().toString().substring(0, 8);

        // Проверяем до создания функции
        boolean isUniqueBefore = functionService.validateFunctionName(testUserId, functionName);
        assertTrue(isUniqueBefore, "Имя функции должно быть уникальным до создания");

        // Создаем функцию
        functionService.createFunction(functionName, testUserId, "f(x) = x");

        // Проверяем после создания функции
        boolean isUniqueAfter = functionService.validateFunctionName(testUserId, functionName);
        assertFalse(isUniqueAfter, "Имя функции не должно быть уникальным после создания");
    }

    @Test
    void testGetFunctionStatistics() {
        String functionName = "stats_test_" + UUID.randomUUID().toString().substring(0, 8);
        Long functionId = functionService.createFunction(functionName, testUserId, "f(x) = x");

        PointService pointService = new PointService(pointDAO, functionDAO);
        pointService.generateFunctionPoints(functionId, "linear", -5, 5, 1);

        FunctionService.FunctionStatistics stats = functionService.getFunctionStatistics(functionId);

        assertNotNull(stats, "Статистика не должна быть null");
        assertEquals(11, stats.getPointCount(), "Количество точек должно быть 11 (от -5 до 5 включительно)");
        assertEquals(-5.0, stats.getMinX(), 0.001, "Минимальное значение X должно быть -5");
        assertEquals(5.0, stats.getMaxX(), 0.001, "Максимальное значение X должно быть 5");
        assertEquals(-5.0, stats.getMinY(), 0.001, "Минимальное значение Y должно быть -5");
        assertEquals(5.0, stats.getMaxY(), 0.001, "Максимальное значение Y должно быть 5");
        assertEquals(functionName, stats.getFunctionName(), "Имя функции должно совпадать");
    }

    @Test
    void testGetFunctionStatisticsForNonExistentFunction() {
        Long nonExistentFunctionId = -1L;

        FunctionService.FunctionStatistics stats = functionService.getFunctionStatistics(nonExistentFunctionId);
        assertNull(stats, "Статистика должна быть null для несуществующей функции");
    }

    @Test
    void testCreateFunctionWithInvalidUser() {
        Long invalidUserId = -1L;

        assertThrows(Exception.class, () -> {
            functionService.createFunction("test_function", invalidUserId, "f(x) = x^2");
        }, "Должно быть выброшено исключение при создании функции с несуществующим пользователем");
    }

    @Test
    void testGetFunctionsWithUsername() {
        String functionName = "with_username_test_" + UUID.randomUUID().toString().substring(0, 8);
        functionService.createFunction(functionName, testUserId, "f(x) = x");

        List<Map<String, Object>> functions = functionService.getFunctionsWithUsername();

        assertNotNull(functions, "Список функций с именами пользователей не должен быть null");
        assertFalse(functions.isEmpty(), "Список функций не должен быть пустым");

        // Проверяем, что у функций есть поле username
        functions.forEach(func -> {
            assertNotNull(func.get("username"), "У функции должно быть поле username");
        });
    }

    @Test
    void testSearchFunctionsByExpression() {
        String baseExpression = "x^2 + " + UUID.randomUUID().toString().substring(0, 8);
        String expression1 = baseExpression + " * 2";
        String expression2 = baseExpression + " * 3";
        String expression3 = "sin(x)";

        functionService.createFunction("search_expr_1", testUserId, expression1);
        functionService.createFunction("search_expr_2", testUserId, expression2);
        functionService.createFunction("search_expr_3", testUserId, expression3);

        List<Map<String, Object>> foundFunctions = functionService.searchFunctionsByExpression(baseExpression);

        assertNotNull(foundFunctions, "Список найденных функций не должен быть null");
        assertEquals(2, foundFunctions.size(), "Должно быть найдено 2 функции с указанным выражением");

        foundFunctions.forEach(func -> {
            String expression = (String) func.get("expression");
            assertTrue(expression.contains(baseExpression), "Выражение должно содержать паттерн поиска");
        });
    }

    @Test
    void testGetFunctionCountByUser() {
        String functionName1 = "count_test_1_" + UUID.randomUUID().toString().substring(0, 8);
        String functionName2 = "count_test_2_" + UUID.randomUUID().toString().substring(0, 8);

        functionService.createFunction(functionName1, testUserId, "f(x) = x");
        functionService.createFunction(functionName2, testUserId, "f(x) = x^2");

        int count = functionService.getFunctionCountByUser(testUserId);
        assertTrue(count >= 2, "Количество функций пользователя должно быть как минимум 2");
    }
}