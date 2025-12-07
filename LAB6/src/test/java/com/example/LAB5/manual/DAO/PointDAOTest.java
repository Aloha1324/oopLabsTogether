package com.example.LAB5.manual.DAO;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class PointDAOTest {

    private static final String TEST_PREFIX = "point_test_" + System.currentTimeMillis() + "_";

    private PointDAO pointDAO;
    private FunctionDAO functionDAO;
    private UserDAO userDAO;

    private Long testUserId;
    private Long testFunctionId;
    private Long otherFunctionId;

    @BeforeAll
    void setUpAll() {
        pointDAO = new PointDAO();
        functionDAO = new FunctionDAO();
        userDAO = new UserDAO();

        logger.info("Установлен префикс тестов: {}", TEST_PREFIX);
    }

    @BeforeEach
    void setUp() {
        try {
            // Создать тестового пользователя
            testUserId = userDAO.createUser(TEST_PREFIX + "point_test_user", "testpassword");
            logger.info("Создан тестовый пользователь с ID: {}", testUserId);

            // Создать тестовую функцию
            testFunctionId = functionDAO.createFunction(
                    TEST_PREFIX + "test_function_points",
                    testUserId,
                    "x^2 + 2*x + 1"
            );
            logger.info("Создана тестовая функция с ID: {} для тестов точек", testFunctionId);

        } catch (Exception e) {
            logger.error("Ошибка при настройке теста", e);
            fail("Не удалось настроить тестовые данные");
        }
    }

    @AfterEach
    void tearDown() {
        try {
            logger.info("Начало очистки тестовых данных...");

            // ПРАВИЛЬНЫЙ ПОРЯДОК ОЧИСТКИ:

            // 1. Сначала удалить все точки других тестовых функций
            if (otherFunctionId != null && otherFunctionId != 0) {
                int pointsDeleted = pointDAO.deletePointsByFunctionId(otherFunctionId);
                logger.info("Удалено {} точек другой функции с ID: {}", pointsDeleted, otherFunctionId);

                // Затем удалить саму функцию
                boolean funcDeleted = functionDAO.deleteFunction(otherFunctionId);
                if (funcDeleted) {
                    logger.info("Удалена другая тестовая функция с ID: {}", otherFunctionId);
                    otherFunctionId = null;
                }
            }

            // 2. Удалить все точки основной тестовой функции
            if (testFunctionId != null && testFunctionId != 0) {
                int pointsDeleted = pointDAO.deletePointsByFunctionId(testFunctionId);
                logger.info("Удалено {} точек основной функции с ID: {}", pointsDeleted, testFunctionId);

                // Затем удалить саму функцию
                boolean funcDeleted = functionDAO.deleteFunction(testFunctionId);
                if (funcDeleted) {
                    logger.info("Удалена основная тестовая функция с ID: {}", testFunctionId);
                    testFunctionId = null;
                }
            }

            // 3. В конце удалить тестового пользователя
            if (testUserId != null && testUserId != 0) {
                boolean userDeleted = userDAO.deleteUser(testUserId);
                if (userDeleted) {
                    logger.info("Удален тестовый пользователь с ID: {}", testUserId);
                    testUserId = null;
                }
            }

            logger.info("Очистка тестовых данных завершена");

        } catch (Exception e) {
            logger.error("❌ Ошибка при очистке тестовых данных", e);
        }
    }

    @AfterAll
    void tearDownAll() {
        try {
            logger.info("Начало финальной очистки всех тестовых данных...");

            // ПРАВИЛЬНЫЙ ПОРЯДОК ФИНАЛЬНОЙ ОЧИСТКИ:

            // 1. Сначала удалить ВСЕ точки, связанные с тестовыми функциями
            int totalPointsDeleted = pointDAO.deletePointsByTestPrefix(TEST_PREFIX);
            logger.info("Финальная очистка: удалено {} точек с префиксом {}", totalPointsDeleted, TEST_PREFIX);

            // 2. Затем удалить ВСЕ тестовые функции
            int totalFunctionsDeleted = functionDAO.deleteFunctionsByTestPrefix(TEST_PREFIX);
            logger.info("Финальная очистка: удалено {} функций с префиксом {}", totalFunctionsDeleted, TEST_PREFIX);

            // 3. В конце удалить ВСЕХ тестовых пользователей
            int totalUsersDeleted = userDAO.deleteUsersByTestPrefix(TEST_PREFIX);
            logger.info("Финальная очистка: удалено {} пользователей с префиксом {}", totalUsersDeleted, TEST_PREFIX);

            logger.info("✅ Финальная очистка всех тестовых данных завершена успешно");

        } catch (Exception e) {
            logger.error("❌ Ошибка при финальной очистке тестовых данных", e);
        }
    }

    @Test
    void testCreatePoint() {
        // Создание точки
        Long pointId = pointDAO.createPoint(testFunctionId, 1.0, 4.0);
        assertTrue(pointId > 0, "ID созданной точки должен быть положительным");

        // Проверка, что точка создана
        Map<String, Object> createdPoint = pointDAO.findById(pointId);
        assertNotNull(createdPoint, "Созданная точка должна существовать");
        assertEquals(1.0, (Double) createdPoint.get("x_value"), 0.001, "X значение должно совпадать");
        assertEquals(4.0, (Double) createdPoint.get("y_value"), 0.001, "Y значение должно совпадать");
        assertEquals(testFunctionId, createdPoint.get("function_id"), "ID функции должно совпадать");
    }

    @Test
    void testCreatePointsBatch() {
        // Создание нескольких точек в batch режиме
        List<Object[]> points = Arrays.asList(
                new Object[]{testFunctionId, 1.0, 4.0},
                new Object[]{testFunctionId, 2.0, 9.0},
                new Object[]{testFunctionId, 3.0, 16.0}
        );

        int results = pointDAO.createPointsBatch(points);
        assertEquals(points.size(), results, "Должны быть созданы все точки");

        // Проверка, что все точки созданы
        List<Map<String, Object>> createdPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(points.size(), createdPoints.size(), "Должны быть найдены все созданные точки");
    }

    @Test
    void testFindByFunctionId() {
        // Создаем другую функцию для проверки изоляции
        otherFunctionId = functionDAO.createFunction(
                TEST_PREFIX + "other_function_points",
                testUserId,
                "sin(x)"
        );

        // Создаем точки для обеих функций
        pointDAO.createPoint(testFunctionId, 1.0, 4.0); // для основной функции
        pointDAO.createPoint(testFunctionId, 2.0, 9.0); // для основной функции

        pointDAO.createPoint(otherFunctionId, 3.0, 0.141); // для другой функции

        // Проверяем, что findByFunctionId возвращает только точки нужной функции
        List<Map<String, Object>> mainFunctionPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(2, mainFunctionPoints.size(), "Должны быть найдены только точки основной функции");

        List<Map<String, Object>> otherFunctionPoints = pointDAO.findByFunctionId(otherFunctionId);
        assertEquals(1, otherFunctionPoints.size(), "Должна быть найдена точка другой функции");
    }

    @Test
    void testFindByFunctionIdAndXRange() {
        // Создаем точки с разными X значениями
        pointDAO.createPoint(testFunctionId, -2.0, 4.0);
        pointDAO.createPoint(testFunctionId, 0.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 9.0);
        pointDAO.createPoint(testFunctionId, 5.0, 36.0);

        // Ищем точки в диапазоне X [-2.0, 2.0]
        List<Map<String, Object>> pointsInRange = pointDAO.findByFunctionIdAndXRange(testFunctionId, -2.0, 2.0);
        assertEquals(3, pointsInRange.size(), "Должны быть найдены 3 точки в диапазоне X [-2.0, 2.0]");

        // Проверяем, что все найденные точки в нужном диапазоне
        for (Map<String, Object> point : pointsInRange) {
            Double xValue = (Double) point.get("x_value");
            assertTrue(xValue >= -2.0 && xValue <= 2.0,
                    "X значение должно быть в диапазоне [-2.0, 2.0]");
        }
    }

    @Test
    void testFindByYValueGreaterThan() {
        // Создаем точки с разными Y значениями
        pointDAO.createPoint(testFunctionId, 1.0, 2.0);
        pointDAO.createPoint(testFunctionId, 2.0, 5.0);
        pointDAO.createPoint(testFunctionId, 3.0, 10.0);

        // Ищем точки с Y > 3.0
        List<Map<String, Object>> pointsWithHighY = pointDAO.findByYValueGreaterThan(3.0);

        // ВАЖНО: этот тест может зависеть от существующих данных в БД
        // Поэтому проверяем только наши тестовые точки
        long ourPointsCount = pointsWithHighY.stream()
                .filter(p -> testFunctionId.equals(p.get("function_id")))
                .count();

        assertEquals(2, ourPointsCount, "Должны быть найдены 2 наши точки с Y > 3.0");
    }

    @Test
    void testFindByXValue() {
        // Создаем точку с конкретным X значением
        pointDAO.createPoint(testFunctionId, 3.14, 9.8596);

        // Ищем точку по точному X значению
        List<Map<String, Object>> points = pointDAO.findByXValue(3.14);

        // Проверяем, что наша точка найдена
        Optional<Map<String, Object>> ourPoint = points.stream()
                .filter(p -> testFunctionId.equals(p.get("function_id")) &&
                        3.14 == (Double) p.get("x_value"))
                .findFirst();

        assertTrue(ourPoint.isPresent(), "Должна быть найдена наша точка с X = 3.14");
        assertEquals(9.8596, (Double) ourPoint.get().get("y_value"), 0.001, "Y значение должно совпадать");
    }

    @Test
    void testUpdatePoint() {
        // Создаем точку
        Long pointId = pointDAO.createPoint(testFunctionId, 1.0, 4.0);

        // Обновляем точку
        boolean updateResult = pointDAO.updatePoint(pointId, 2.0, 8.0);
        assertTrue(updateResult, "Обновление точки должно быть успешным");

        // Проверяем обновление
        Map<String, Object> retrievedPoint = pointDAO.findById(pointId);
        assertNotNull(retrievedPoint, "Точка должна существовать после обновления");
        assertEquals(2.0, (Double) retrievedPoint.get("x_value"), 0.001, "X значение должно быть обновлено");
        assertEquals(8.0, (Double) retrievedPoint.get("y_value"), 0.001, "Y значение должно быть обновлено");
    }

    @Test
    void testUpdatePointYValue() {
        // Создаем точку
        Long pointId = pointDAO.createPoint(testFunctionId, 1.0, 4.0);

        // Обновляем только Y значение
        boolean updateResult = pointDAO.updatePointYValue(pointId, 16.0);
        assertTrue(updateResult, "Обновление Y значения должно быть успешным");

        // Проверяем обновление
        Map<String, Object> retrievedPoint = pointDAO.findById(pointId);
        assertNotNull(retrievedPoint, "Точка должна существовать после обновления");
        assertEquals(1.0, (Double) retrievedPoint.get("x_value"), 0.001, "X значение не должно измениться");
        assertEquals(16.0, (Double) retrievedPoint.get("y_value"), 0.001, "Y значение должно быть обновлено");
    }

    @Test
    void testUpdatePointsYValueByFunction() {
        // Создаем несколько точек с формулой y = x^2
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);  // y = 1^2 = 1
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);  // y = 2^2 = 4
        pointDAO.createPoint(testFunctionId, 3.0, 9.0);  // y = 3^2 = 9

        // Обновляем Y значения всех точек функции (умножаем на 2)
        int updatedCount = pointDAO.updatePointsYValueByFunction(testFunctionId, 2.0);
        assertEquals(3, updatedCount, "Должны быть обновлены 3 точки");

        // Проверяем обновления
        List<Map<String, Object>> updatedPoints = pointDAO.findByFunctionId(testFunctionId);

        // Ожидаемые значения после умножения на 2:
        // точка 1: 1.0 * 2 = 2.0
        // точка 2: 4.0 * 2 = 8.0
        // точка 3: 9.0 * 2 = 18.0

        for (Map<String, Object> point : updatedPoints) {
            Double xValue = (Double) point.get("x_value");
            Double yValue = (Double) point.get("y_value");
            Double originalY = xValue * xValue;  // исходная формула y = x^2
            Double expectedY = originalY * 2.0;  // после умножения на 2

            assertEquals(expectedY, yValue, 0.001,
                    String.format("Y значение должно быть обновлено: x=%.1f, expected=%.1f, actual=%.1f",
                            xValue, expectedY, yValue));
        }
    }

    @Test
    void testDeletePoint() {
        // Создаем точку
        Long pointId = pointDAO.createPoint(testFunctionId, 1.0, 4.0);

        // Удаляем точку
        boolean deleteResult = pointDAO.deletePoint(pointId);
        assertTrue(deleteResult, "Удаление точки должно быть успешным");

        // Проверяем, что точка удалена
        Map<String, Object> deletedPoint = pointDAO.findById(pointId);
        assertNull(deletedPoint, "Точка должна быть удалена");
    }

    @Test
    void testDeletePointsByFunctionId() {
        // Создаем другую функцию
        otherFunctionId = functionDAO.createFunction(
                TEST_PREFIX + "other_function_delete",
                testUserId,
                "cos(x)"
        );

        // Создаем точки для обеих функций
        pointDAO.createPoint(testFunctionId, 1.0, 4.0); // основная функция
        pointDAO.createPoint(testFunctionId, 2.0, 9.0); // основная функция

        pointDAO.createPoint(otherFunctionId, 3.0, -0.99); // другая функция

        // Удаляем точки только основной функции
        int deletedCount = pointDAO.deletePointsByFunctionId(testFunctionId);
        assertEquals(2, deletedCount, "Должны быть удалены 2 точки основной функции");

        // Проверяем, что точки основной функции удалены
        List<Map<String, Object>> remainingMainPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(0, remainingMainPoints.size(), "Не должно остаться точек основной функции");

        // Проверяем, что точки другой функции остались
        List<Map<String, Object>> remainingOtherPoints = pointDAO.findByFunctionId(otherFunctionId);
        assertEquals(1, remainingOtherPoints.size(), "Точки другой функции должны остаться");
    }

    @Test
    void testGetPointsOrderedByX() {
        // Создаем точки в разном порядке
        pointDAO.createPoint(testFunctionId, 3.0, 9.0);
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);
        pointDAO.createPoint(testFunctionId, 5.0, 25.0);
        pointDAO.createPoint(testFunctionId, 4.0, 16.0);

        // Получаем точки, отсортированные по X
        List<Map<String, Object>> sortedPoints = pointDAO.getPointsOrderedByX(testFunctionId);

        // Проверяем сортировку
        assertEquals(5, sortedPoints.size(), "Должны быть найдены все 5 точек");
        for (int i = 0; i < sortedPoints.size() - 1; i++) {
            Double currentX = (Double) sortedPoints.get(i).get("x_value");
            Double nextX = (Double) sortedPoints.get(i + 1).get("x_value");
            assertTrue(currentX <= nextX,
                    "Точки должны быть отсортированы по возрастанию X");
        }
    }

    @Test
    void testGetPointsWithFunctionName() {
        // Создаем точку
        pointDAO.createPoint(testFunctionId, 1.0, 4.0);

        // Получаем точки с именами функций
        List<Map<String, Object>> pointsWithNames = pointDAO.getPointsWithFunctionName();

        // Проверяем, что наша точка есть в результате
        Optional<Map<String, Object>> ourPoint = pointsWithNames.stream()
                .filter(p -> testFunctionId.equals(p.get("function_id")) &&
                        1.0 == (Double) p.get("x_value"))
                .findFirst();

        assertTrue(ourPoint.isPresent(), "Должна быть найдена наша точка с именем функции");
        assertNotNull(ourPoint.get().get("function_name"), "Должно быть имя функции");
    }

    @Test
    void testGetPointCountByFunction() {
        // Создаем другую функцию для подсчета
        otherFunctionId = functionDAO.createFunction(
                TEST_PREFIX + "count_test_function",
                testUserId,
                "x^3"
        );

        // Создаем точки для обеих функций
        pointDAO.createPoint(testFunctionId, 1.0, 4.0); // основная функция
        pointDAO.createPoint(testFunctionId, 2.0, 9.0); // основная функция

        pointDAO.createPoint(otherFunctionId, 3.0, 27.0); // другая функция

        // Получаем количество точек для каждой функции через общий метод
        List<Map<String, Object>> pointCounts = pointDAO.getPointCountByFunction();

        // Находим количество точек для наших функций
        Long mainFunctionCount = pointCounts.stream()
                .filter(pc -> testFunctionId.equals(pc.get("function_id")))
                .findFirst()
                .map(pc -> ((Number) pc.get("point_count")).longValue())
                .orElse(0L);

        Long otherFunctionCount = pointCounts.stream()
                .filter(pc -> otherFunctionId.equals(pc.get("function_id")))
                .findFirst()
                .map(pc -> ((Number) pc.get("point_count")).longValue())
                .orElse(0L);

        assertEquals(2L, mainFunctionCount, "Основная функция должна иметь 2 точки");
        assertEquals(1L, otherFunctionCount, "Другая функция должна иметь 1 точку");
    }

    @Test
    void testFindByFunctionIds() {
        // Создаем вторую функцию
        otherFunctionId = functionDAO.createFunction(
                TEST_PREFIX + "second_function",
                testUserId,
                "log(x)"
        );

        // Создаем точки для обеих функций
        pointDAO.createPoint(testFunctionId, 1.0, 4.0); // функция 1
        pointDAO.createPoint(testFunctionId, 2.0, 9.0); // функция 1

        pointDAO.createPoint(otherFunctionId, 3.0, 1.098); // функция 2
        pointDAO.createPoint(otherFunctionId, 4.0, 1.386); // функция 2

        // Ищем точки для обеих функций
        List<Long> functionIds = Arrays.asList(testFunctionId, otherFunctionId);
        List<Map<String, Object>> points = pointDAO.findByFunctionIds(functionIds);

        assertEquals(4, points.size(), "Должны быть найдены все 4 точки обеих функций");

        // Проверяем распределение по функциям
        long mainFuncPoints = points.stream()
                .filter(p -> testFunctionId.equals(p.get("function_id")))
                .count();
        long otherFuncPoints = points.stream()
                .filter(p -> otherFunctionId.equals(p.get("function_id")))
                .count();

        assertEquals(2, mainFuncPoints, "Должны быть найдены 2 точки основной функции");
        assertEquals(2, otherFuncPoints, "Должны быть найдены 2 точки другой функции");
    }

    @Test
    void testDeletePointsByFunctionIdAndXRange() {
        // Создаем точки с разными X значениями
        pointDAO.createPoint(testFunctionId, -2.0, 4.0);
        pointDAO.createPoint(testFunctionId, -1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 0.0, 1.0);
        pointDAO.createPoint(testFunctionId, 1.0, 4.0);
        pointDAO.createPoint(testFunctionId, 2.0, 9.0);

        // Удаляем точки с X < 0.0
        int deletedCount = pointDAO.deletePointsByFunctionIdAndXRange(testFunctionId, 0.0);
        assertEquals(2, deletedCount, "Должны быть удалены 2 точки с X < 0.0");

        // Проверяем оставшиеся точки
        List<Map<String, Object>> remainingPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(3, remainingPoints.size(), "Должны остаться 3 точки с X >= 0.0");

        // Проверяем, что все оставшиеся точки имеют X >= 0.0
        for (Map<String, Object> point : remainingPoints) {
            Double xValue = (Double) point.get("x_value");
            assertTrue(xValue >= 0.0, "Все оставшиеся точки должны иметь X >= 0.0");
        }
    }

    @Test
    void testCreatePointsBatchEmptyList() {
        // Тестируем batch создание с пустым списком
        List<Object[]> emptyList = Collections.emptyList();
        int results = pointDAO.createPointsBatch(emptyList);

        assertEquals(0, results, "Должно быть создано 0 точек");
    }

    @Test
    void testFindByFunctionIdsEmptyList() {
        // Тестируем поиск по пустому списку ID функций
        List<Long> emptyFunctionIds = Collections.emptyList();
        List<Map<String, Object>> points = pointDAO.findByFunctionIds(emptyFunctionIds);

        assertNotNull(points, "Результат не должен быть null");
        assertTrue(points.isEmpty(), "Результат должен быть пустым списком");
    }

    @Test
    void testGetFunctionStatistics() {
        // Создаем точки для статистики
        pointDAO.createPoint(testFunctionId, 1.0, 2.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);
        pointDAO.createPoint(testFunctionId, 3.0, 6.0);
        pointDAO.createPoint(testFunctionId, 4.0, 8.0);
        pointDAO.createPoint(testFunctionId, 5.0, 10.0);

        // Получаем статистику
        Map<String, Object> stats = pointDAO.getFunctionStatistics(testFunctionId);

        assertNotNull(stats, "Статистика не должна быть null");
        assertEquals(1.0, (Double) stats.get("min_x"), 0.001, "Минимальный X должен быть 1.0");
        assertEquals(5.0, (Double) stats.get("max_x"), 0.001, "Максимальный X должен быть 5.0");
        assertEquals(6.0, (Double) stats.get("avg_y"), 0.001, "Средний Y должен быть 6.0");
    }

    @Test
    void testDeletePointsByUsernamePrefix() {
        // Создаем точки
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        // Удаляем точки по префиксу пользователя
        int deletedCount = pointDAO.deletePointsByUsernamePrefix(TEST_PREFIX);

        // Проверяем, что точки удалены
        List<Map<String, Object>> remainingPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(0, remainingPoints.size(), "Все точки должны быть удалены");
        assertTrue(deletedCount >= 2, "Должно быть удалено как минимум 2 точки");
    }

    @Test
    void testSafeDeletePointsByUsernamePrefix() {
        // Создаем точки
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        // Безопасно удаляем точки по префиксу
        int deletedCount = pointDAO.safeDeletePointsByUsernamePrefix(TEST_PREFIX);

        // Проверяем, что точки удалены
        List<Map<String, Object>> remainingPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(0, remainingPoints.size(), "Все точки должны быть удалены");
        assertTrue(deletedCount >= 2, "Должно быть удалено как минимум 2 точки");
    }

    @Test
    void testGetPointsCountByUsernamePrefix() {
        // Создаем точки
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        // Получаем количество точек по префиксу
        int pointsCount = pointDAO.getPointsCountByUsernamePrefix(TEST_PREFIX);

        assertTrue(pointsCount >= 2, "Должно быть найдено как минимум 2 точки");
    }

    @Test
    void testGetPointsByUsernamePrefix() {
        // Создаем точки
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        // Получаем точки по префиксу
        List<Map<String, Object>> points = pointDAO.getPointsByUsernamePrefix(TEST_PREFIX);

        assertTrue(points.size() >= 2, "Должно быть найдено как минимум 2 точки");

        // Проверяем, что у точек есть дополнительная информация
        for (Map<String, Object> point : points) {
            assertNotNull(point.get("function_name"), "Должно быть имя функции");
            assertNotNull(point.get("username"), "Должно быть имя пользователя");
        }
    }

    @Test
    void testHasPointsWithUsernamePrefix() {
        // Создаем точки
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);

        // Проверяем существование точек по префиксу
        boolean hasPoints = pointDAO.hasPointsWithUsernamePrefix(TEST_PREFIX);

        assertTrue(hasPoints, "Должны существовать точки с данным префиксом");
    }

    @Test
    void testDeletePointsByUserId() {
        // Создаем точки
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        // Удаляем точки по ID пользователя
        int deletedCount = pointDAO.deletePointsByUserId(testUserId);

        // Проверяем, что точки удалены
        List<Map<String, Object>> remainingPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(0, remainingPoints.size(), "Все точки должны быть удалены");
        assertTrue(deletedCount >= 2, "Должно быть удалено как минимум 2 точки");
    }

    @Test
    void testFindAllPoints() {
        // Создаем несколько точек
        pointDAO.createPoint(testFunctionId, 1.0, 1.0);
        pointDAO.createPoint(testFunctionId, 2.0, 4.0);

        // Получаем все точки
        List<Map<String, Object>> allPoints = pointDAO.findAll();

        assertNotNull(allPoints, "Список точек не должен быть null");
        // Не проверяем точное количество, так как в БД могут быть другие точки
        assertTrue(allPoints.size() >= 2, "Должно быть найдено как минимум 2 точки");
    }

    // Вспомогательный метод для логирования
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PointDAOTest.class);
}