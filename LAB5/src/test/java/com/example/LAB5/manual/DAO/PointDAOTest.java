package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DAO.UserDAO;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PointDAOTest {
    private static final Logger logger = LoggerFactory.getLogger(PointDAOTest.class);
    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointDAO pointDAO;
    private Long testUserId;
    private Long testFunctionId;
    private static String testPrefix;

    @BeforeAll
    static void setUpAll() {
        testPrefix = "point_test_" + UUID.randomUUID().toString().substring(0, 8) + "_";
        logger.info("Установлен префикс тестов: {}", testPrefix);
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        functionDAO = new FunctionDAO();
        pointDAO = new PointDAO();

        // Создаем тестового пользователя и функцию
        String username = uniqueUsername("point_test_user");
        testUserId = createTestUser(username, "test_password");

        String functionName = "test_function_points";
        String expression = "x^2";
        testFunctionId = functionDAO.createFunction(functionName, testUserId, expression);

        logger.info("Создана тестовая функция с ID: {} для тестов точек", testFunctionId);
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
    void testCreateAndFindPoint() {
        Double xValue = -2.0;
        Double yValue = 4.0;

        Long pointId = pointDAO.createPoint(testFunctionId, xValue, yValue);

        assertNotNull(pointId);
        assertTrue(pointId > 0);

        Map<String, Object> foundPoint = pointDAO.findById(pointId);
        assertNotNull(foundPoint);
        assertEquals(testFunctionId, foundPoint.get("function_id"));
        assertEquals(xValue, foundPoint.get("x_value"));
        assertEquals(yValue, foundPoint.get("y_value"));
    }

    @Test
    @Order(2)
    void testCreatePointsBatch() {
        List<Object[]> points = new ArrayList<>();
        points.add(new Object[]{testFunctionId, -2.0, 4.0});
        points.add(new Object[]{testFunctionId, -1.0, 1.0});
        points.add(new Object[]{testFunctionId, 0.0, 0.0});
        points.add(new Object[]{testFunctionId, 1.0, 1.0});
        points.add(new Object[]{testFunctionId, 2.0, 4.0});

        int insertedCount = pointDAO.createPointsBatch(points);

        assertEquals(points.size(), insertedCount);

        List<Map<String, Object>> functionPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(points.size(), functionPoints.size());
    }

    @Test
    @Order(3)
    void testFindByFunctionId() {
        // Создаем несколько точек для функции
        pointDAO.createPoint(testFunctionId, -2.0, 4.0);
        pointDAO.createPoint(testFunctionId, -1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 0.0, 0.0);

        // Создаем другую функцию и точку для нее
        String otherFunctionName = "other_function_points";
        Long otherFunctionId = functionDAO.createFunction(otherFunctionName, testUserId, "sin(x)");
        pointDAO.createPoint(otherFunctionId, 0.0, 0.0);

        List<Map<String, Object>> functionPoints = pointDAO.findByFunctionId(testFunctionId);

        assertEquals(3, functionPoints.size());
        functionPoints.forEach(point ->
                assertEquals(testFunctionId, point.get("function_id"))
        );
    }

    @Test
    @Order(4)
    void testFindByFunctionIdAndXRange() {
        // Создаем точки в разных диапазонах x
        pointDAO.createPoint(testFunctionId, -5.0, 25.0);
        pointDAO.createPoint(testFunctionId, -1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 0.0, 0.0);
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 5.0, 25.0);

        List<Map<String, Object>> pointsInRange = pointDAO.findByFunctionIdAndXRange(testFunctionId, -2.0, 2.0);

        assertFalse(pointsInRange.isEmpty());
        pointsInRange.forEach(point -> {
            Double xValue = (Double) point.get("x_value");
            assertTrue(xValue >= -2.0 && xValue <= 2.0);
        });
    }

    @Test
    @Order(5)
    void testFindByYValueGreaterThan() {
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);
        pointDAO.createPoint(testFunctionId, 3.0, 9.0);
        pointDAO.createPoint(testFunctionId, 0.5, 0.25);

        List<Map<String, Object>> pointsWithLargeY = pointDAO.findByYValueGreaterThan(2.0);

        assertFalse(pointsWithLargeY.isEmpty());
        pointsWithLargeY.forEach(point -> {
            Double yValue = (Double) point.get("y_value");
            assertTrue(yValue > 2.0);
        });
    }

    @Test
    @Order(6)
    void testFindByXValue() {
        Double targetX = 3.14;
        pointDAO.createPoint(testFunctionId, targetX, 9.8596);
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        List<Map<String, Object>> pointsWithX = pointDAO.findByXValue(targetX);

        assertFalse(pointsWithX.isEmpty());
        pointsWithX.forEach(point ->
                assertEquals(targetX, point.get("x_value"))
        );
    }

    @Test
    @Order(7)
    void testUpdatePoint() {
        Double originalX = 1.0;
        Double originalY = 1.0;

        Long pointId = pointDAO.createPoint(testFunctionId, originalX, originalY);

        Double updatedX = 2.0;
        Double updatedY = 4.0;
        boolean updated = pointDAO.updatePoint(pointId, updatedX, updatedY);

        assertTrue(updated);

        Map<String, Object> foundPoint = pointDAO.findById(pointId);
        assertNotNull(foundPoint);
        assertEquals(updatedX, foundPoint.get("x_value"));
        assertEquals(updatedY, foundPoint.get("y_value"));
    }

    @Test
    @Order(8)
    void testUpdatePointYValue() {
        Double xValue = 2.0;
        Double originalY = 4.0;

        Long pointId = pointDAO.createPoint(testFunctionId, xValue, originalY);

        Double updatedY = 8.0;
        boolean updated = pointDAO.updatePointYValue(pointId, updatedY);

        assertTrue(updated);

        Map<String, Object> foundPoint = pointDAO.findById(pointId);
        assertNotNull(foundPoint);
        assertEquals(updatedY, foundPoint.get("y_value"));
        assertEquals(xValue, foundPoint.get("x_value")); // X не должен измениться
    }

    @Test
    @Order(9)
    void testUpdatePointsYValueByFunction() {
        // Создаем несколько точек для функции
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);
        pointDAO.createPoint(testFunctionId, 3.0, 9.0);

        Double multiplier = 2.0;
        int updatedCount = pointDAO.updatePointsYValueByFunction(testFunctionId, multiplier);

        assertEquals(3, updatedCount);

        List<Map<String, Object>> updatedPoints = pointDAO.findByFunctionId(testFunctionId);
        updatedPoints.forEach(point -> {
            Double originalY = (Double) point.get("x_value") * (Double) point.get("x_value"); // x^2
            Double expectedY = originalY * multiplier;
            assertEquals(expectedY, point.get("y_value"));
        });
    }

    @Test
    @Order(10)
    void testDeletePoint() {
        Double xValue = 5.0;
        Double yValue = 25.0;

        Long pointId = pointDAO.createPoint(testFunctionId, xValue, yValue);

        boolean deleted = pointDAO.deletePoint(pointId);
        assertTrue(deleted);

        Map<String, Object> deletedPoint = pointDAO.findById(pointId);
        assertNull(deletedPoint);
    }

    @Test
    @Order(11)
    void testDeletePointsByFunctionId() {
        // Создаем точки для тестовой функции
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        // Создаем другую функцию и точку
        String otherFunctionName = "other_function_delete";
        Long otherFunctionId = functionDAO.createFunction(otherFunctionName, testUserId, "cos(x)");
        pointDAO.createPoint(otherFunctionId, 0.0, 1.0);

        // Удаляем точки тестовой функции
        boolean deleted = pointDAO.deletePointsByFunctionId(testFunctionId);
        assertTrue(deleted);

        // Проверяем, что точки тестовой функции удалены
        List<Map<String, Object>> remainingPoints = pointDAO.findByFunctionId(testFunctionId);
        assertTrue(remainingPoints.isEmpty());

        // Проверяем, что точки другой функции остались
        List<Map<String, Object>> otherFunctionPoints = pointDAO.findByFunctionId(otherFunctionId);
        assertFalse(otherFunctionPoints.isEmpty());
    }

    @Test
    @Order(12)
    void testGetPointsOrderedByX() {
        // Создаем точки в разном порядке
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);
        pointDAO.createPoint(testFunctionId, -1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 0.0, 0.0);
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, -2.0, 4.0);

        List<Map<String, Object>> orderedPoints = pointDAO.getPointsOrderedByX(testFunctionId);

        assertEquals(5, orderedPoints.size());

        // Проверяем порядок по возрастанию x
        for (int i = 0; i < orderedPoints.size() - 1; i++) {
            Double currentX = (Double) orderedPoints.get(i).get("x_value");
            Double nextX = (Double) orderedPoints.get(i + 1).get("x_value");
            assertTrue(currentX <= nextX);
        }
    }

    @Test
    @Order(13)
    void testGetFunctionStatistics() {
        // Создаем точки для статистики
        pointDAO.createPoint(testFunctionId, -2.0, 4.0);
        pointDAO.createPoint(testFunctionId, -1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 0.0, 0.0);
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        Map<String, Object> stats = pointDAO.getFunctionStatistics(testFunctionId);

        assertNotNull(stats);
        assertEquals(-2.0, stats.get("min_x"));
        assertEquals(2.0, stats.get("max_x"));
        assertEquals(2.0, stats.get("avg_y")); // (4+1+0+1+4)/5 = 2.0
    }

    @Test
    @Order(14)
    void testGetPointsWithFunctionName() {
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);

        List<Map<String, Object>> pointsWithNames = pointDAO.getPointsWithFunctionName();

        assertFalse(pointsWithNames.isEmpty());
        pointsWithNames.forEach(point -> {
            assertNotNull(point.get("function_name"));
            assertNotNull(point.get("x_value"));
            assertNotNull(point.get("y_value"));
        });
    }

    @Test
    @Order(15)
    void testGetPointCountByFunction() {
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        String otherFunctionName = "count_test_function";
        Long otherFunctionId = functionDAO.createFunction(otherFunctionName, testUserId, "x^3");
        pointDAO.createPoint(otherFunctionId, 1.0, 1.0);

        List<Map<String, Object>> pointCounts = pointDAO.getPointCountByFunction();

        assertFalse(pointCounts.isEmpty());

        // Проверяем, что наша тестовая функция есть в статистике
        boolean foundTestFunction = pointCounts.stream()
                .anyMatch(count -> testFunctionId.equals(count.get("function_id")));
        assertTrue(foundTestFunction);
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
        // Очищаем тестовые точки
        List<Map<String, Object>> allPoints = pointDAO.findAll();
        int deletedPoints = 0;

        for (Map<String, Object> point : allPoints) {
            Long functionId = (Long) point.get("function_id");
            Map<String, Object> function = functionDAO.findById(functionId);
            if (function != null) {
                Long userId = (Long) function.get("user_id");
                Map<String, Object> user = userDAO.findById(userId);
                if (user != null) {
                    String username = (String) user.get("username");
                    if (username != null && username.startsWith(testPrefix)) {
                        pointDAO.deletePoint((Long) point.get("id"));
                        deletedPoints++;
                    }
                }
            }
        }

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

        if (deletedPoints > 0 || deletedFunctions > 0 || deletedUsers > 0) {
            logger.info("Очищено {} точек, {} функций и {} пользователей",
                    deletedPoints, deletedFunctions, deletedUsers);
        }
    }
}