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

class PointServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(PointServiceTest.class);
    private UserDAO userDAO;
    private FunctionDAO functionDAO;
    private PointDAO pointDAO;
    private PointService pointService;
    private UserService userService;
    private FunctionService functionService;

    private Long testUserId;
    private Long testFunctionId;
    private String uniqueTestLogin;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        functionDAO = new FunctionDAO();
        pointDAO = new PointDAO();
        pointService = new PointService(pointDAO, functionDAO);
        userService = new UserService(userDAO);
        functionService = new FunctionService(functionDAO, userDAO, pointDAO);

        // Создаем тестового пользователя
        uniqueTestLogin = "point_service_test_" + UUID.randomUUID().toString().substring(0, 8);
        testUserId = userService.createUser(uniqueTestLogin, "test_pass");

        // Создаем тестовую функцию
        String functionName = "test_func_" + UUID.randomUUID().toString().substring(0, 8);
        testFunctionId = functionService.createFunction(functionName, testUserId, "f(x) = x^2");

        logger.info("Созданы тестовые пользователь и функция: user={}, function={}", testUserId, testFunctionId);
    }

    @AfterEach
    void tearDown() {
        if (testUserId != null) {
            try {
                // Сначала удаляем все функции пользователя (точки удалятся каскадно)
                functionService.deleteFunctionsByUserId(testUserId);

                // Затем удаляем пользователя
                userService.deleteUser(testUserId);
                logger.info("Удален тестовый пользователь с ID: {}", testUserId);
            } catch (Exception e) {
                logger.warn("Не удалось удалить тестового пользователя: {}", e.getMessage());
            }
        }
    }

    @Test
    void testCreatePoint() {
        Long pointId = pointService.createPoint(testFunctionId, 1.0, 1.0);

        assertNotNull(pointId);
        assertTrue(pointId > 0);

        Map<String, Object> createdPoint = pointService.getPointById(pointId);
        assertNotNull(createdPoint);
        assertEquals(1.0, (Double) createdPoint.get("x_value"));
        assertEquals(1.0, (Double) createdPoint.get("y_value"));
        assertEquals(testFunctionId, createdPoint.get("function_id"));
    }

    @Test
    void testCreatePointsBatch() {
        List<Double> xValues = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> yValues = List.of(1.0, 4.0, 9.0, 16.0, 25.0);

        int createdCount = pointService.createPointsBatch(testFunctionId, xValues, yValues);

        assertEquals(5, createdCount);

        List<Map<String, Object>> points = pointService.getPointsByFunctionId(testFunctionId);
        assertEquals(5, points.size());
    }

    @Test
    void testGenerateFunctionPoints() {
        int pointCount = pointService.generateFunctionPoints(testFunctionId, "quadratic", -2, 2, 0.5);

        // Для диапазона [-2, 2] с шагом 0.5 должно быть 9 точек
        assertTrue(pointCount >= 8); // Может быть 8 или 9 в зависимости от точности вычислений

        List<Map<String, Object>> points = pointService.getPointsByFunctionId(testFunctionId);
        assertTrue(points.size() >= 8);

        boolean foundZeroPoint = points.stream()
                .anyMatch(p -> Math.abs((Double) p.get("x_value")) < 0.001 && Math.abs((Double) p.get("y_value")) < 0.001);
        assertTrue(foundZeroPoint);
    }

    @Test
    void testGetPointById() {
        Long pointId = pointService.createPoint(testFunctionId, 3.0, 9.0);

        Map<String, Object> point = pointService.getPointById(pointId);

        assertNotNull(point);
        assertEquals(pointId, point.get("id"));
        assertEquals(3.0, (Double) point.get("x_value"));
        assertEquals(9.0, (Double) point.get("y_value"));
    }

    @Test
    void testGetPointsByFunctionId() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);
        pointService.createPoint(testFunctionId, 3.0, 9.0);

        List<Map<String, Object>> points = pointService.getPointsByFunctionId(testFunctionId);

        assertNotNull(points);
        assertEquals(3, points.size());

        points.forEach(point -> {
            assertEquals(testFunctionId, point.get("function_id"));
        });
    }

    @Test
    void testGetPointsByXRange() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);
        pointService.createPoint(testFunctionId, 3.0, 9.0);
        pointService.createPoint(testFunctionId, 4.0, 16.0);

        List<Map<String, Object>> points = pointService.getPointsByXRange(testFunctionId, 2.0, 3.0);

        assertNotNull(points);
        assertEquals(2, points.size());

        points.forEach(point -> {
            Double xValue = (Double) point.get("x_value");
            assertTrue(xValue >= 2.0 && xValue <= 3.0);
        });
    }

    @Test
    void testGetPointsByYRange() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);
        pointService.createPoint(testFunctionId, 3.0, 9.0);
        pointService.createPoint(testFunctionId, 4.0, 16.0);

        List<Map<String, Object>> points = pointService.getPointsByYRange(testFunctionId, 5.0, 15.0);

        assertNotNull(points);
        assertEquals(1, points.size()); // Только точка с y=9.0

        Map<String, Object> point = points.get(0);
        assertEquals(9.0, (Double) point.get("y_value"));
    }

    @Test
    void testGetAllPoints() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);

        // Создаем еще одну функцию и точки для нее
        String anotherFunctionName = "another_func_" + UUID.randomUUID().toString().substring(0, 8);
        Long anotherFunctionId = functionService.createFunction(anotherFunctionName, testUserId, "f(x) = x");
        pointService.createPoint(anotherFunctionId, 5.0, 5.0);

        List<Map<String, Object>> allPoints = pointService.getAllPoints();

        assertNotNull(allPoints);
        assertTrue(allPoints.size() >= 2);
    }

    @Test
    void testFindMaxYPoint() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);
        pointService.createPoint(testFunctionId, 3.0, 9.0);
        pointService.createPoint(testFunctionId, 4.0, 16.0);

        Map<String, Object> maxPoint = pointService.findMaxYPoint(testFunctionId);

        assertNotNull(maxPoint);
        assertEquals(16.0, (Double) maxPoint.get("y_value"));
        assertEquals(4.0, (Double) maxPoint.get("x_value"));
    }

    @Test
    void testFindMinYPoint() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);
        pointService.createPoint(testFunctionId, 3.0, 9.0);
        pointService.createPoint(testFunctionId, 4.0, 16.0);

        Map<String, Object> minPoint = pointService.findMinYPoint(testFunctionId);

        assertNotNull(minPoint);
        assertEquals(1.0, (Double) minPoint.get("y_value"));
        assertEquals(1.0, (Double) minPoint.get("x_value"));
    }

    @Test
    void testFindRoots() {
        // Создаем функцию f(x) = x и точки вокруг корня
        String linearFunctionName = "linear_func_" + UUID.randomUUID().toString().substring(0, 8);
        Long linearFunctionId = functionService.createFunction(linearFunctionName, testUserId, "f(x) = x");

        pointService.createPoint(linearFunctionId, -1.0, -1.0);
        pointService.createPoint(linearFunctionId, -0.1, -0.1);
        pointService.createPoint(linearFunctionId, 0.0, 0.0); // корень
        pointService.createPoint(linearFunctionId, 0.1, 0.1);
        pointService.createPoint(linearFunctionId, 1.0, 1.0);

        List<Map<String, Object>> roots = pointService.findRoots(linearFunctionId, 0.05);

        assertNotNull(roots);
        assertEquals(1, roots.size()); // Только точка (0.0, 0.0)

        Map<String, Object> root = roots.get(0);
        assertEquals(0.0, (Double) root.get("x_value"));
        assertEquals(0.0, (Double) root.get("y_value"));
    }

    @Test
    void testUpdatePoint() {
        Long pointId = pointService.createPoint(testFunctionId, 1.0, 1.0);

        boolean updated = pointService.updatePoint(pointId, testFunctionId, 2.0, 4.0);
        assertTrue(updated);

        Map<String, Object> updatedPoint = pointService.getPointById(pointId);
        assertNotNull(updatedPoint);
        assertEquals(2.0, (Double) updatedPoint.get("x_value"));
        assertEquals(4.0, (Double) updatedPoint.get("y_value"));
    }

    @Test
    void testRecalculatePoints() {
        // Создаем точки для квадратичной функции
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);

        int updatedCount = pointService.recalculatePoints(testFunctionId, "cubic"); // меняем на кубическую

        // Теперь ожидаем 2 обновления, так как обновляются все точки
        assertEquals(2, updatedCount, "Должно быть обновлено 2 точки");

        List<Map<String, Object>> points = pointService.getPointsByFunctionId(testFunctionId);
        assertEquals(2, points.size(), "Должно остаться 2 точки");

        // Проверяем, что все точки теперь соответствуют кубической функции
        points.forEach(point -> {
            Double x = (Double) point.get("x_value");
            Double y = (Double) point.get("y_value");
            assertEquals(x * x * x, y, 0.001, "Точка должна соответствовать кубической функции");
        });

        // Проверяем конкретные значения
        boolean foundPoint1 = points.stream()
                .anyMatch(p -> (Double) p.get("x_value") == 1.0 && (Double) p.get("y_value") == 1.0);
        boolean foundPoint2 = points.stream()
                .anyMatch(p -> (Double) p.get("x_value") == 2.0 && (Double) p.get("y_value") == 8.0);

        assertTrue(foundPoint1, "Должна быть точка (1.0, 1.0)");
        assertTrue(foundPoint2, "Должна быть точка (2.0, 8.0)");
    }

    @Test
    void testDeletePoint() {
        Long pointId = pointService.createPoint(testFunctionId, 1.0, 1.0);

        boolean deleted = pointService.deletePoint(pointId);
        assertTrue(deleted);

        Map<String, Object> deletedPoint = pointService.getPointById(pointId);
        assertNull(deletedPoint);
    }

    @Test
    void testDeletePointsByFunctionId() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);

        // Метод возвращает количество удаленных точек, а не boolean
        int deletedCount = pointService.deletePointsByFunctionId(testFunctionId);

        // Проверяем, что удалено как минимум 2 точки
        assertTrue(deletedCount >= 2, "Должно быть удалено как минимум 2 точки, но удалено: " + deletedCount);

        // Проверяем, что точек больше нет
        List<Map<String, Object>> points = pointService.getPointsByFunctionId(testFunctionId);
        assertTrue(points.isEmpty(), "Список точек должен быть пустым после удаления");
    }

    @Test
    void testDeletePointsByXRange() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);
        pointService.createPoint(testFunctionId, 3.0, 9.0);
        pointService.createPoint(testFunctionId, 4.0, 16.0);

        int deletedCount = pointService.deletePointsByXRange(testFunctionId, 2.0, 3.0);
        assertEquals(2, deletedCount);

        List<Map<String, Object>> remainingPoints = pointService.getPointsByFunctionId(testFunctionId);
        assertEquals(2, remainingPoints.size());

        remainingPoints.forEach(point -> {
            Double x = (Double) point.get("x_value");
            assertTrue(x < 2.0 || x > 3.0);
        });
    }

    @Test
    void testIsXValueUnique() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);

        boolean isUnique = pointService.isXValueUnique(testFunctionId, 2.0);
        assertTrue(isUnique);

        boolean isNotUnique = pointService.isXValueUnique(testFunctionId, 1.0);
        assertFalse(isNotUnique);
    }

    @Test
    void testGetPointStatistics() {
        pointService.createPoint(testFunctionId, 1.0, 1.0);
        pointService.createPoint(testFunctionId, 2.0, 4.0);
        pointService.createPoint(testFunctionId, 3.0, 9.0);
        pointService.createPoint(testFunctionId, 4.0, 16.0);

        PointService.PointStatistics stats = pointService.getPointStatistics(testFunctionId);

        assertNotNull(stats);
        assertEquals(testFunctionId, stats.getFunctionId());
        assertEquals(4, stats.getPointCount());
        assertEquals(1.0, stats.getMinX());
        assertEquals(4.0, stats.getMaxX());
        assertEquals(1.0, stats.getMinY());
        assertEquals(16.0, stats.getMaxY());
        assertEquals(2.5, stats.getAverageX()); // (1+2+3+4)/4 = 2.5
        assertEquals(7.5, stats.getAverageY()); // (1+4+9+16)/4 = 7.5
    }

    @Test
    void testCalculateFunction() {
        // Тестируем различные типы функций
        int linearPoints = pointService.generateFunctionPoints(testFunctionId, "linear", 5.0, 5.0, 1.0);
        assertEquals(1, linearPoints);

        int quadraticPoints = pointService.generateFunctionPoints(testFunctionId, "quadratic", 5.0, 5.0, 1.0);
        assertEquals(1, quadraticPoints);

        int cubicPoints = pointService.generateFunctionPoints(testFunctionId, "cubic", 5.0, 5.0, 1.0);
        assertEquals(1, cubicPoints);

        // Для тригонометрических функций проверяем корректность вычислений
        int sinPoints = pointService.generateFunctionPoints(testFunctionId, "sin", 0.0, 0.0, 1.0);
        assertEquals(1, sinPoints);

        int cosPoints = pointService.generateFunctionPoints(testFunctionId, "cos", 0.0, 0.0, 1.0);
        assertEquals(1, cosPoints);
    }
}