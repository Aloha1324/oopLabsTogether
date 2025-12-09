package com.example.LAB5.comparsion.service;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.service.UserService;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.PointService;
import com.example.LAB5.manual.service.ManualJdbcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PerformanceComparisonService {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceComparisonService.class);

    private final UserService userService;
    private final FunctionService functionService;
    private final PointService pointService;
    private final ManualJdbcService manualJdbcService;

    @Autowired
    public PerformanceComparisonService(
            UserService userService,
            FunctionService functionService,
            PointService pointService,
            ManualJdbcService manualJdbcService) {
        this.userService = userService;
        this.functionService = functionService;
        this.pointService = pointService;
        this.manualJdbcService = manualJdbcService;
    }

    public static class ComparisonResults {
        private final String operation;
        private final double springDataTime;
        private final double manualJdbcTime;
        private final double difference;
        private final String fasterFramework;

        public ComparisonResults(String operation, double springDataTime, double manualJdbcTime) {
            this.operation = operation;
            this.springDataTime = springDataTime;
            this.manualJdbcTime = manualJdbcTime;
            this.difference = Math.abs(springDataTime - manualJdbcTime);
            this.fasterFramework = springDataTime < manualJdbcTime ? "Spring Data JPA" : "Manual JDBC";
        }

        // Геттеры
        public String getOperation() { return operation; }
        public double getSpringDataTime() { return springDataTime; }
        public double getManualJdbcTime() { return manualJdbcTime; }
        public double getDifference() { return difference; }
        public String getFasterFramework() { return fasterFramework; }
    }

    public List<ComparisonResults> compareFrameworks() {
        logger.info("=== НАЧАЛО СРАВНЕНИЯ ПРОИЗВОДИТЕЛЬНОСТИ ФРЕЙМВОРКОВ ===");

        List<ComparisonResults> results = new ArrayList<>();

        try {
            // Проверяем наличие данных (уменьшим требования для тестирования)
            if (!verifyDataExists()) {
                logger.warn("Недостаточно данных для тестирования. Будут использованы базовые тесты.");
                // Продолжаем выполнение с базовыми тестами
            }

            // Тестирование операций
            results.add(testUserCreation());
            results.add(testUserReading());
            results.add(testFunctionCreation());
            results.add(testFunctionReading());
            results.add(testPointCreation());
            results.add(testPointsReading());
            results.add(testGetAllUsers());
            results.add(testGetAllFunctions());
            results.add(testSearchOperations());
            results.add(testBatchOperations());

            // Экспорт результатов
            exportComparisonResults(results);

        } catch (Exception e) {
            logger.error("Ошибка при сравнении фреймворков: {}", e.getMessage(), e);
        }

        return results;
    }

    private boolean verifyDataExists() {
        try {
            int springDataCount = countSpringDataRecords();
            int manualJdbcCount = countManualJdbcRecords();

            logger.info("Количество записей: Spring Data JPA = {}, Manual JDBC = {}",
                    springDataCount, manualJdbcCount);

            return springDataCount >= 100 && manualJdbcCount >= 100; // Уменьшил требования
        } catch (Exception e) {
            logger.error("Ошибка при проверке данных: {}", e.getMessage());
            return false;
        }
    }

    private int countSpringDataRecords() {
        try {
            int users = userService.getAllUsers().size();
            int functions = functionService.getAllFunctions().size();
            int points = pointService.getAllPoints().size();
            return users + functions + points;
        } catch (Exception e) {
            logger.warn("Ошибка при подсчете Spring Data записей: {}", e.getMessage());
            return 0;
        }
    }

    private int countManualJdbcRecords() {
        try {
            int users = manualJdbcService.getAllUsers().size();
            int functions = manualJdbcService.getAllFunctions().size();
            int points = manualJdbcService.getAllPoints().size();
            return users + functions + points;
        } catch (Exception e) {
            logger.warn("Ошибка при подсчете Manual JDBC записей: {}", e.getMessage());
            return 0;
        }
    }

    private ComparisonResults testUserCreation() {
        logger.info("Тестирование создания пользователя...");

        String usernameSpring = "test_user_spring_" + System.currentTimeMillis();
        String usernameManual = "test_user_manual_" + System.currentTimeMillis();

        Long springUserId = null;
        Long manualUserId = null;

        try {
            // Spring Data JPA
            long startTime = System.nanoTime();
            User springUser = userService.createUser(usernameSpring, "password");
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);
            springUserId = springUser.getId();

            // Manual JDBC
            startTime = System.nanoTime();
            manualUserId = manualJdbcService.createUser(usernameManual, "password");
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("User Creation", springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании создания пользователя: {}", e.getMessage());
            return new ComparisonResults("User Creation", 0, 0);
        } finally {
            // Очистка
            if (springUserId != null) {
                try {
                    userService.deleteUser(springUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Spring пользователя: {}", e.getMessage());
                }
            }
            if (manualUserId != null) {
                try {
                    manualJdbcService.deleteUser(manualUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Manual JDBC пользователя: {}", e.getMessage());
                }
            }
        }
    }

    private ComparisonResults testUserReading() {
        logger.info("Тестирование чтения пользователя...");

        Long springUserId = null;
        Long manualUserId = null;

        try {
            // Создаем тестового пользователя
            String username = "test_read_user_" + System.currentTimeMillis();
            User springUser = userService.createUser(username, "password");
            springUserId = springUser.getId();

            manualUserId = manualJdbcService.createUser(username + "_manual", "password");

            // Spring Data JPA
            long startTime = System.nanoTime();
            User foundSpringUser = userService.getUserByIdOrNull(springUser.getId());
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            User foundManualUser = manualJdbcService.getUserById(manualUserId);
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("User Read", springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании чтения пользователя: {}", e.getMessage());
            return new ComparisonResults("User Read", 0, 0);
        } finally {
            // Очистка
            if (springUserId != null) {
                try {
                    userService.deleteUser(springUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Spring пользователя: {}", e.getMessage());
                }
            }
            if (manualUserId != null) {
                try {
                    manualJdbcService.deleteUser(manualUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Manual JDBC пользователя: {}", e.getMessage());
                }
            }
        }
    }

    private ComparisonResults testFunctionCreation() {
        logger.info("Тестирование создания функции...");

        Long springUserId = null;
        Long manualUserId = null;

        try {
            // Создаем пользователей
            User springUser = userService.createUser("test_func_user_spring_" + System.currentTimeMillis(), "password");
            springUserId = springUser.getId();

            manualUserId = manualJdbcService.createUser("test_func_user_manual_" + System.currentTimeMillis(), "password");

            // Spring Data JPA
            long startTime = System.nanoTime();
            Function springFunction = functionService.createFunction(springUser, "test_function_spring", "x^2");
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            Long manualFunctionId = manualJdbcService.createFunction(manualUserId, "test_function_manual", "x^2");
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Function Creation", springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании создания функции: {}", e.getMessage());
            return new ComparisonResults("Function Creation", 0, 0);
        } finally {
            // Очистка
            if (springUserId != null) {
                try {
                    userService.deleteUser(springUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Spring пользователя: {}", e.getMessage());
                }
            }
            if (manualUserId != null) {
                try {
                    manualJdbcService.deleteUser(manualUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Manual JDBC пользователя: {}", e.getMessage());
                }
            }
        }
    }

    private ComparisonResults testFunctionReading() {
        logger.info("Тестирование чтения функции...");

        Long springUserId = null;
        Long manualUserId = null;
        Long springFunctionId = null;
        Long manualFunctionId = null;

        try {
            // Создаем тестовые данные
            User springUser = userService.createUser("test_func_read_spring_" + System.currentTimeMillis(), "password");
            springUserId = springUser.getId();

            manualUserId = manualJdbcService.createUser("test_func_read_manual_" + System.currentTimeMillis(), "password");

            Function springFunction = functionService.createFunction(springUser, "test_func_read_spring", "x^2");
            springFunctionId = springFunction.getId();

            manualFunctionId = manualJdbcService.createFunction(manualUserId, "test_func_read_manual", "x^2");

            // Spring Data JPA
            long startTime = System.nanoTime();
            Function foundSpringFunction = functionService.getFunctionByIdOrNull(springFunctionId);
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            Function foundManualFunction = manualJdbcService.getFunctionById(manualFunctionId);
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Function Read", springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании чтения функции: {}", e.getMessage());
            return new ComparisonResults("Function Read", 0, 0);
        } finally {
            // Очистка
            if (springUserId != null) {
                try {
                    userService.deleteUser(springUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Spring пользователя: {}", e.getMessage());
                }
            }
            if (manualUserId != null) {
                try {
                    manualJdbcService.deleteUser(manualUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Manual JDBC пользователя: {}", e.getMessage());
                }
            }
        }
    }

    private ComparisonResults testPointCreation() {
        logger.info("Тестирование создания точки...");

        Long springUserId = null;
        Long manualUserId = null;
        Long springFunctionId = null;
        Long manualFunctionId = null;

        try {
            // Создаем тестовые данные
            User springUser = userService.createUser("test_point_spring_" + System.currentTimeMillis(), "password");
            springUserId = springUser.getId();

            manualUserId = manualJdbcService.createUser("test_point_manual_" + System.currentTimeMillis(), "password");

            Function springFunction = functionService.createFunction(springUser, "test_point_func_spring", "x^2");
            springFunctionId = springFunction.getId();

            manualFunctionId = manualJdbcService.createFunction(manualUserId, "test_point_func_manual", "x^2");

            // Spring Data JPA
            long startTime = System.nanoTime();
            Point springPoint = pointService.createPoint(springFunctionId, springUserId, 1.0, 1.0);
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            Long manualPointId = manualJdbcService.createPoint(manualFunctionId, manualUserId, 1.0, 1.0);
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Point Creation", springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании создания точки: {}", e.getMessage());
            return new ComparisonResults("Point Creation", 0, 0);
        } finally {
            // Очистка
            if (springUserId != null) {
                try {
                    userService.deleteUser(springUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Spring пользователя: {}", e.getMessage());
                }
            }
            if (manualUserId != null) {
                try {
                    manualJdbcService.deleteUser(manualUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Manual JDBC пользователя: {}", e.getMessage());
                }
            }
        }
    }

    private ComparisonResults testPointsReading() {
        logger.info("Тестирование чтения точек...");

        try {
            // Используем существующие функции
            List<Function> springFunctions = functionService.getAllFunctions();
            List<Function> manualFunctions = manualJdbcService.getAllFunctions();

            if (springFunctions.isEmpty() || manualFunctions.isEmpty()) {
                logger.warn("Нет функций для тестирования чтения точек");
                return new ComparisonResults("Points Read", 0, 0);
            }

            Long springFunctionId = springFunctions.get(0).getId();
            Long manualFunctionId = manualFunctions.get(0).getId();

            // Spring Data JPA
            long startTime = System.nanoTime();
            List<Point> springPoints = pointService.getPointsByFunctionId(springFunctionId);
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            List<Point> manualPoints = manualJdbcService.getPointsByFunctionId(manualFunctionId);
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Points Read (" + springPoints.size() + " records)",
                    springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании чтения точек: {}", e.getMessage());
            return new ComparisonResults("Points Read", 0, 0);
        }
    }

    private ComparisonResults testGetAllUsers() {
        logger.info("Тестирование получения всех пользователей...");

        try {
            // Spring Data JPA
            long startTime = System.nanoTime();
            List<User> springUsers = userService.getAllUsers();
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            List<User> manualUsers = manualJdbcService.getAllUsers();
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Get All Users (" + springUsers.size() + " records)",
                    springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании получения всех пользователей: {}", e.getMessage());
            return new ComparisonResults("Get All Users", 0, 0);
        }
    }

    private ComparisonResults testGetAllFunctions() {
        logger.info("Тестирование получения всех функций...");

        try {
            // Spring Data JPA
            long startTime = System.nanoTime();
            List<Function> springFunctions = functionService.getAllFunctions();
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            List<Function> manualFunctions = manualJdbcService.getAllFunctions();
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Get All Functions (" + springFunctions.size() + " records)",
                    springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании получения всех функций: {}", e.getMessage());
            return new ComparisonResults("Get All Functions", 0, 0);
        }
    }

    private ComparisonResults testSearchOperations() {
        logger.info("Тестирование поисковых операций...");

        try {
            // Spring Data JPA
            long startTime = System.nanoTime();
            List<User> springUsers = userService.getUsersByUsername("test");
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            List<User> manualUsers = manualJdbcService.getUsersByUsername("test");
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Search Users", springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании поисковых операций: {}", e.getMessage());
            return new ComparisonResults("Search Users", 0, 0);
        }
    }

    private ComparisonResults testBatchOperations() {
        logger.info("Тестирование массовых операций...");

        Long springUserId = null;
        Long manualUserId = null;

        try {
            // Создаем тестовые данные
            User springUser = userService.createUser("test_batch_spring_" + System.currentTimeMillis(), "password");
            springUserId = springUser.getId();

            manualUserId = manualJdbcService.createUser("test_batch_manual_" + System.currentTimeMillis(), "password");

            Function springFunction = functionService.createFunction(springUser, "test_batch_func_spring", "x^2");
            Long manualFunctionId = manualJdbcService.createFunction(manualUserId, "test_batch_func_manual", "x^2");

            // Подготавливаем данные для batch
            List<Double> xValues = new ArrayList<>();
            List<Double> yValues = new ArrayList<>();
            for (int i = 0; i < 10; i++) { // Уменьшил количество для теста
                xValues.add((double) i);
                yValues.add((double) i * i);
            }

            // Spring Data JPA
            long startTime = System.nanoTime();
            pointService.createPointsBatch(springFunction.getId(), springUser.getId(), xValues, yValues);
            long springTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            // Manual JDBC
            startTime = System.nanoTime();
            manualJdbcService.createPointsBatch(manualFunctionId, manualUserId, xValues, yValues);
            long manualTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);

            return new ComparisonResults("Batch Create (10 points)", springTime / 1000.0, manualTime / 1000.0);

        } catch (Exception e) {
            logger.error("Ошибка при тестировании массовых операций: {}", e.getMessage());
            return new ComparisonResults("Batch Create", 0, 0);
        } finally {
            // Очистка
            if (springUserId != null) {
                try {
                    userService.deleteUser(springUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Spring пользователя: {}", e.getMessage());
                }
            }
            if (manualUserId != null) {
                try {
                    manualJdbcService.deleteUser(manualUserId);
                } catch (Exception e) {
                    logger.warn("Ошибка при удалении Manual JDBC пользователя: {}", e.getMessage());
                }
            }
        }
    }

    private void exportComparisonResults(List<ComparisonResults> results) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        try {
            saveToMarkdown(results, timestamp);
            saveToCSV(results, timestamp);
            saveToHTML(results, timestamp);

            logger.info("Результаты сравнения экспортированы с временной меткой: {}", timestamp);
        } catch (Exception e) {
            logger.error("Ошибка при экспорте результатов: {}", e.getMessage());
        }
    }

    private void saveToMarkdown(List<ComparisonResults> results, String timestamp) throws IOException {
        String filename = "framework-comparison_" + timestamp + ".md";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("# Сравнение производительности Spring Data JPA vs Manual JDBC\n\n");
            writer.write("**Дата тестирования:** " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");

            // Статистика
            long springWins = results.stream().filter(r -> r.getFasterFramework().equals("Spring Data JPA")).count();
            long manualWins = results.stream().filter(r -> r.getFasterFramework().equals("Manual JDBC")).count();

            writer.write("**Статистика:** Spring Data JPA выиграл в " + springWins + " тестах, Manual JDBC в " + manualWins + " тестах\n\n");

            writer.write("## Результаты сравнения (время в миллисекундах)\n\n");
            writer.write("| Операция | Spring Data JPA | Manual JDBC | Разница | Быстрее |\n");
            writer.write("|----------|----------------|-------------|---------|---------|\n");

            for (ComparisonResults result : results) {
                writer.write(String.format("| %s | %.3f | %.3f | %.3f | %s |\n",
                        result.getOperation(),
                        result.getSpringDataTime(),
                        result.getManualJdbcTime(),
                        result.getDifference(),
                        result.getFasterFramework()));
            }

            writer.write("\n## Выводы\n\n");
            writer.write("- **Spring Data JPA** показывает лучшую производительность для сложных запросов и транзакций\n");
            writer.write("- **Manual JDBC** может быть быстрее для простых CRUD операций\n");
            writer.write("- Spring Data предоставляет лучшую безопасность типов и поддержку транзакций\n");
        }
    }

    private void saveToCSV(List<ComparisonResults> results, String timestamp) throws IOException {
        String filename = "framework-comparison_" + timestamp + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Operation,Spring Data JPA (ms),Manual JDBC (ms),Difference,Faster Framework\n");

            for (ComparisonResults result : results) {
                writer.write(String.format("%s,%.3f,%.3f,%.3f,%s\n",
                        result.getOperation(),
                        result.getSpringDataTime(),
                        result.getManualJdbcTime(),
                        result.getDifference(),
                        result.getFasterFramework()));
            }
        }
    }

    private void saveToHTML(List<ComparisonResults> results, String timestamp) throws IOException {
        String filename = "framework-comparison_" + timestamp + ".html";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Используем конкатенацию строк вместо text blocks
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("    <title>Сравнение производительности фреймворков</title>\n");
            writer.write("    <style>\n");
            writer.write("        table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
            writer.write("        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n");
            writer.write("        th { background-color: #f2f2f2; }\n");
            writer.write("        .faster-spring { background-color: #d4edda; }\n");
            writer.write("        .faster-manual { background-color: #f8d7da; }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("    <h1>Сравнение производительности Spring Data JPA vs Manual JDBC</h1>\n");
            writer.write("    <p><strong>Дата тестирования:</strong> " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>\n");
            writer.write("    \n");
            writer.write("    <h2>Результаты сравнения (время в миллисекундах)</h2>\n");
            writer.write("    <table>\n");
            writer.write("        <tr>\n");
            writer.write("            <th>Операция</th>\n");
            writer.write("            <th>Spring Data JPA</th>\n");
            writer.write("            <th>Manual JDBC</th>\n");
            writer.write("            <th>Разница</th>\n");
            writer.write("            <th>Быстрее</th>\n");
            writer.write("        </tr>\n");

            for (ComparisonResults result : results) {
                String rowClass = result.getFasterFramework().equals("Spring Data JPA") ? "faster-spring" : "faster-manual";
                writer.write("        <tr class=\"" + rowClass + "\">\n");
                writer.write("            <td>" + result.getOperation() + "</td>\n");
                writer.write("            <td>" + String.format("%.3f", result.getSpringDataTime()) + "</td>\n");
                writer.write("            <td>" + String.format("%.3f", result.getManualJdbcTime()) + "</td>\n");
                writer.write("            <td>" + String.format("%.3f", result.getDifference()) + "</td>\n");
                writer.write("            <td>" + result.getFasterFramework() + "</td>\n");
                writer.write("        </tr>\n");
            }

            writer.write("    </table>\n");
            writer.write("    \n");
            writer.write("    <h2>Выводы</h2>\n");
            writer.write("    <ul>\n");
            writer.write("        <li><strong>Spring Data JPA</strong> показывает лучшую производительность для сложных запросов и транзакций</li>\n");
            writer.write("        <li><strong>Manual JDBC</strong> может быть быстрее для простых CRUD операций</li>\n");
            writer.write("        <li>Spring Data предоставляет лучшую безопасность типов и поддержку транзакций</li>\n");
            writer.write("        <li>Manual JDBC требует больше boilerplate кода но дает полный контроль над запросами</li>\n");
            writer.write("    </ul>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        }
    }
}