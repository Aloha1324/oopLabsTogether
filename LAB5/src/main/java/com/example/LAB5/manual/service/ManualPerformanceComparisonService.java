package com.example.LAB5.manual.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManualPerformanceComparisonService {
    private static final Logger logger = LoggerFactory.getLogger(ManualPerformanceComparisonService.class);
    private static boolean testDataGenerated = false;

    // Оптимизированные константы для быстрой генерации 10k+ записей
    private static final int OPTIMIZED_USER_COUNT = 50;
    private static final int OPTIMIZED_FUNCTION_COUNT = 200;
    private static final int OPTIMIZED_POINT_COUNT = 9800;

    private final UserService userService;
    private final FunctionService functionService;
    private final PointService pointService;

    public ManualPerformanceComparisonService(UserService userService,
                                              FunctionService functionService,
                                              PointService pointService) {
        this.userService = userService;
        this.functionService = functionService;
        this.pointService = pointService;

        ensureTestDataExists();
    }

    private void ensureTestDataExists() {
        if (!testDataGenerated) {
            if (!verify10kDataExists()) {
                generate10kTestDataOptimized();
            }
            testDataGenerated = true;
        }
    }

    private boolean verify10kDataExists() {
        int totalRecords = countTotalRecords();
        logger.info("Текущее количество записей в базе: {}", totalRecords);

        if (totalRecords < 10000) {
            logger.warn("Недостаточно данных ({}), требуется генерация", totalRecords);
            return false;
        }

        logger.info("Достаточно данных для тестирования: {} записей", totalRecords);
        return true;
    }

    private void generate10kTestDataOptimized() {
        logger.info("Быстрая генерация 10k+ тестовых данных...");

        try (Connection conn = com.example.LAB5.manual.config.DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // Генерация пользователей
            generateUsersBatch(conn, OPTIMIZED_USER_COUNT);

            // Генерация функций
            generateFunctionsBatch(conn, OPTIMIZED_FUNCTION_COUNT);

            // Генерация точек
            generatePointsBatch(conn, OPTIMIZED_POINT_COUNT);

            conn.commit();
            logger.info("Генерация 10k+ данных завершена");

        } catch (SQLException e) {
            logger.error("Ошибка генерации данных", e);
        }
    }

    private void generateUsersBatch(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                stmt.setString(1, "batch_user_" + i);
                stmt.setString(2, "password");
                stmt.addBatch();

                if (i % 50 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
        logger.info("Создано {} пользователей", count);
    }

    private void generateFunctionsBatch(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO functions (name, user_id, expression) VALUES (?, ?, ?)";

        // Получаем существующие ID пользователей
        List<Long> userIds = getExistingUserIds(conn);
        if (userIds.isEmpty()) {
            logger.warn("Нет пользователей для создания функций");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                Long userId = userIds.get(i % userIds.size());
                stmt.setString(1, "batch_func_" + i);
                stmt.setLong(2, userId);
                stmt.setString(3, "f(x) = x^" + (i % 5 + 1));
                stmt.addBatch();

                if (i % 100 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
        logger.info("Создано {} функций", count);
    }

    private void generatePointsBatch(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO points (function_id, x, y) VALUES (?, ?, ?)";

        // Получаем существующие ID функций
        List<Long> functionIds = getExistingFunctionIds(conn);
        if (functionIds.isEmpty()) {
            logger.warn("Нет функций для создания точек");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                Long functionId = functionIds.get(i % functionIds.size());
                stmt.setLong(1, functionId);
                stmt.setDouble(2, Math.random() * 100 - 50);
                stmt.setDouble(3, Math.random() * 100);
                stmt.addBatch();

                if (i % 1000 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
        logger.info("Создано {} точек", count);
    }

    private List<Long> getExistingUserIds(Connection conn) throws SQLException {
        List<Long> userIds = new ArrayList<>();
        String sql = "SELECT id FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {
            while (rs.next()) {
                userIds.add(rs.getLong("id"));
            }
        }
        return userIds;
    }

    private List<Long> getExistingFunctionIds(Connection conn) throws SQLException {
        List<Long> functionIds = new ArrayList<>();
        String sql = "SELECT id FROM functions";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {
            while (rs.next()) {
                functionIds.add(rs.getLong("id"));
            }
        }
        return functionIds;
    }

    private int countTotalRecords() {
        try {
            // Используем существующие методы
            int userCount = userService.getAllUsers().size();
            int functionCount = functionService.getAllFunctions().size();
            int pointCount = pointService.getAllPoints().size();

            return userCount + functionCount + pointCount;
        } catch (Exception e) {
            logger.error("Ошибка при подсчете записей: {}", e.getMessage());
            return -1;
        }
    }

    public PerformanceResults comparePerformance() {
        PerformanceResults results = new PerformanceResults();

        // Тестирование на существующих данных (10k+ записей)
        testReadOperations(results);
        testWriteOperations(results);
        testComplexQueries(results);
        testBatchOperations(results);

        return results;
    }

    private void testReadOperations(PerformanceResults results) {
        long startTime, endTime;

        // Находим существующие данные для тестирования
        Long existingUserId = findExistingUserId();
        Long existingFunctionId = findExistingFunctionId();

        if (existingUserId == null || existingFunctionId == null) {
            logger.warn("Не найдены существующие данные для тестирования");
            return;
        }

        // Тест чтения пользователя
        startTime = System.nanoTime();
        userService.getUserById(existingUserId);
        endTime = System.nanoTime();
        results.setUserReadTime((endTime - startTime) / 1_000_000.0);

        // Тест чтения функции
        startTime = System.nanoTime();
        functionService.getFunctionById(existingFunctionId);
        endTime = System.nanoTime();
        results.setFunctionReadTime((endTime - startTime) / 1_000_000.0);

        // Тест чтения точек
        startTime = System.nanoTime();
        pointService.getPointsByFunctionId(existingFunctionId);
        endTime = System.nanoTime();
        results.setPointsReadTime((endTime - startTime) / 1_000_000.0);
    }

    private void testWriteOperations(PerformanceResults results) {
        long startTime, endTime;

        // Тест создания пользователя
        startTime = System.nanoTime();
        String login = "test_user_" + System.currentTimeMillis();
        Long userId = userService.createUser(login, "testpass");
        endTime = System.nanoTime();
        results.setUserCreateTime((endTime - startTime) / 1_000_000.0);

        // Тест создания функции
        startTime = System.nanoTime();
        Long functionId = functionService.createFunction("test_func", userId, "f(x) = x^2");
        endTime = System.nanoTime();
        results.setFunctionCreateTime((endTime - startTime) / 1_000_000.0);

        // Тест создания точки
        startTime = System.nanoTime();
        pointService.createPoint(functionId, 1.0, 1.0);
        endTime = System.nanoTime();
        results.setPointCreateTime((endTime - startTime) / 1_000_000.0);

        // Очистка тестовых данных
        try {
            userService.deleteUser(userId);
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private void testComplexQueries(PerformanceResults results) {
        long startTime, endTime;

        // Тест получения всех пользователей
        startTime = System.nanoTime();
        userService.getAllUsers();
        endTime = System.nanoTime();
        results.setGetAllUsersTime((endTime - startTime) / 1_000_000.0);

        // Тест получения всех функций
        startTime = System.nanoTime();
        functionService.getAllFunctions();
        endTime = System.nanoTime();
        results.setGetAllFunctionsTime((endTime - startTime) / 1_000_000.0);

        // Тест поиска пользователей
        startTime = System.nanoTime();
        userService.getUsersByUsernamePattern("batch_user");
        endTime = System.nanoTime();
        results.setSearchUsersTime((endTime - startTime) / 1_000_000.0);

        // Тест поиска функций
        startTime = System.nanoTime();
        functionService.getFunctionsByName("batch_func");
        endTime = System.nanoTime();
        results.setSearchFunctionsTime((endTime - startTime) / 1_000_000.0);
    }

    private void testBatchOperations(PerformanceResults results) {
        long startTime, endTime;

        // Создаем временные данные для тестирования batch операций
        String login = "batch_test_" + System.currentTimeMillis();
        Long userId = userService.createUser(login, "password");
        Long functionId = functionService.createFunction("batch_func", userId, "f(x) = x");

        // Тест массового создания точек
        startTime = System.nanoTime();
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            xValues.add((double) i);
            yValues.add((double) i * i);
        }
        pointService.createPointsBatch(functionId, xValues, yValues);
        endTime = System.nanoTime();
        results.setBatchCreateTime((endTime - startTime) / 1_000_000.0);

        // Очистка
        try {
            userService.deleteUser(userId);
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private Long findExistingUserId() {
        try {
            // Сначала ищем по шаблону perf_user_ (генерируются в generate10kTestData)
            List<Map<String, Object>> users = userService.getUsersByUsernamePattern("perf_user");

            // Если не найдено, ищем по batch_user_ (генерируются в generate10kTestDataOptimized)
            if (users.isEmpty()) {
                users = userService.getUsersByUsernamePattern("batch_user");
            }

            // Если все еще не найдено, берем любого пользователя
            if (users.isEmpty()) {
                users = userService.getAllUsersLimited(1);
            }

            logger.debug("Найдено пользователей для тестирования: {}", users.size());
            return users.isEmpty() ? null : (Long) users.get(0).get("id");

        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя для тестирования: {}", e.getMessage());
            return null;
        }
    }

    private Long findExistingFunctionId() {
        try {
            // Сначала ищем по шаблону func_ (генерируются в generate10kTestData)
            List<Map<String, Object>> functions = functionService.getFunctionsByName("func_");

            // Если не найдено, ищем по batch_func_ (генерируются в generate10kTestDataOptimized)
            if (functions.isEmpty()) {
                functions = functionService.getFunctionsByName("batch_func");
            }

            // Если все еще не найдено, берем любую функцию
            if (functions.isEmpty()) {
                functions = functionService.getAllFunctionsLimited(1);
            }

            logger.debug("Найдено функций для тестирования: {}", functions.size());
            return functions.isEmpty() ? null : (Long) functions.get(0).get("id");

        } catch (Exception e) {
            logger.error("Ошибка при поиске функции для тестирования: {}", e.getMessage());
            return null;
        }
    }
    public void exportResultsToGitHub() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        try {
            // Запускаем тесты производительности
            PerformanceResults results = comparePerformance();

            // Сохраняем в файлы
            saveToMarkdown(results, timestamp);
            saveToCSV(results, timestamp);

            logger.info("Результаты экспортированы с временной меткой: {}", timestamp);

        } catch (Exception e) {
            logger.error("Ошибка при экспорте результатов: {}", e.getMessage());
        }
    }

    private void saveToMarkdown(PerformanceResults results, String timestamp) {
        String filename = "performance_results_" + timestamp + ".md";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(results.toMarkdownTable());
            logger.info("Markdown таблица сохранена в: {}", filename);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении Markdown: {}", e.getMessage());
        }
    }

    private void saveToCSV(PerformanceResults results, String timestamp) {
        String filename = "performance_results_" + timestamp + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(results.toCSV());
            logger.info("CSV таблица сохранена в: {}", filename);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении CSV: {}", e.getMessage());
        }
    }

    public static class PerformanceResults {
        private double userCreateTime;
        private double userReadTime;
        private double functionCreateTime;
        private double functionReadTime;
        private double pointCreateTime;
        private double pointsReadTime;
        private double batchCreateTime;
        private double getAllUsersTime;
        private double getAllFunctionsTime;
        private double searchUsersTime;
        private double searchFunctionsTime;

        // Getters and Setters
        public double getUserCreateTime() { return userCreateTime; }
        public void setUserCreateTime(double userCreateTime) { this.userCreateTime = userCreateTime; }

        public double getUserReadTime() { return userReadTime; }
        public void setUserReadTime(double userReadTime) { this.userReadTime = userReadTime; }

        public double getFunctionCreateTime() { return functionCreateTime; }
        public void setFunctionCreateTime(double functionCreateTime) { this.functionCreateTime = functionCreateTime; }

        public double getFunctionReadTime() { return functionReadTime; }
        public void setFunctionReadTime(double functionReadTime) { this.functionReadTime = functionReadTime; }

        public double getPointCreateTime() { return pointCreateTime; }
        public void setPointCreateTime(double pointCreateTime) { this.pointCreateTime = pointCreateTime; }

        public double getPointsReadTime() { return pointsReadTime; }
        public void setPointsReadTime(double pointsReadTime) { this.pointsReadTime = pointsReadTime; }

        public double getBatchCreateTime() { return batchCreateTime; }
        public void setBatchCreateTime(double batchCreateTime) { this.batchCreateTime = batchCreateTime; }

        public double getGetAllUsersTime() { return getAllUsersTime; }
        public void setGetAllUsersTime(double getAllUsersTime) { this.getAllUsersTime = getAllUsersTime; }

        public double getGetAllFunctionsTime() { return getAllFunctionsTime; }
        public void setGetAllFunctionsTime(double getAllFunctionsTime) { this.getAllFunctionsTime = getAllFunctionsTime; }

        public double getSearchUsersTime() { return searchUsersTime; }
        public void setSearchUsersTime(double searchUsersTime) { this.searchUsersTime = searchUsersTime; }

        public double getSearchFunctionsTime() { return searchFunctionsTime; }
        public void setSearchFunctionsTime(double searchFunctionsTime) { this.searchFunctionsTime = searchFunctionsTime; }

        public String toMarkdownTable() {
            return """
                    # Результаты тестирования производительности JDBC
                    
                    **Дата тестирования:** %s
                    
                    **Размер данных:** 10,000+ записей
                    
                    ## Время выполнения операций (мс)
                    
                    | Операция | Время (мс) |
                    |----------|------------|
                    | Создание пользователя | %.3f |
                    | Чтение пользователя | %.3f |
                    | Создание функции | %.3f |
                    | Чтение функции | %.3f |
                    | Создание точки | %.3f |
                    | Чтение точек функции | %.3f |
                    | Массовое создание (100 точек) | %.3f |
                    | Получение всех пользователей | %.3f |
                    | Получение всех функций | %.3f |
                    | Поиск пользователей | %.3f |
                    | Поиск функций | %.3f |
                    """.formatted(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    userCreateTime, userReadTime,
                    functionCreateTime, functionReadTime,
                    pointCreateTime, pointsReadTime,
                    batchCreateTime,
                    getAllUsersTime, getAllFunctionsTime,
                    searchUsersTime, searchFunctionsTime
            );
        }

        public String toCSV() {
            return """
                    Operation,Time(ms)
                    User Create,%.3f
                    User Read,%.3f
                    Function Create,%.3f
                    Function Read,%.3f
                    Point Create,%.3f
                    Points Read,%.3f
                    Batch Create (100 points),%.3f
                    Get All Users,%.3f
                    Get All Functions,%.3f
                    Search Users,%.3f
                    Search Functions,%.3f
                    """.formatted(
                    userCreateTime, userReadTime,
                    functionCreateTime, functionReadTime,
                    pointCreateTime, pointsReadTime,
                    batchCreateTime,
                    getAllUsersTime, getAllFunctionsTime,
                    searchUsersTime, searchFunctionsTime
            );
        }
    }
}