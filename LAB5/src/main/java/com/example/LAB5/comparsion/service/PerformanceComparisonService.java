package com.example.LAB5.comparsion.service;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.service.UserService;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PerformanceComparisonService {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceComparisonService.class);

    private final UserService userService;
    private final FunctionService functionService;
    private final PointService pointService;

    @Autowired
    public PerformanceComparisonService(
            UserService userService,
            FunctionService functionService,
            PointService pointService) {
        this.userService = userService;
        this.functionService = functionService;
        this.pointService = pointService;
    }

    public static class PerformanceResults {
        private double userReadTime;
        private double functionReadTime;
        private double pointsReadTime;
        private double getAllUsersTime;
        private double getAllFunctionsTime;
        private double userCreateTime;
        private double functionCreateTime;
        private double pointCreateTime;
        private double batchCreateTime;
        private double searchUsersTime;
        private double searchFunctionsTime;

        // Геттеры и сеттеры
        public double getUserReadTime() { return userReadTime; }
        public void setUserReadTime(double userReadTime) { this.userReadTime = userReadTime; }

        public double getFunctionReadTime() { return functionReadTime; }
        public void setFunctionReadTime(double functionReadTime) { this.functionReadTime = functionReadTime; }

        public double getPointsReadTime() { return pointsReadTime; }
        public void setPointsReadTime(double pointsReadTime) { this.pointsReadTime = pointsReadTime; }

        public double getGetAllUsersTime() { return getAllUsersTime; }
        public void setGetAllUsersTime(double getAllUsersTime) { this.getAllUsersTime = getAllUsersTime; }

        public double getGetAllFunctionsTime() { return getAllFunctionsTime; }
        public void setGetAllFunctionsTime(double getAllFunctionsTime) { this.getAllFunctionsTime = getAllFunctionsTime; }

        public double getUserCreateTime() { return userCreateTime; }
        public void setUserCreateTime(double userCreateTime) { this.userCreateTime = userCreateTime; }

        public double getFunctionCreateTime() { return functionCreateTime; }
        public void setFunctionCreateTime(double functionCreateTime) { this.functionCreateTime = functionCreateTime; }

        public double getPointCreateTime() { return pointCreateTime; }
        public void setPointCreateTime(double pointCreateTime) { this.pointCreateTime = pointCreateTime; }

        public double getBatchCreateTime() { return batchCreateTime; }
        public void setBatchCreateTime(double batchCreateTime) { this.batchCreateTime = batchCreateTime; }

        public double getSearchUsersTime() { return searchUsersTime; }
        public void setSearchUsersTime(double searchUsersTime) { this.searchUsersTime = searchUsersTime; }

        public double getSearchFunctionsTime() { return searchFunctionsTime; }
        public void setSearchFunctionsTime(double searchFunctionsTime) { this.searchFunctionsTime = searchFunctionsTime; }

        public String toMarkdownTable() {
            return String.format(
                    "| Операция | Время (мс) |\n" +
                            "|----------|------------|\n" +
                            "| Чтение пользователя | %.3f |\n" +
                            "| Чтение функции | %.3f |\n" +
                            "| Чтение точек | %.3f |\n" +
                            "| Получение всех пользователей | %.3f |\n" +
                            "| Получение всех функций | %.3f |\n" +
                            "| Создание пользователя | %.3f |\n" +
                            "| Создание функции | %.3f |\n" +
                            "| Создание точки | %.3f |\n" +
                            "| Массовое создание | %.3f |\n" +
                            "| Поиск пользователей | %.3f |\n" +
                            "| Поиск функций | %.3f |",
                    userReadTime, functionReadTime, pointsReadTime,
                    getAllUsersTime, getAllFunctionsTime,
                    userCreateTime, functionCreateTime, pointCreateTime,
                    batchCreateTime, searchUsersTime, searchFunctionsTime
            );
        }
    }

    public PerformanceResults comparePerformance() {
        logger.info("=== НАЧАЛО ТЕСТИРОВАНИЯ ПРОИЗВОДИТЕЛЬНОСТИ SPRING DATA JPA ===");

        PerformanceResults results = new PerformanceResults();

        try {
            // Тестирование операций чтения
            testReadOperations(results);

            // Тестирование операций записи
            testWriteOperations(results);

            // Тестирование поисковых операций
            testSearchOperations(results);

            // Тестирование массовых операций
            testBatchOperations(results);

            // Экспорт результатов
            exportPerformanceResults(results);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании производительности: {}", e.getMessage(), e);
        }

        return results;
    }

    private void testReadOperations(PerformanceResults results) {
        // Тест чтения пользователя
        long startTime = System.nanoTime();
        List<User> users = userService.getAllUsers();
        if (!users.isEmpty()) {
            userService.getUserByIdOrNull(users.get(0).getId());
        }
        long endTime = System.nanoTime();
        results.setUserReadTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Тест чтения функции
        startTime = System.nanoTime();
        List<Function> functions = functionService.getAllFunctions();
        if (!functions.isEmpty()) {
            functionService.getFunctionByIdOrNull(functions.get(0).getId());
        }
        endTime = System.nanoTime();
        results.setFunctionReadTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Тест чтения точек
        startTime = System.nanoTime();
        List<Point> points = pointService.getAllPoints();
        endTime = System.nanoTime();
        results.setPointsReadTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Тест получения всех пользователей
        startTime = System.nanoTime();
        userService.getAllUsers();
        endTime = System.nanoTime();
        results.setGetAllUsersTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Тест получения всех функций
        startTime = System.nanoTime();
        functionService.getAllFunctions();
        endTime = System.nanoTime();
        results.setGetAllFunctionsTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
    }

    private void testWriteOperations(PerformanceResults results) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Тест создания пользователя
        long startTime = System.nanoTime();
        User testUser = userService.createUser("perf_test_user_" + timestamp, "test_password");
        long endTime = System.nanoTime();
        results.setUserCreateTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Тест создания функции
        startTime = System.nanoTime();
        Function function = new Function();
        function.setName("perf_test_func_" + timestamp);
        function.setExpression("x^2");
        function.setUser(testUser);
        Function savedFunction = functionService.saveFunction(function);
        endTime = System.nanoTime();
        results.setFunctionCreateTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Тест создания точки
        startTime = System.nanoTime();
        pointService.createPoint(savedFunction.getId(), testUser.getId(), 1.0, 1.0);
        endTime = System.nanoTime();
        results.setPointCreateTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Очистка тестовых данных
        try {
            pointService.getPointsByFunctionId(savedFunction.getId()).forEach(point ->
                    pointService.deletePoint(point.getId()));
            functionService.deleteFunction(savedFunction.getId());
            userService.deleteUser(testUser.getId());
        } catch (Exception e) {
            logger.warn("Ошибка при очистке тестовых данных: {}", e.getMessage());
        }
    }

    private void testSearchOperations(PerformanceResults results) {
        // Тест поиска пользователей
        long startTime = System.nanoTime();
        List<User> users = userService.getAllUsers();
        if (!users.isEmpty()) {
            userService.getUsersByUsername(users.get(0).getUsername());
        }
        long endTime = System.nanoTime();
        results.setSearchUsersTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Тест поиска функций
        startTime = System.nanoTime();
        List<Function> functions = functionService.getAllFunctions();
        if (!functions.isEmpty()) {
            functionService.getFunctionsByName(functions.get(0).getName());
        }
        endTime = System.nanoTime();
        results.setSearchFunctionsTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
    }

    private void testBatchOperations(PerformanceResults results) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Создание тестового пользователя для batch операций
        User batchUser = userService.createUser("batch_test_user_" + timestamp, "test_password");

        // Создание тестовой функции
        Function batchFunction = new Function();
        batchFunction.setName("batch_test_func_" + timestamp);
        batchFunction.setExpression("x^2");
        batchFunction.setUser(batchUser);
        Function savedBatchFunction = functionService.saveFunction(batchFunction);

        // Тест массового создания точек
        int batchSize = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < batchSize; i++) {
            pointService.createPoint(savedBatchFunction.getId(), batchUser.getId(), (double)i, (double)i*i);
        }

        long endTime = System.nanoTime();
        results.setBatchCreateTime(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // Очистка
        try {
            pointService.getPointsByFunctionId(savedBatchFunction.getId()).forEach(point ->
                    pointService.deletePoint(point.getId()));
            functionService.deleteFunction(savedBatchFunction.getId());
            userService.deleteUser(batchUser.getId());
        } catch (Exception e) {
            logger.warn("Ошибка при очистке batch данных: {}", e.getMessage());
        }
    }

    private void exportPerformanceResults(PerformanceResults results) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        try {
            saveToMarkdown(results, timestamp);
            logger.info("Результаты производительности экспортированы: {}", timestamp);
        } catch (Exception e) {
            logger.error("Ошибка при экспорте результатов: {}", e.getMessage());
        }
    }

    private void saveToMarkdown(PerformanceResults results, String timestamp) throws IOException {
        String filename = "spring-data-performance_" + timestamp + ".md";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("# Результаты производительности Spring Data JPA\n\n");
            writer.write("**Дата тестирования:** " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            writer.write("## Результаты тестов\n\n");
            writer.write(results.toMarkdownTable());
        }
    }
}
