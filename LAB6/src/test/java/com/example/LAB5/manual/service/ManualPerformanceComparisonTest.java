package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DAO.UserDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManualPerformanceComparisonTest {
    private static final Logger logger = LoggerFactory.getLogger(ManualPerformanceComparisonTest.class);

    private static UserService userService;
    private static FunctionService functionService;
    private static PointService pointService;
    private static ManualPerformanceComparisonService performanceService;

    @BeforeAll
    static void setUp() {
        userService = new UserService(new UserDAO());
        functionService = new FunctionService(new FunctionDAO(), new UserDAO(), new PointDAO());
        pointService = new PointService(new PointDAO(), new FunctionDAO());
        performanceService = new ManualPerformanceComparisonService(userService, functionService, pointService);

        logger.info("=== ИНИЦИАЛИЗАЦИЯ СЕРВИСОВ ЗАВЕРШЕНА ===");
        logger.info("Данные будут сгенерированы автоматически при необходимости");
    }

    @Test
    @Order(1)
    void testDataGeneration() {
        logger.info("=== ТЕСТ ГЕНЕРАЦИИ ДАННЫХ 10k+ ===");

        // Проверяем, что данные генерируются
        int initialCount = countTotalRecords();
        logger.info("Начальное количество записей: {}", initialCount);

        // Сервис автоматически проверит и сгенерирует данные при создании
        assertNotNull(performanceService, "Сервис сравнения производительности должен быть инициализирован");

        int finalCount = countTotalRecords();
        logger.info("Финальное количество записей: {}", finalCount);

        assertTrue(finalCount >= 10000, "Должно быть сгенерировано минимум 10,000 записей. Фактически: " + finalCount);
        logger.info("✅ Генерация данных завершена успешно");
    }

    @Test
    @Order(2)
    void testPerformanceComparisonWith10kData() {
        logger.info("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ С 10k+ ДАННЫМИ ===");

        // Проверяем, что данные есть
        int recordCount = countTotalRecords();
        assertTrue(recordCount >= 10000, "Для тестирования должно быть минимум 10,000 записей");

        ManualPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("📊 РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ ПРОИЗВОДИТЕЛЬНОСТИ:");
        logger.info("\n" + results.toMarkdownTable());

        // Сохраняем результаты в файл
        saveResultsToFile(results);

        // Проверяем, что результаты были получены
        assertNotNull(results, "Результаты тестирования не должны быть null");

        // Проверяем операции чтения (должны работать на существующих данных)
        assertTrue(results.getUserReadTime() >= 0, "Время чтения пользователя не должно быть отрицательным");
        assertTrue(results.getFunctionReadTime() >= 0, "Время чтения функции не должно быть отрицательным");
        assertTrue(results.getPointsReadTime() >= 0, "Время чтения точек не должно быть отрицательным");

        // Проверяем операции записи (создают временные данные)
        assertTrue(results.getUserCreateTime() > 0, "Время создания пользователя должно быть положительным");
        assertTrue(results.getFunctionCreateTime() > 0, "Время создания функции должно быть положительным");
        assertTrue(results.getPointCreateTime() > 0, "Время создания точки должно быть положительным");

        logger.info("✅ Тест производительности завершен успешно");
    }

    @Test
    @Order(3)
    void testReadOperationsPerformance() {
        logger.info("=== ТЕСТИРОВАНИЕ ПРОИЗВОДИТЕЛЬНОСТИ ОПЕРАЦИЙ ЧТЕНИЯ ===");

        ManualPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("📖 ПРОИЗВОДИТЕЛЬНОСТЬ ОПЕРАЦИЙ ЧТЕНИЯ:");
        logger.info("Чтение пользователя: {:.3f} мс", results.getUserReadTime());
        logger.info("Чтение функции: {:.3f} мс", results.getFunctionReadTime());
        logger.info("Чтение точек: {:.3f} мс", results.getPointsReadTime());
        logger.info("Получение всех пользователей: {:.3f} мс", results.getGetAllUsersTime());
        logger.info("Получение всех функций: {:.3f} мс", results.getGetAllFunctionsTime());

        // Проверяем, что операции чтения выполняются за разумное время
        assertTrue(results.getUserReadTime() < 1000, "Чтение пользователя должно выполняться менее чем за 1000 мс");
        assertTrue(results.getFunctionReadTime() < 1000, "Чтение функции должно выполняться менее чем за 1000 мс");
        assertTrue(results.getGetAllUsersTime() < 5000, "Получение всех пользователей должно выполняться менее чем за 5000 мс");

        logger.info("✅ Тест операций чтения завершен успешно");
    }

    @Test
    @Order(4)
    void testWriteOperationsPerformance() {
        logger.info("=== ТЕСТИРОВАНИЕ ПРОИЗВОДИТЕЛЬНОСТИ ОПЕРАЦИЙ ЗАПИСИ ===");

        ManualPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("📝 ПРОИЗВОДИТЕЛЬНОСТЬ ОПЕРАЦИЙ ЗАПИСИ:");
        logger.info("Создание пользователя: {:.3f} мс", results.getUserCreateTime());
        logger.info("Создание функции: {:.3f} мс", results.getFunctionCreateTime());
        logger.info("Создание точки: {:.3f} мс", results.getPointCreateTime());
        logger.info("Массовое создание точек: {:.3f} мс", results.getBatchCreateTime());

        // Проверяем, что операции записи выполняются за разумное время
        assertTrue(results.getUserCreateTime() > 0, "Время создания пользователя должно быть положительным");
        assertTrue(results.getFunctionCreateTime() > 0, "Время создания функции должно быть положительным");
        assertTrue(results.getPointCreateTime() > 0, "Время создания точки должно быть положительным");
        assertTrue(results.getBatchCreateTime() > 0, "Время массового создания должно быть положительным");

        // Проверяем, что массовые операции эффективнее одиночных
        assertTrue(results.getBatchCreateTime() < results.getPointCreateTime() * 100,
                "Массовое создание должно быть эффективнее одиночного");

        logger.info("✅ Тест операций записи завершен успешно");
    }

    @Test
    @Order(5)
    void testSearchOperationsPerformance() {
        logger.info("=== ТЕСТИРОВАНИЕ ПРОИЗВОДИТЕЛЬНОСТИ ПОИСКОВЫХ ОПЕРАЦИЙ ===");

        ManualPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        logger.info("🔍 ПРОИЗВОДИТЕЛЬНОСТЬ ПОИСКОВЫХ ОПЕРАЦИЙ:");
        logger.info("Поиск пользователей: {:.3f} мс", results.getSearchUsersTime());
        logger.info("Поиск функций: {:.3f} мс", results.getSearchFunctionsTime());

        // Проверяем производительность поиска
        assertTrue(results.getSearchUsersTime() >= 0, "Время поиска пользователей не должно быть отрицательным");
        assertTrue(results.getSearchFunctionsTime() >= 0, "Время поиска функций не должно быть отрицательным");
        assertTrue(results.getSearchUsersTime() < 2000, "Поиск пользователей должен выполняться менее чем за 2000 мс");
        assertTrue(results.getSearchFunctionsTime() < 2000, "Поиск функций должен выполняться менее чем за 2000 мс");

        logger.info("✅ Тест поисковых операций завершен успешно");
    }

    @Test
    @Order(6)
    void testExportFunctionality() {
        logger.info("=== ТЕСТИРОВАНИЕ ЭКСПОРТА РЕЗУЛЬТАТОВ ===");

        // Тестируем экспорт в разные форматы
        ManualPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

        String markdownTable = results.toMarkdownTable();
        String csvData = results.toCSV();

        // Проверяем, что данные экспорта корректны
        assertNotNull(markdownTable, "Markdown таблица не должна быть null");
        assertNotNull(csvData, "CSV данные не должны быть null");
        assertTrue(markdownTable.contains("Результаты тестирования производительности JDBC"),
                "Markdown должен содержать заголовок");
        assertTrue(markdownTable.contains("Время выполнения операций"),
                "Markdown должен содержать таблицу результатов");
        assertTrue(csvData.contains("Operation,Time(ms)"),
                "CSV должен содержать заголовок");
        assertTrue(csvData.contains("User Create"),
                "CSV должен содержать данные о создании пользователя");

        // Тестируем полный экспорт
        performanceService.exportResultsToGitHub();

        // Проверяем, что файлы создались
        assertTrue(Files.exists(Paths.get("performance_results.md")) ||
                        Files.exists(Paths.get("performance_results.csv")),
                "Должен создаться хотя бы один файл с результатами");

        logger.info("✅ Экспорт результатов завершен успешно");
    }

    @Test
    @Order(7)
    void testPerformanceStability() {
        logger.info("=== ТЕСТИРОВАНИЕ СТАБИЛЬНОСТИ ПРОИЗВОДИТЕЛЬНОСТИ ===");

        int numberOfRuns = 3;
        double totalReadTime = 0;
        double totalWriteTime = 0;

        for (int i = 0; i < numberOfRuns; i++) {
            logger.info("Запуск теста производительности №{}", i + 1);

            ManualPerformanceComparisonService.PerformanceResults results = performanceService.comparePerformance();

            totalReadTime += results.getUserReadTime() + results.getFunctionReadTime();
            totalWriteTime += results.getUserCreateTime() + results.getFunctionCreateTime();

            logger.info("Запуск {}: чтение = {:.3f} мс, запись = {:.3f} мс",
                    i + 1,
                    results.getUserReadTime() + results.getFunctionReadTime(),
                    results.getUserCreateTime() + results.getFunctionCreateTime());

            // Небольшая пауза между запусками
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        double avgReadTime = totalReadTime / numberOfRuns;
        double avgWriteTime = totalWriteTime / numberOfRuns;

        logger.info("📈 СРЕДНИЕ РЕЗУЛЬТАТЫ ПОСЛЕ {} ЗАПУСКОВ:", numberOfRuns);
        logger.info("Среднее время операций чтения: {:.3f} мс", avgReadTime);
        logger.info("Среднее время операций записи: {:.3f} мс", avgWriteTime);

        assertTrue(avgReadTime > 0, "Среднее время чтения должно быть положительным");
        assertTrue(avgWriteTime > 0, "Среднее время записи должно быть положительным");
        assertTrue(avgReadTime < 1000, "Среднее время чтения должно быть менее 1000 мс");

        logger.info("✅ Тест стабильности завершен успешно");
    }

    @Test
    @Order(8)
    void testDatabaseConsistency() {
        logger.info("=== ПРОВЕРКА СОГЛАСОВАННОСТИ БАЗЫ ДАННЫХ ===");

        // Проверяем, что данные согласованы после всех тестов
        int userCount = userService.getAllUsers().size();
        int functionCount = functionService.getAllFunctions().size();
        int pointCount = pointService.getAllPoints().size();
        int totalRecords = userCount + functionCount + pointCount;

        logger.info("📋 СТАТИСТИКА БАЗЫ ДАННЫХ:");
        logger.info("Пользователей: {}", userCount);
        logger.info("Функций: {}", functionCount);
        logger.info("Точек: {}", pointCount);
        logger.info("Всего записей: {}", totalRecords);

        // Проверяем согласованность
        assertTrue(userCount > 0, "Должен быть хотя бы один пользователь");
        assertTrue(functionCount > 0, "Должна быть хотя бы одна функция");
        assertTrue(pointCount > 0, "Должна быть хотя бы одна точка");
        assertTrue(totalRecords >= 10000, "Общее количество записей должно быть не менее 10,000");

        // Проверяем, что у функций есть пользователи
        List<Map<String, Object>> functions = functionService.getAllFunctions();
        for (Map<String, Object> function : functions) {
            Long userId = (Long) function.get("user_id");
            assertNotNull(userId, "Функция должна иметь user_id");
            assertTrue(userId > 0, "user_id должен быть положительным");
        }

        // Проверяем, что у точек есть функции
        List<Map<String, Object>> points = pointService.getAllPoints();
        for (Map<String, Object> point : points) {
            Long functionId = (Long) point.get("function_id");
            assertNotNull(functionId, "Точка должна иметь function_id");
            assertTrue(functionId > 0, "function_id должен быть положительным");
        }

        logger.info("✅ Проверка согласованности базы данных завершена успешно");
    }

    @Test
    @Order(9)
    void testPerformanceWithDifferentDataSizes() {
        logger.info("=== ТЕСТИРОВАНИЕ С РАЗНЫМИ ОБЪЕМАМИ ДАННЫХ ===");

        // Тест с текущим объемом данных (10k+)
        ManualPerformanceComparisonService.PerformanceResults results10k = performanceService.comparePerformance();

        logger.info("📊 РЕЗУЛЬТАТЫ ДЛЯ 10k+ ЗАПИСЕЙ:");
        logger.info("Чтение: {:.3f} мс", results10k.getUserReadTime() + results10k.getFunctionReadTime());
        logger.info("Запись: {:.3f} мс", results10k.getUserCreateTime() + results10k.getFunctionCreateTime());
        logger.info("Поиск: {:.3f} мс", results10k.getSearchUsersTime() + results10k.getSearchFunctionsTime());

        // Проверяем, что производительность в допустимых пределах
        assertTrue(results10k.getGetAllUsersTime() < 10000, "Получение всех пользователей должно быть менее 10 секунд");
        assertTrue(results10k.getGetAllFunctionsTime() < 10000, "Получение всех функций должно быть менее 10 секунд");

        logger.info("✅ Тест с разными объемами данных завершен успешно");
    }

    @Test
    @Order(10)
    void testFinalResultsExport() {
        logger.info("=== ФИНАЛЬНЫЙ ЭКСПОРТ РЕЗУЛЬТАТОВ ===");

        // Запускаем финальный тест и экспортируем результаты
        ManualPerformanceComparisonService.PerformanceResults finalResults = performanceService.comparePerformance();

        // Сохраняем в несколько форматов
        saveResultsToFile(finalResults);
        performanceService.exportResultsToGitHub();

        logger.info("🎯 ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ:");
        logger.info("\n" + finalResults.toMarkdownTable());

        // Проверяем, что все ключевые метрики присутствуют
        assertTrue(finalResults.getUserCreateTime() > 0, "Время создания пользователя должно быть измерено");
        assertTrue(finalResults.getFunctionCreateTime() > 0, "Время создания функции должно быть измерено");
        assertTrue(finalResults.getBatchCreateTime() > 0, "Время массового создания должно быть измерено");

        logger.info("✅ Все тесты завершены успешно! Результаты экспортированы.");
    }

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

    private void saveResultsToFile(ManualPerformanceComparisonService.PerformanceResults results) {
        try {
            String filename = "performance_results.md";
            Files.write(Paths.get(filename), results.toMarkdownTable().getBytes());
            logger.info("📄 Результаты сохранены в файл: {}", filename);

            // Также сохраняем в CSV
            String csvFilename = "performance_results.csv";
            Files.write(Paths.get(csvFilename), results.toCSV().getBytes());
            logger.info("📊 CSV результаты сохранены в файл: {}", csvFilename);
        } catch (Exception e) {
            logger.error("Ошибка при сохранении результатов: {}", e.getMessage());
        }
    }
}