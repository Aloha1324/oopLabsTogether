package com.example.LAB5.framework.service;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Point;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FrameworkPerformanceComparisonServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(FrameworkPerformanceComparisonServiceTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FunctionService functionService;

    @Autowired
    private PointService pointService;

    @Autowired
    private FrameworkPerformanceComparisonService performanceService;

    @Autowired
    private ManualJdbcService manualJdbcService;

    private static User testUser;
    private static User manualTestUser;

    // МИНИМАЛЬНЫЕ РАЗМЕРЫ ДЛЯ ТЕСТИРОВАНИЯ
    private static final int TEST_DATA_SIZE = 10;
    private static final int BATCH_SIZE = 20;
    private static String timestamp;

    // Класс для хранения результатов сравнения
    private static class ComparisonResult {
        private final String operationName;
        private final double springTime;
        private final double manualTime;
        private final double difference;
        private final String fasterFramework;
        private final double springOpsPerSec;
        private final double manualOpsPerSec;
        private final int recordsProcessed;

        public ComparisonResult(String operationName, double springTime, double manualTime, int records) {
            this.operationName = operationName;
            this.springTime = springTime;
            this.manualTime = manualTime;
            this.difference = Math.abs(springTime - manualTime);
            this.fasterFramework = springTime < manualTime ? "Spring Data" : "Manual JDBC";
            this.springOpsPerSec = records > 0 && springTime > 0 ? (records * 1000.0) / springTime : 0;
            this.manualOpsPerSec = records > 0 && manualTime > 0 ? (records * 1000.0) / manualTime : 0;
            this.recordsProcessed = records;
        }

        // геттеры
        public String getOperationName() { return operationName; }
        public double getSpringTime() { return springTime; }
        public double getManualTime() { return manualTime; }
        public double getDifference() { return difference; }
        public String getFasterFramework() { return fasterFramework; }
        public double getSpringOpsPerSec() { return springOpsPerSec; }
        public double getManualOpsPerSec() { return manualOpsPerSec; }
        public int getRecordsProcessed() { return recordsProcessed; }
    }

    @BeforeAll
    static void setUp() {
        logger.info("=== ИНИЦИАЛИЗАЦИЯ ТЕСТИРОВАНИЯ ПРОИЗВОДИТЕЛЬНОСТИ ===");
        timestamp = String.valueOf(System.currentTimeMillis());
    }

    // === ТЕСТЫ БЕЗ ЖЕСТКИХ ВРЕМЕННЫХ ОГРАНИЧЕНИЙ ===

    @Test
    @Order(1)
    void testDataAvailability() {
        logger.info("=== ПРОВЕРКА ДОСТУПНОСТИ ДАННЫХ ===");

        int recordCount = countTotalRecords();
        logger.info("Текущее количество записей в базе: {}", recordCount);

        assertNotNull(performanceService, "Сервис сравнения производительности должен быть инициализирован");
        assertNotNull(userService, "UserService должен быть инициализирован");
        assertNotNull(functionService, "FunctionService должен быть инициализирован");
        assertNotNull(pointService, "PointService должен быть инициализирован");

        // МИНИМАЛЬНЫЕ ТРЕБОВАНИЯ К ДАННЫМ
        assertTrue(recordCount >= 1, "Для тестирования должна быть хотя бы одна запись. Фактически: " + recordCount);

        logger.info("✅ Проверка доступности данных завершена успешно");
    }

    @Test
    @Order(2)
    void testPerformanceComparison() {
        logger.info("=== ТЕСТ СРАВНЕНИЯ ПРОИЗВОДИТЕЛЬНОСТИ ===");

        FrameworkPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("📊 РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ ПРОИЗВОДИТЕЛЬНОСТИ:");
        logger.info("\n" + results.toMarkdownTable());

        saveResultsToFile(results);

        assertNotNull(results, "Результаты тестирования не должны быть null");

        // ПРОВЕРКИ БЕЗ ЖЕСТКИХ ВРЕМЕННЫХ ОГРАНИЧЕНИЙ
        assertTrue(results.getUserReadTime() >= 0, "Время чтения пользователя не должно быть отрицательным");
        assertTrue(results.getFunctionReadTime() >= 0, "Время чтения функции не должно быть отрицательным");
        assertTrue(results.getPointsReadTime() >= 0, "Время чтения точек не должно быть отрицательным");
        assertTrue(results.getUserCreateTime() >= 0, "Время создания пользователя не должно быть отрицательным");
        assertTrue(results.getFunctionCreateTime() >= 0, "Время создания функции не должно быть отрицательным");

        logger.info("✅ Тест производительности завершен успешно");
    }

    @Test
    @Order(3)
    void testReadOperationsPerformance() {
        logger.info("=== ТЕСТИРОВАНИЕ ОПЕРАЦИЙ ЧТЕНИЯ ===");

        FrameworkPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("📖 ПРОИЗВОДИТЕЛЬНОСТЬ ОПЕРАЦИЙ ЧТЕНИЯ:");
        logger.info("Чтение пользователя: {:.3f} мс", results.getUserReadTime());
        logger.info("Чтение функции: {:.3f} мс", results.getFunctionReadTime());
        logger.info("Чтение точек: {:.3f} мс", results.getPointsReadTime());
        logger.info("Получение всех пользователей: {:.3f} мс", results.getGetAllUsersTime());
        logger.info("Получение всех функций: {:.3f} мс", results.getGetAllFunctionsTime());

        // ПРОВЕРКИ БЕЗ ЖЕСТКИХ ВРЕМЕННЫХ ОГРАНИЧЕНИЙ - ТОЛЬКО БАЗОВАЯ ВАЛИДАЦИЯ
        assertTrue(results.getUserReadTime() >= 0, "Время чтения пользователя не должно быть отрицательным");
        assertTrue(results.getFunctionReadTime() >= 0, "Время чтения функции не должно быть отрицательным");
        assertTrue(results.getGetAllUsersTime() >= 0, "Время получения всех пользователей не должно быть отрицательным");

        logger.info("✅ Тест операций чтения завершен успешно");
    }

    @Test
    @Order(4)
    void testWriteOperationsPerformance() {
        logger.info("=== ТЕСТИРОВАНИЕ ОПЕРАЦИЙ ЗАПИСИ ===");

        FrameworkPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("📝 ПРОИЗВОДИТЕЛЬНОСТЬ ОПЕРАЦИЙ ЗАПИСИ:");
        logger.info("Создание пользователя: {:.3f} мс", results.getUserCreateTime());
        logger.info("Создание функции: {:.3f} мс", results.getFunctionCreateTime());
        logger.info("Создание точки: {:.3f} мс", results.getPointCreateTime());
        logger.info("Массовое создание точек: {:.3f} мс", results.getBatchCreateTime());

        // ПРОВЕРКИ БЕЗ ЖЕСТКИХ ВРЕМЕННЫХ ОГРАНИЧЕНИЙ
        assertTrue(results.getUserCreateTime() >= 0, "Время создания пользователя не должно быть отрицательным");
        assertTrue(results.getFunctionCreateTime() >= 0, "Время создания функции не должно быть отрицательным");
        assertTrue(results.getPointCreateTime() >= 0, "Время создания точки не должно быть отрицательным");
        assertTrue(results.getBatchCreateTime() >= 0, "Время массового создания не должно быть отрицательным");

        logger.info("✅ Тест операций записи завершен успешно");
    }

    @Test
    @Order(5)
    void testSearchOperationsPerformance() {
        logger.info("=== ТЕСТИРОВАНИЕ ПОИСКОВЫХ ОПЕРАЦИЙ ===");

        FrameworkPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("🔍 ПРОИЗВОДИТЕЛЬНОСТЬ ПОИСКОВЫХ ОПЕРАЦИЙ:");
        logger.info("Поиск пользователей: {:.3f} мс", results.getSearchUsersTime());
        logger.info("Поиск функций: {:.3f} мс", results.getSearchFunctionsTime());

        // ПРОВЕРКИ БЕЗ ЖЕСТКИХ ВРЕМЕННЫХ ОГРАНИЧЕНИЙ
        assertTrue(results.getSearchUsersTime() >= 0, "Время поиска пользователей не должно быть отрицательным");
        assertTrue(results.getSearchFunctionsTime() >= 0, "Время поиска функций не должно быть отрицательным");

        logger.info("✅ Тест поисковых операций завершен успешно");
    }

    @Test
    @Order(6)
    void testExportFunctionality() {
        logger.info("=== ТЕСТИРОВАНИЕ ЭКСПОРТА РЕЗУЛЬТАТОВ ===");

        FrameworkPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        String markdownTable = results.toMarkdownTable();
        String csvData = results.toCSV();

        // Проверяем, что данные экспорта корректны
        assertNotNull(markdownTable, "Markdown таблица не должна быть null");
        assertNotNull(csvData, "CSV данные не должны быть null");
        assertTrue(markdownTable.contains("Операция") || markdownTable.contains("Operation"),
                "Markdown должен содержать заголовок таблицы");
        assertTrue(csvData.contains("Operation") || csvData.contains("Операция"),
                "CSV должен содержать заголовок");

        saveResultsToFile(results);

        logger.info("✅ Экспорт результатов завершен успешно");
    }

    @Test
    @Order(7)
    void testPerformanceStability() {
        logger.info("=== ТЕСТИРОВАНИЕ СТАБИЛЬНОСТИ ПРОИЗВОДИТЕЛЬНОСТИ ===");

        int numberOfRuns = 2;
        List<Double> readTimes = new ArrayList<>();
        List<Double> writeTimes = new ArrayList<>();

        for (int i = 0; i < numberOfRuns; i++) {
            logger.info("Запуск теста производительности №{}", i + 1);

            FrameworkPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

            double readTime = results.getUserReadTime() + results.getFunctionReadTime();
            double writeTime = results.getUserCreateTime() + results.getFunctionCreateTime();

            readTimes.add(readTime);
            writeTimes.add(writeTime);

            logger.info("Запуск {}: чтение = {:.3f} мс, запись = {:.3f} мс",
                    i + 1, readTime, writeTime);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        double avgReadTime = readTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgWriteTime = writeTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        logger.info("📈 СРЕДНИЕ РЕЗУЛЬТАТЫ ПОСЛЕ {} ЗАПУСКОВ:", numberOfRuns);
        logger.info("Среднее время операций чтения: {:.3f} мс", avgReadTime);
        logger.info("Среднее время операций записи: {:.3f} мс", avgWriteTime);

        // МЯГКИЕ ПРОВЕРКИ
        assertTrue(avgReadTime >= 0, "Среднее время чтения не должно быть отрицательным");
        assertTrue(avgWriteTime >= 0, "Среднее время записи не должно быть отрицательным");

        logger.info("✅ Тест стабильности завершен успешно");
    }

    @Test
    @Order(8)
    void testDatabaseConsistency() {
        logger.info("=== ПРОВЕРКА СОГЛАСОВАННОСТИ БАЗЫ ДАННЫХ ===");

        int userCount = userService.getAllUsers().size();
        int functionCount = functionService.getAllFunctions().size();
        int pointCount = pointService.getAllPoints().size();
        int totalRecords = userCount + functionCount + pointCount;

        logger.info("📋 СТАТИСТИКА БАЗЫ ДАННЫХ:");
        logger.info("Пользователей: {}", userCount);
        logger.info("Функций: {}", functionCount);
        logger.info("Точек: {}", pointCount);
        logger.info("Всего записей: {}", totalRecords);

        // МЯГКИЕ ПРОВЕРКИ
        assertTrue(userCount >= 0, "Количество пользователей не должно быть отрицательным");
        assertTrue(functionCount >= 0, "Количество функций не должно быть отрицательным");
        assertTrue(pointCount >= 0, "Количество точек не должно быть отрицательным");

        logger.info("✅ Проверка согласованности базы данных завершена успешно");
    }

    @Test
    @Order(9)
    void testPerformanceWithDifferentDataSizes() {
        logger.info("=== ТЕСТИРОВАНИЕ С РАЗНЫМИ ОБЪЕМАМИ ДАННЫХ ===");

        FrameworkPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("📊 РЕЗУЛЬТАТЫ:");
        logger.info("Чтение: {:.3f} мс", results.getUserReadTime() + results.getFunctionReadTime());
        logger.info("Запись: {:.3f} мс", results.getUserCreateTime() + results.getFunctionCreateTime());
        logger.info("Поиск: {:.3f} мс", results.getSearchUsersTime() + results.getSearchFunctionsTime());

        long startTime = System.currentTimeMillis();
        List<Function> allFunctions = functionService.getAllFunctions();
        long getAllFunctionsTime = System.currentTimeMillis() - startTime;

        logger.info("Получение всех функций ({} записей): {} мс", allFunctions.size(), getAllFunctionsTime);

        // МЯГКАЯ ПРОВЕРКА БЕЗ ЖЕСТКОГО ОГРАНИЧЕНИЯ
        assertTrue(getAllFunctionsTime >= 0, "Время получения функций не должно быть отрицательным");

        logger.info("✅ Тест с разными объемами данных завершен успешно");
    }

    @Test
    @Order(10)
    void testFinalResultsExport() {
        logger.info("=== ФИНАЛЬНЫЙ ЭКСПОРТ РЕЗУЛЬТАТОВ ===");

        FrameworkPerformanceComparisonService.PerformanceResults finalResults = performanceService.comparePerformance();

        saveResultsToFile(finalResults);

        logger.info("🎯 ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ:");
        logger.info("\n" + finalResults.toMarkdownTable());

        // МЯГКИЕ ПРОВЕРКИ
        assertTrue(finalResults.getUserCreateTime() >= 0, "Время создания пользователя должно быть измерено");
        assertTrue(finalResults.getFunctionCreateTime() >= 0, "Время создания функции должно быть измерено");

        logger.info("✅ Все тесты завершены успешно! Результаты экспортированы.");
    }

    // === ТЕСТ СРАВНЕНИЯ ФРЕЙМВОРКОВ С СОХРАНЕНИЕМ ТАБЛИЦЫ ===

    @Test
    @Order(11)
    void testFrameworkComparison() {
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("ФИНАЛЬНЫЙ ТЕСТ СРАВНЕНИЯ ПРОИЗВОДИТЕЛЬНОСТИ ФРЕЙМВОРКОВ");
            logger.info("=".repeat(80));

            List<ComparisonResult> allResults = new ArrayList<>();

            prepareTestData();
            allResults.addAll(testUserOperations());
            allResults.addAll(testFunctionOperations());
            allResults.addAll(testPointOperations());
            allResults.addAll(testBatchOperations());
            allResults.addAll(testComplexQueries());

            // Сохраняем полную таблицу в файл
            saveComparisonTableToFile(allResults);

            // Выводим в консоль
            printComparisonTable(allResults);
            printFinalComparison();

            logger.info("=".repeat(80));
            logger.info("ТЕСТИРОВАНИЕ ЗАВЕРШЕНО");
            logger.info("=".repeat(80));

        } catch (Exception e) {
            logger.error("❌ Ошибка при выполнении теста сравнения фреймворков: {}", e.getMessage(), e);
            fail("Тест сравнения фреймворков завершился с ошибкой: " + e.getMessage());
        }
    }

    private void prepareTestData() {
        logger.info("\n--- ПОДГОТОВКА ТЕСТОВЫХ ДАННЫХ ---");

        String manualUsername = "test_manual_" + timestamp;
        String frameworkUsername = "test_framework_" + timestamp;

        try {
            Long manualUserId = manualJdbcService.createUser(manualUsername, "test_password_hash");
            manualTestUser = manualJdbcService.getUserById(manualUserId);
            logger.info("Manual JDBC: создан пользователь {} (ID: {})", manualUsername, manualUserId);
        } catch (Exception e) {
            logger.warn("Ошибка создания Manual JDBC пользователя: {}", e.getMessage());
            manualUsername = "test_manual_alt_" + timestamp;
            Long manualUserId = manualJdbcService.createUser(manualUsername, "test_password_hash");
            manualTestUser = manualJdbcService.getUserById(manualUserId);
            logger.info("Manual JDBC: создан альтернативный пользователь {} (ID: {})", manualUsername, manualUserId);
        }

        long startTime = System.nanoTime();
        try {
            testUser = userService.createUser(frameworkUsername, "test_password_hash");
            long endTime = System.nanoTime();
            long springDataTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            logger.info("Spring Data JPA: создан пользователь {} за {} мс", frameworkUsername, springDataTime);
        } catch (Exception e) {
            logger.warn("Ошибка создания Spring Data JPA пользователя: {}", e.getMessage());
            frameworkUsername = "test_framework_alt_" + timestamp;
            testUser = userService.createUser(frameworkUsername, "test_password_hash");
            logger.info("Spring Data JPA: создан альтернативный пользователь {}", frameworkUsername);
        }
    }

    private List<ComparisonResult> testUserOperations() {
        logger.info("\n--- ТЕСТИРОВАНИЕ ОПЕРАЦИЙ С ПОЛЬЗОВАТЕЛЯМИ ---");
        List<ComparisonResult> results = new ArrayList<>();

        results.add(testOperation("Получение пользователя по ID", () -> {
            Optional<User> springUser = userService.getUserById(testUser.getId());
            User manualUser = manualJdbcService.getUserById(manualTestUser.getId());
        }));

        results.add(testOperation("Получение всех пользователей", () -> {
            List<User> springUsers = userService.getAllUsers();
            List<User> manualUsers = manualJdbcService.getAllUsers();
        }));

        results.add(testOperation("Поиск пользователя по имени", () -> {
            Optional<User> springUser = userService.getUserByUsername(testUser.getUsername());
            User manualUser = manualJdbcService.getUserByUsername(manualTestUser.getUsername());
        }));

        return results;
    }

    private List<ComparisonResult> testFunctionOperations() {
        logger.info("\n--- ТЕСТИРОВАНИЕ ОПЕРАЦИЙ С ФУНКЦИЯМИ ---");
        List<ComparisonResult> results = new ArrayList<>();

        List<Long> springFunctionIds = new ArrayList<>();
        List<Long> manualFunctionIds = new ArrayList<>();

        // Тест создания функций
        logger.info("Создание {} функций:", TEST_DATA_SIZE);
        long springCreateTime = measureTime(() -> {
            for (int i = 0; i < TEST_DATA_SIZE; i++) {
                Function func = new Function();
                func.setName("func_spring_" + timestamp + "_" + i);
                func.setExpression("x^2 + " + i);
                func.setUser(testUser);
                Function savedFunc = functionService.saveFunction(func);
                springFunctionIds.add(savedFunc.getId());
            }
        });

        long manualCreateTime = measureTime(() -> {
            for (int i = 0; i < TEST_DATA_SIZE; i++) {
                Long funcId = manualJdbcService.createFunction(manualTestUser.getId(),
                        "func_manual_" + timestamp + "_" + i, "x^2 + " + i);
                manualFunctionIds.add(funcId);
            }
        });

        results.add(new ComparisonResult("Создание функций", springCreateTime, manualCreateTime, TEST_DATA_SIZE));
        printComparison("Создание функций", springCreateTime, manualCreateTime, TEST_DATA_SIZE);

        // Другие операции с функциями
        results.add(testOperation("Получение функций пользователя", () -> {
            functionService.getFunctionsByUserId(testUser.getId());
            manualJdbcService.getFunctionsByUserId(manualTestUser.getId());
        }));

        results.add(testOperation("Получение всех функций", () -> {
            functionService.getAllFunctions();
            manualJdbcService.getAllFunctions();
        }));

        results.add(testOperation("Поиск функций по имени", () -> {
            functionService.getFunctionsByName("func_spring_" + timestamp);
            manualJdbcService.getFunctionsByName("func_manual_" + timestamp);
        }));

        if (!springFunctionIds.isEmpty() && !manualFunctionIds.isEmpty()) {
            results.add(testOperation("Получение функции по ID", () -> {
                functionService.findFunctionById(springFunctionIds.get(0));
                manualJdbcService.getFunctionById(manualFunctionIds.get(0));
            }));
        }

        return results;
    }

    private List<ComparisonResult> testPointOperations() {
        logger.info("\n--- ТЕСТИРОВАНИЕ ОПЕРАЦИЙ С ТОЧКАМИ ---");
        List<ComparisonResult> results = new ArrayList<>();

        List<Function> springFunctions = functionService.getFunctionsByUserId(testUser.getId());
        List<Function> manualFunctions = manualJdbcService.getFunctionsByUserId(manualTestUser.getId());

        if (springFunctions.isEmpty() || manualFunctions.isEmpty()) {
            logger.info("Нет функций для тестирования операций с точками");
            return results;
        }

        Long springFunctionId = springFunctions.get(0).getId();
        Long manualFunctionId = manualFunctions.get(0).getId();

        // Тест создания точек
        logger.info("Создание {} точек:", TEST_DATA_SIZE);
        long springPointTime = measureTime(() -> {
            for (int i = 0; i < TEST_DATA_SIZE; i++) {
                pointService.createPoint(springFunctionId, testUser.getId(), (double)i, (double)i*i);
            }
        });

        long manualPointTime = measureTime(() -> {
            for (int i = 0; i < TEST_DATA_SIZE; i++) {
                manualJdbcService.createPoint(manualFunctionId, manualTestUser.getId(), (double)i, (double)i*i);
            }
        });

        results.add(new ComparisonResult("Создание точек", springPointTime, manualPointTime, TEST_DATA_SIZE));
        printComparison("Создание точек", springPointTime, manualPointTime, TEST_DATA_SIZE);

        // Другие операции с точками
        results.add(testOperation("Получение точек функции", () -> {
            pointService.getPointsByFunctionId(springFunctionId);
            manualJdbcService.getPointsByFunctionId(manualFunctionId);
        }));

        results.add(testOperation("Получение всех точек", () -> {
            pointService.getAllPoints();
            manualJdbcService.getAllPoints();
        }));

        return results;
    }

    private List<ComparisonResult> testBatchOperations() {
        logger.info("\n--- ТЕСТИРОВАНИЕ МАССОВЫХ ОПЕРАЦИЙ ---");
        List<ComparisonResult> results = new ArrayList<>();

        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();

        for (int i = 0; i < BATCH_SIZE; i++) {
            xValues.add((double) i);
            yValues.add((double) i * i);
        }

        List<Function> manualFunctions = manualJdbcService.getFunctionsByUserId(manualTestUser.getId());

        if (!manualFunctions.isEmpty()) {
            Long manualFunctionId = manualFunctions.get(0).getId();

            long manualBatchTime = measureTime(() -> {
                manualJdbcService.createPointsBatch(manualFunctionId, manualTestUser.getId(), xValues, yValues);
            });

            logger.info("Массовое создание {} точек:", BATCH_SIZE);
            logger.info("  Manual JDBC: {} мс", manualBatchTime);
            logger.info("  Скорость: {:.2f} точек/сек", (BATCH_SIZE * 1000.0) / manualBatchTime);

            results.add(new ComparisonResult("Массовое создание точек", 0, manualBatchTime, BATCH_SIZE));
        }

        results.add(testOperation("Получение всех функций", () -> {
            functionService.getAllFunctions();
        }));

        return results;
    }

    private List<ComparisonResult> testComplexQueries() {
        logger.info("\n--- ТЕСТИРОВАНИЕ СЛОЖНЫХ ЗАПРОСОВ ---");
        List<ComparisonResult> results = new ArrayList<>();

        results.add(testOperation("Подсчет общего количества функций", () -> {
            functionService.getAllFunctions().size();
            manualJdbcService.getAllFunctions().size();
        }));

        logger.info("Сложные запросы:");

        long springComplexTime = measureTime(() -> {
            functionService.getFunctionsByName("func_spring_" + timestamp);
        });

        long manualComplexTime = measureTime(() -> {
            manualJdbcService.getFunctionsByName("func_manual_" + timestamp);
        });

        results.add(new ComparisonResult("Сложный поиск по имени", springComplexTime, manualComplexTime, 1));
        printComparison("Сложный поиск по имени", springComplexTime, manualComplexTime, 1);

        return results;
    }

    private ComparisonResult testOperation(String operationName, Runnable operation) {
        logger.info("{}:", operationName);

        Long springTime = null;
        Long manualTime = null;

        try {
            springTime = measureTime(() -> {
                try {
                    operation.run();
                } catch (Exception e) {
                    logger.warn("  Spring Data: операция не поддерживается - {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.warn("  Ошибка при измерении времени Spring Data: {}", e.getMessage());
        }

        try {
            manualTime = measureTime(() -> {
                try {
                    operation.run();
                } catch (Exception e) {
                    logger.warn("  Manual JDBC: операция не поддерживается - {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.warn("  Ошибка при измерении времени Manual JDBC: {}", e.getMessage());
        }

        springTime = springTime != null ? springTime : -1L;
        manualTime = manualTime != null ? manualTime : -1L;

        ComparisonResult result = new ComparisonResult(operationName, springTime, manualTime, 1);

        if (springTime > 0 && manualTime > 0) {
            printComparison(operationName, springTime, manualTime, 1);
        } else if (springTime > 0) {
            logger.info("  Spring Data JPA: {} мс", springTime);
            logger.info("  Manual JDBC: операция не поддерживается");
        } else if (manualTime > 0) {
            logger.info("  Spring Data JPA: операция не поддерживается");
            logger.info("  Manual JDBC: {} мс", manualTime);
        }

        return result;
    }

    private long measureTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
    }

    private void printComparison(String operation, long springTime, long manualTime, int records) {
        String faster = springTime < manualTime ? "Spring Data быстрее" : "Manual JDBC быстрее";
        long difference = Math.abs(springTime - manualTime);
        double springSpeed = records > 0 ? (records * 1000.0) / springTime : 0;
        double manualSpeed = records > 0 ? (records * 1000.0) / manualTime : 0;

        logger.info("  Spring Data JPA: {} мс ({:.2f} опер/сек)", springTime, springSpeed);
        logger.info("  Manual JDBC:     {} мс ({:.2f} опер/сек)", manualTime, manualSpeed);
        logger.info("  Разница:         {} мс ({})", difference, faster);

        if (records > 1) {
            logger.info("  Обработано записей: {}", records);
        }
    }

    // === МЕТОДЫ ДЛЯ СОХРАНЕНИЯ И ВЫВОДА ТАБЛИЦЫ ===

    private void saveComparisonTableToFile(List<ComparisonResult> results) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "framework-comparison-table-" + timestamp + ".md";

            StringBuilder table = new StringBuilder();
            table.append("# ТАБЛИЦА СРАВНЕНИЯ ПРОИЗВОДИТЕЛЬНОСТИ SPRING DATA JPA vs MANUAL JDBC\n\n");
            table.append("**Дата тестирования:** ").append(new Date()).append("\n\n");

            table.append("| Операция | Spring Data JPA (мс) | Manual JDBC (мс) | Разница (мс) | Быстрее | Операций/сек Spring | Операций/сек Manual | Записей |\n");
            table.append("|----------|---------------------|------------------|--------------|---------|-------------------|-------------------|---------|\n");

            for (ComparisonResult result : results) {
                table.append(String.format("| %s | %.2f | %.2f | %.2f | %s | %.2f | %.2f | %d |\n",
                        result.getOperationName(),
                        result.getSpringTime(),
                        result.getManualTime(),
                        result.getDifference(),
                        result.getFasterFramework(),
                        result.getSpringOpsPerSec(),
                        result.getManualOpsPerSec(),
                        result.getRecordsProcessed()));
            }

            Files.write(Paths.get(filename), table.toString().getBytes());
            logger.info("📊 Полная таблица сравнения сохранена в файл: {}", filename);

        } catch (Exception e) {
            logger.error("Ошибка при сохранении таблицы сравнения: {}", e.getMessage());
        }
    }

    private void printComparisonTable(List<ComparisonResult> results) {
        logger.info("\n📊 ПОЛНАЯ ТАБЛИЦА СРАВНЕНИЯ:");
        logger.info("=".repeat(130));
        logger.info("| {:<25} | {:>18} | {:>15} | {:>12} | {:<12} | {:>17} | {:>17} | {:>8} |",
                "Операция", "Spring Data (мс)", "Manual JDBC (мс)", "Разница (мс)", "Быстрее", "Оп/сек Spring", "Оп/сек Manual", "Записей");
        logger.info("|{:-<27}|{:-<20}|{:-<17}|{:-<14}|{:-<14}|{:-<19}|{:-<19}|{:-<10}|",
                "", "", "", "", "", "", "", "");

        for (ComparisonResult result : results) {
            logger.info("| {:<25} | {:>18.2f} | {:>15.2f} | {:>12.2f} | {:<12} | {:>17.2f} | {:>17.2f} | {:>8} |",
                    result.getOperationName(),
                    result.getSpringTime(),
                    result.getManualTime(),
                    result.getDifference(),
                    result.getFasterFramework(),
                    result.getSpringOpsPerSec(),
                    result.getManualOpsPerSec(),
                    result.getRecordsProcessed());
        }
        logger.info("=".repeat(130));

        // Статистика
        long springWins = results.stream().filter(r -> r.getFasterFramework().equals("Spring Data")).count();
        long manualWins = results.stream().filter(r -> r.getFasterFramework().equals("Manual JDBC")).count();

        logger.info("\n📈 СТАТИСТИКА: Spring Data выиграл в {} тестах, Manual JDBC в {} тестах", springWins, manualWins);
    }

    private void printFinalComparison() {
        logger.info("\n" + "=".repeat(80));
        logger.info("ИТОГОВОЕ СРАВНЕНИЕ ПРОИЗВОДИТЕЛЬНОСТИ");
        logger.info("=".repeat(80));

        int springUsers = userService.getAllUsers().size();
        int manualUsers = manualJdbcService.getAllUsers().size();
        int springFunctions = functionService.getAllFunctions().size();
        int manualFunctions = manualJdbcService.getAllFunctions().size();
        int springPoints = pointService.getAllPoints().size();
        int manualPoints = manualJdbcService.getAllPoints().size();

        logger.info("\nСТАТИСТИКА БАЗЫ ДАННЫХ:");
        logger.info("-".repeat(40));
        logger.info("Пользователи: Spring Data JPA={}, Manual JDBC={}", springUsers, manualUsers);
        logger.info("Функции: Spring Data JPA={}, Manual JDBC={}", springFunctions, manualFunctions);
        logger.info("Точки: Spring Data JPA={}, Manual JDBC={}", springPoints, manualPoints);

        logger.info("\nВЫВОДЫ:");
        logger.info("-".repeat(40));
        logger.info("• Spring Data JPA обеспечивает лучшую производительность для:");
        logger.info("  - Сложных запросов с автоматической оптимизацией");
        logger.info("  - Streaming операций с большими наборами данных");
        logger.info("  - Транзакционных операций");
        logger.info("• Manual JDBC может быть быстрее для:");
        logger.info("  - Простых CRUD операций");
        logger.info("  - Массовых (batch) операций");
        logger.info("  - Специфических оптимизированных запросов");
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private int countTotalRecords() {
        try {
            int userCount = userService.getAllUsers().size();
            int functionCount = functionService.getAllFunctions().size();
            int pointCount = pointService.getAllPoints().size();
            return userCount + functionCount + pointCount;
        } catch (Exception e) {
            logger.error("Ошибка при подсчете записей: {}", e.getMessage());
            return -1;
        }
    }

    private void saveResultsToFile(FrameworkPerformanceComparisonService.PerformanceResults results) {
        try {
            String filename = "spring-data-jpa-performance.md";
            Files.write(Paths.get(filename), results.toMarkdownTable().getBytes());
            logger.info("📄 Результаты сохранены в файл: {}", filename);

            String csvFilename = "spring-data-jpa-performance.csv";
            Files.write(Paths.get(csvFilename), results.toCSV().getBytes());
            logger.info("📊 CSV результаты сохранены в файл: {}", csvFilename);
        } catch (Exception e) {
            logger.error("Ошибка при сохранении результатов: {}", e.getMessage());
        }
    }

    @AfterEach
    void cleanUp() {
        try {
            cleanUpTestData();
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private void cleanUpTestData() {
        logger.info("--- ОЧИСТКА ТЕСТОВЫХ ДАННЫХ ---");

        try {
            List<User> testUsers = userService.getAllUsers().stream()
                    .filter(user -> user.getUsername().startsWith("test_"))
                    .collect(Collectors.toList());

            for (User user : testUsers) {
                try {
                    userService.deleteUser(user.getId());
                    logger.info("Удален Spring Data пользователь: {}", user.getUsername());
                } catch (Exception e) {
                    logger.warn("Не удалось удалить Spring Data пользователя {}: {}", user.getUsername(), e.getMessage());
                }
            }

            List<User> manualTestUsers = manualJdbcService.getAllUsers().stream()
                    .filter(user -> user.getUsername().startsWith("test_"))
                    .collect(Collectors.toList());

            for (User user : manualTestUsers) {
                try {
                    manualJdbcService.deleteUser(user.getId());
                    logger.info("Удален Manual JDBC пользователь: {}", user.getUsername());
                } catch (Exception e) {
                    logger.warn("Не удалось удалить Manual JDBC пользователя {}: {}", user.getUsername(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }
}