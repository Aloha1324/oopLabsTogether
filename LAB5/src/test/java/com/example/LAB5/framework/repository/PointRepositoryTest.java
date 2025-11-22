package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PointRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(PointRepositoryTest.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private PointRepository pointRepository;

    private static String testPrefix;

    @BeforeAll
    static void setUpAll() {
        testPrefix = "point_test_" + UUID.randomUUID().toString().substring(0, 8) + "_";
        logger.info("Установлен префикс тестов: {}", testPrefix);
    }

    private String uniqueName(String baseName) {
        return testPrefix + baseName + "_" + System.currentTimeMillis();
    }

    private User createTestUser() {
        String username = uniqueName("point_user");
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("test_hash");
        return userRepository.save(user);
    }

    private Function createTestFunction(User user, String name) {
        Function function = new Function();
        function.setName(name);
        function.setUser(user);
        function.setExpression("x");
        return functionRepository.save(function);
    }

    private Point createTestPoint(Function function, Double x, Double y) {
        Point point = new Point();
        point.setFunction(function);
        point.setXValue(x);
        point.setYValue(y);
        return pointRepository.save(point);
    }

    @Test
    @Order(1)
    void testSaveAndFindById() {
        logger.info("=== Тест сохранения и поиска точки по ID ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("test_func"));

        Double x = 1.5;
        Double y = 2.25;
        Point point = createTestPoint(function, x, y);

        assertNotNull(point.getId());
        assertTrue(point.getId() > 0);

        Optional<Point> foundPoint = pointRepository.findById(point.getId());
        assertTrue(foundPoint.isPresent());
        assertEquals(x, foundPoint.get().getXValue());
        assertEquals(y, foundPoint.get().getYValue());
        assertEquals(function.getId(), foundPoint.get().getFunction().getId());

        logger.info("Создана точка: ID={}, X={}, Y={}", point.getId(), x, y);
    }

    @Test
    @Order(2)
    void testFindByFunction() {
        logger.info("=== Тест поиска точек по функции ===");

        User user = createTestUser();
        Function function1 = createTestFunction(user, uniqueName("func1"));
        Function function2 = createTestFunction(user, uniqueName("func2"));

        createTestPoint(function1, 1.0, 1.0);
        createTestPoint(function1, 2.0, 4.0);
        createTestPoint(function2, 3.0, 9.0);

        List<Point> function1Points = pointRepository.findByFunction(function1);
        List<Point> function2Points = pointRepository.findByFunction(function2);

        assertEquals(2, function1Points.size());
        assertEquals(1, function2Points.size());

        function1Points.forEach(point ->
                assertEquals(function1.getId(), point.getFunction().getId())
        );

        logger.info("Найдено {} точек для function1, {} точек для function2",
                function1Points.size(), function2Points.size());
    }

    @Test
    @Order(3)
    void testFindByFunctionId() {
        logger.info("=== Тест поиска точек по ID функции ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("func_points"));

        createTestPoint(function, 0.0, 0.0);
        createTestPoint(function, 1.0, 1.0);
        createTestPoint(function, 2.0, 4.0);

        List<Point> points = pointRepository.findByFunctionId(function.getId());

        assertFalse(points.isEmpty());
        assertEquals(3, points.size());
        points.forEach(point ->
                assertEquals(function.getId(), point.getFunction().getId())
        );

        logger.info("Найдено {} точек для функции ID={}", points.size(), function.getId());
    }

    @Test
    @Order(4)
    void testFindAll() {
        logger.info("=== Тест поиска всех точек ===");

        int initialCount = pointRepository.findAll().size();

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("all_points"));
        createTestPoint(function, 1.0, 1.0);
        createTestPoint(function, 2.0, 4.0);

        List<Point> allPoints = pointRepository.findAll();
        assertTrue(allPoints.size() >= initialCount + 2);

        logger.info("Найдено {} точек (было: {})", allPoints.size(), initialCount);
    }

    @Test
    @Order(5)
    void testFindByXValueBetween() {
        logger.info("=== Тест поиска точек по диапазону X ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("range_test"));

        createTestPoint(function, 1.0, 1.0);
        createTestPoint(function, 2.5, 6.25);
        createTestPoint(function, 4.0, 16.0);
        createTestPoint(function, 5.0, 25.0);

        List<Point> pointsInRange = pointRepository.findByXValueBetween(2.0, 4.5);

        assertFalse(pointsInRange.isEmpty());
        assertEquals(2, pointsInRange.size()); // 2.5 и 4.0

        pointsInRange.forEach(point -> {
            assertTrue(point.getXValue() >= 2.0);
            assertTrue(point.getXValue() <= 4.5);
        });

        logger.info("Найдено {} точек в диапазоне X [2.0, 4.5]", pointsInRange.size());
    }

    @Test
    @Order(6)
    void testFindByYValueGreaterThan() {
        logger.info("=== Тест поиска точек с Y больше заданного значения ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("y_greater_test"));

        createTestPoint(function, 1.0, 1.0);
        createTestPoint(function, 2.0, 4.0);
        createTestPoint(function, 3.0, 9.0);
        createTestPoint(function, 4.0, 16.0);

        List<Point> pointsWithLargeY = pointRepository.findByYValueGreaterThan(5.0);

        assertFalse(pointsWithLargeY.isEmpty());
        assertEquals(2, pointsWithLargeY.size()); // 9.0 и 16.0

        pointsWithLargeY.forEach(point ->
                assertTrue(point.getYValue() > 5.0)
        );

        logger.info("Найдено {} точек с Y > 5.0", pointsWithLargeY.size());
    }

    @Test
    @Order(7)
    void testCountByFunction() {
        logger.info("=== Тест подсчета точек функции ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("count_func"));

        int initialCount = pointRepository.countByFunction(function);

        createTestPoint(function, 1.0, 1.0);
        createTestPoint(function, 2.0, 4.0);
        createTestPoint(function, 3.0, 9.0);

        int finalCount = pointRepository.countByFunction(function);
        assertEquals(initialCount + 3, finalCount);

        logger.info("Количество точек: было {}, стало {}", initialCount, finalCount);
    }

    @Test
    @Order(8)
    void testUpdatePoint() {
        logger.info("=== Тест обновления точки ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("update_func"));
        Point point = createTestPoint(function, 1.0, 1.0);

        Double newX = 2.5;
        Double newY = 6.25;
        point.setXValue(newX);
        point.setYValue(newY);

        Point updatedPoint = pointRepository.save(point);
        assertNotNull(updatedPoint);

        Optional<Point> foundPoint = pointRepository.findById(point.getId());
        assertTrue(foundPoint.isPresent());
        assertEquals(newX, foundPoint.get().getXValue());
        assertEquals(newY, foundPoint.get().getYValue());

        logger.info("Точка обновлена: X={}, Y={}", newX, newY);
    }

    @Test
    @Order(9)
    void testDeletePoint() {
        logger.info("=== Тест удаления точки ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("delete_func"));
        Point point = createTestPoint(function, 1.0, 1.0);

        pointRepository.delete(point);

        Optional<Point> foundPoint = pointRepository.findById(point.getId());
        assertFalse(foundPoint.isPresent());

        logger.info("Точка удалена: ID={}", point.getId());
    }

    @Test
    @Order(10)
    void testDeleteByFunction() {
        logger.info("=== Тест удаления точек по функции ===");

        User user = createTestUser();
        Function function1 = createTestFunction(user, uniqueName("func_del_1"));
        Function function2 = createTestFunction(user, uniqueName("func_del_2"));

        createTestPoint(function1, 1.0, 1.0);
        createTestPoint(function1, 2.0, 4.0);
        createTestPoint(function2, 3.0, 9.0);

        pointRepository.deleteByFunction(function1);

        List<Point> remainingPoints1 = pointRepository.findByFunction(function1);
        List<Point> remainingPoints2 = pointRepository.findByFunction(function2);

        assertTrue(remainingPoints1.isEmpty());
        assertFalse(remainingPoints2.isEmpty());
        assertEquals(1, remainingPoints2.size());

        logger.info("Точки функции1 удалены, точки функции2 сохранены");
    }

    @Test
    @Order(11)
    void testPointNotFound() {
        logger.info("=== Тест поиска несуществующей точки ===");

        Optional<Point> nonExistentPoint = pointRepository.findById(999999L);
        assertFalse(nonExistentPoint.isPresent());

        logger.info("Несуществующая точка не найдена (ожидаемо)");
    }

    @Test
    @Order(12)
    void testFindByFunctionIdAndXValueBetween() {
        logger.info("=== Тест поиска точек по ID функции и диапазону X ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("complex_search"));

        createTestPoint(function, 1.0, 1.0);
        createTestPoint(function, 2.5, 6.25);
        createTestPoint(function, 4.0, 16.0);
        createTestPoint(function, 5.0, 25.0);

        List<Point> foundPoints = pointRepository.findByFunctionIdAndXValueBetween(
                function.getId(), 2.0, 4.5);

        assertFalse(foundPoints.isEmpty());
        assertEquals(2, foundPoints.size()); // 2.5 и 4.0

        foundPoints.forEach(point -> {
            assertEquals(function.getId(), point.getFunction().getId());
            assertTrue(point.getXValue() >= 2.0);
            assertTrue(point.getXValue() <= 4.5);
        });

        logger.info("Найдено {} точек для функции ID={} в диапазоне X [2.0, 4.5]",
                foundPoints.size(), function.getId());
    }

    @Test
    @Order(13)
    void testFindByYValueLessThan() {
        logger.info("=== Тест поиска точек с Y меньше заданного значения ===");

        User user = createTestUser();
        Function function = createTestFunction(user, uniqueName("y_less_test"));

        createTestPoint(function, 1.0, 1.0);
        createTestPoint(function, 2.0, 4.0);
        createTestPoint(function, 3.0, 9.0);

        List<Point> pointsWithSmallY = pointRepository.findByYValueLessThan(5.0);

        assertFalse(pointsWithSmallY.isEmpty());
        assertEquals(2, pointsWithSmallY.size()); // 1.0 и 4.0

        pointsWithSmallY.forEach(point ->
                assertTrue(point.getYValue() < 5.0)
        );

        logger.info("Найдено {} точек с Y < 5.0", pointsWithSmallY.size());
    }

    @AfterEach
    void tearDown() {
        try {
            cleanTestData();
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private void cleanTestData() {
        logger.info("Очистка тестовых данных точек...");

        // Очистка точек
        List<Point> allPoints = pointRepository.findAll();
        int deletedPoints = 0;

        for (Point point : allPoints) {
            // Проверяем через функцию
            Function function = point.getFunction();
            if (function != null && function.getName().startsWith(testPrefix)) {
                pointRepository.delete(point);
                deletedPoints++;
            }
        }

        // Очистка функций
        List<Function> allFunctions = functionRepository.findAll();
        int deletedFunctions = 0;

        for (Function function : allFunctions) {
            if (function.getName().startsWith(testPrefix)) {
                functionRepository.delete(function);
                deletedFunctions++;
            }
        }

        // Очистка пользователей
        List<User> allUsers = userRepository.findAll();
        int deletedUsers = 0;

        for (User user : allUsers) {
            if (user.getUsername().startsWith(testPrefix)) {
                userRepository.delete(user);
                deletedUsers++;
            }
        }

        if (deletedPoints > 0 || deletedFunctions > 0 || deletedUsers > 0) {
            logger.info("Очищено {} точек, {} функций и {} пользователей",
                    deletedPoints, deletedFunctions, deletedUsers);
        }
    }
}