package com.example.LAB5.framework.repository;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.entity.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
    private Long testUserId;
    private Long testFunctionId;
    private List<Long> pointIds = new ArrayList<>();

    @BeforeAll
    static void setUpAll() {
        testPrefix = "point_test_" + System.currentTimeMillis() + "_";
        logger.info("Установлен префикс тестов: {}", testPrefix);
    }

    @BeforeEach
    void setUp() {
        logger.info("Настройка тестовых данных...");

        cleanTestData();

        User testUser = new User();
        testUser.setUsername(testPrefix + "user");
        testUser.setPasswordHash("test_password");
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();

        Function testFunction = new Function();
        testFunction.setName(testPrefix + "function");
        testFunction.setUser(testUser);
        testFunction.setExpression("x^2");
        testFunction = functionRepository.save(testFunction);
        testFunctionId = testFunction.getId();

        logger.info("Созданы тестовые данные: user={}, function={}", testUserId, testFunctionId);
    }

    @Test
    @Order(3)
    void testFindByXValueBetween() {
        logger.info("=== Тест findByXValueBetween ===");

        List<Point> points = List.of(
                createPointEntity(-2.0, 4.0),
                createPointEntity(0.0, 0.0),
                createPointEntity(2.0, 4.0),
                createPointEntity(5.0, 25.0)
        );
        List<Point> savedPoints = pointRepository.saveAll(points);
        pointIds.addAll(savedPoints.stream().map(Point::getId).collect(Collectors.toList()));

        logger.info("Создано {} тестовых точек", savedPoints.size());

        long startTime = System.currentTimeMillis();
        List<Point> foundPoints = pointRepository.findByFunctionIdAndXValueBetween(
                testFunctionId, -1.0, 3.0);
        long endTime = System.currentTimeMillis();

        logger.info("Запрос findByFunctionIdAndXValueBetween выполнен за {} мс", endTime - startTime);
        logger.info("Найдено точек: {}", foundPoints.size());

        foundPoints.forEach(point -> {
            assertEquals(testFunctionId, point.getFunction().getId(),
                    "Точка должна принадлежать тестовой функции");
        });

        assertEquals(2, foundPoints.size(), "Должно быть найдено 2 точки в диапазоне [-1.0, 3.0]");

        foundPoints.forEach(point -> {
            double x = point.getXValue();
            assertTrue(x >= -1.0 && x <= 3.0,
                    String.format("X значение %.2f должно быть в диапазоне [-1.0, 3.0]", x));
        });
    }

    @Test
    @Order(4)
    void testFindByYValueGreaterThan() {
        logger.info("=== Тест findByYValueGreaterThan ===");

        List<Point> points = List.of(
                createPointEntity(1.0, 1.0),
                createPointEntity(2.0, 4.0),
                createPointEntity(3.0, 9.0)
        );
        List<Point> savedPoints = pointRepository.saveAll(points);
        pointIds.addAll(savedPoints.stream().map(Point::getId).collect(Collectors.toList()));

        logger.info("Создано {} тестовых точек", savedPoints.size());

        long startTime = System.currentTimeMillis();
        List<Point> foundPoints = pointRepository.findByFunctionIdAndYValueGreaterThan(
                testFunctionId, 2.0);
        long endTime = System.currentTimeMillis();

        logger.info("Запрос findByFunctionIdAndYValueGreaterThan выполнен за {} мс", endTime - startTime);
        logger.info("Найдено точек: {}", foundPoints.size());

        foundPoints.forEach(point -> {
            assertEquals(testFunctionId, point.getFunction().getId(),
                    "Точка должна принадлежать тестовой функции");
        });

        assertEquals(2, foundPoints.size(), "Должно быть найдено 2 точки с Y > 2.0");

        foundPoints.forEach(point -> {
            double y = point.getYValue();
            assertTrue(y > 2.0,
                    String.format("Y значение %.2f должно быть больше 2.0", y));
        });
    }

    @Test
    @Order(7)
    void testFindByYValueLessThan() {
        logger.info("=== Тест findByYValueLessThan ===");

        List<Point> points = List.of(
                createPointEntity(1.0, 1.0),
                createPointEntity(2.0, 4.0),
                createPointEntity(3.0, 9.0)
        );
        List<Point> savedPoints = pointRepository.saveAll(points);
        pointIds.addAll(savedPoints.stream().map(Point::getId).collect(Collectors.toList()));

        logger.info("Создано {} тестовых точек", savedPoints.size());

        long startTime = System.currentTimeMillis();
        List<Point> foundPoints = pointRepository.findByFunctionIdAndYValueLessThan(
                testFunctionId, 5.0);
        long endTime = System.currentTimeMillis();

        logger.info("Запрос findByFunctionIdAndYValueLessThan выполнен за {} мс", endTime - startTime);
        logger.info("Найдено точек: {}", foundPoints.size());

        foundPoints.forEach(point -> {
            assertEquals(testFunctionId, point.getFunction().getId(),
                    "Точка должна принадлежать тестовой функции");
        });

        assertEquals(2, foundPoints.size(), "Должно быть найдено 2 точки с Y < 5.0");

        foundPoints.forEach(point -> {
            double y = point.getYValue();
            assertTrue(y < 5.0,
                    String.format("Y значение %.2f должно быть меньше 5.0", y));
        });
    }

    @Test
    void testIsolation() {
        logger.info("=== Тест изоляции ===");

        List<Point> points = List.of(
                createPointEntity(1.0, 1.0),
                createPointEntity(2.0, 4.0)
        );
        pointRepository.saveAll(points);

        List<Point> allPoints = pointRepository.findByXValueBetween(0.0, 10.0);
        logger.info("Всего точек в базе в диапазоне [0, 10]: {}", allPoints.size());

        List<Point> ourPoints = pointRepository.findByFunctionIdAndXValueBetween(testFunctionId, 0.0, 10.0);
        logger.info("Наших точек в диапазоне [0, 10]: {}", ourPoints.size());

        assertEquals(2, ourPoints.size(), "Должны найтись только наши тестовые точки");
    }

    private Point createPointEntity(Double x, Double y) {
        Function function = functionRepository.findById(testFunctionId).orElseThrow();
        User user = userRepository.findById(testUserId).orElseThrow();

        Point point = new Point();
        point.setFunction(function);
        point.setUser(user);
        point.setXValue(x);
        point.setYValue(y);
        return point;
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        logger.debug("Очистка тестовых данных...");

        try {
            if (testFunctionId != null) {
                pointRepository.deleteByFunctionId(testFunctionId);
            }

            pointIds.clear();

            if (testFunctionId != null) {
                functionRepository.deleteById(testFunctionId);
                testFunctionId = null;
            }
            if (testUserId != null) {
                userRepository.deleteById(testUserId);
                testUserId = null;
            }

        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());

            try {
                pointRepository.deleteAll();
                functionRepository.deleteAll();
                userRepository.deleteAll();
            } catch (Exception ex) {
                logger.error("Критическая ошибка при очистке: {}", ex.getMessage());
            }
        }
    }
}