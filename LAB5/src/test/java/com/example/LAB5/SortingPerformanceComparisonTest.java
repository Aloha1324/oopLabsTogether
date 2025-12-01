package com.example.LAB5;

import com.example.LAB5.framework.Search.FrameworkSearchService;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.repository.UserRepository;
import com.example.LAB5.framework.repository.FunctionRepository;
import com.example.LAB5.framework.repository.PointRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@Transactional
public class SortingPerformanceComparisonTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private PointRepository pointRepository;

    private static final int DATASET_SIZE = 1000;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+04:00"));
    }

    private static class ComparisonResult {
        private final String operationName;
        private final String operationType;
        private final double frameworkTime;
        private final double manualTime;
        private final double difference;
        private final String fasterImplementation;
        private final double frameworkOpsPerSec;
        private final double manualOpsPerSec;
        private final int recordsProcessed;

        public ComparisonResult(String operationName, String operationType, double frameworkTime,
                                double manualTime, int records) {
            this.operationName = operationName;
            this.operationType = operationType;
            this.frameworkTime = frameworkTime;
            this.manualTime = manualTime;
            this.difference = Math.abs(frameworkTime - manualTime);
            this.fasterImplementation = manualTime == 0 ? "Manual" :
                    (frameworkTime < manualTime ? "Framework" : "Manual");

            // РЕАЛЬНЫЙ расчет операций в секунду - ТОЛЬКО математика, НИКАКИХ ограничений!
            if (frameworkTime > 0) {
                this.frameworkOpsPerSec = (records * 1000.0) / frameworkTime;
            } else {
                this.frameworkOpsPerSec = 0;
            }

            if (manualTime > 0) {
                this.manualOpsPerSec = (records * 1000.0) / manualTime;
            } else {
                this.manualOpsPerSec = 0;
            }

            this.recordsProcessed = records;

            // Отладка для проверки расчетов
            if (frameworkTime > 0) {
                System.out.printf("DEBUG: %s -> %d records / %.2f ms = %.0f ops/sec%n",
                        operationName, records, frameworkTime, this.frameworkOpsPerSec);
            }
        }

        public String getOperationName() { return operationName; }
        public String getOperationType() { return operationType; }
        public double getFrameworkTime() { return frameworkTime; }
        public double getManualTime() { return manualTime; }
        public double getDifference() { return difference; }
        public String getFasterImplementation() { return fasterImplementation; }
        public double getFrameworkOpsPerSec() { return frameworkOpsPerSec; }
        public double getManualOpsPerSec() { return manualOpsPerSec; }
        public int getRecordsProcessed() { return recordsProcessed; }
    }

    public static class ManualCollectionService {
        public List<User> sortUsersByLogin(List<User> users, String order) {
            List<User> sorted = new ArrayList<>(users);

            if ("asc".equalsIgnoreCase(order)) {
                sorted.sort(Comparator.comparing(User::getUsername));
            } else {
                sorted.sort(Comparator.comparing(User::getUsername).reversed());
            }
            return sorted;
        }

        public List<User> sortUsersByRole(List<User> users, String order) {
            List<User> sorted = new ArrayList<>(users);

            if ("asc".equalsIgnoreCase(order)) {
                sorted.sort(Comparator.comparing(User::getPasswordHash));
            } else {
                sorted.sort(Comparator.comparing(User::getPasswordHash).reversed());
            }
            return sorted;
        }

        public List<Function> sortFunctionsByName(List<Function> functions, String order) {
            List<Function> sorted = new ArrayList<>(functions);

            if ("asc".equalsIgnoreCase(order)) {
                sorted.sort(Comparator.comparing(Function::getName));
            } else {
                sorted.sort(Comparator.comparing(Function::getName).reversed());
            }
            return sorted;
        }

        public List<Point> sortPointsByX(List<Point> points, String order) {
            List<Point> sorted = new ArrayList<>(points);

            if ("asc".equalsIgnoreCase(order)) {
                sorted.sort(Comparator.comparing(Point::getXValue));
            } else {
                sorted.sort(Comparator.comparing(Point::getXValue).reversed());
            }
            return sorted;
        }

        public List<Point> sortPointsByY(List<Point> points, String order) {
            List<Point> sorted = new ArrayList<>(points);

            if ("asc".equalsIgnoreCase(order)) {
                sorted.sort(Comparator.comparing(Point::getYValue));
            } else {
                sorted.sort(Comparator.comparing(Point::getYValue).reversed());
            }
            return sorted;
        }

        public List<User> searchUsersByLoginPattern(List<User> users, String pattern) {
            List<User> result = new ArrayList<>();

            for (User user : users) {
                if (user.getUsername().contains(pattern)) {
                    result.add(user);
                }
            }
            return result;
        }

        public List<Function> searchFunctionsByNamePattern(List<Function> functions, String pattern) {
            List<Function> result = new ArrayList<>();

            for (Function function : functions) {
                if (function.getName().contains(pattern)) {
                    result.add(function);
                }
            }
            return result;
        }

        public List<Point> filterPointsByX(List<Point> points, double minX) {
            List<Point> result = new ArrayList<>();

            for (Point point : points) {
                if (point.getXValue() > minX) {
                    result.add(point);
                }
            }
            return result;
        }

        public List<User> sortUsersByMultipleFields(List<User> users, Map<String, String> sortFields) {
            List<User> sorted = new ArrayList<>(users);

            Comparator<User> comparator = null;
            for (Map.Entry<String, String> entry : sortFields.entrySet()) {
                String field = entry.getKey();
                String order = entry.getValue();

                Comparator<User> fieldComparator;
                switch (field.toLowerCase()) {
                    case "login":
                        fieldComparator = Comparator.comparing(User::getUsername);
                        break;
                    case "role":
                        fieldComparator = Comparator.comparing(User::getPasswordHash);
                        break;
                    default:
                        fieldComparator = Comparator.comparing(User::getUsername);
                }

                if ("desc".equalsIgnoreCase(order)) {
                    fieldComparator = fieldComparator.reversed();
                }

                if (comparator == null) {
                    comparator = fieldComparator;
                } else {
                    comparator = comparator.thenComparing(fieldComparator);
                }
            }

            if (comparator != null) {
                sorted.sort(comparator);
            }
            return sorted;
        }
    }

    @Test
    void compareAllSortingPerformance() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("ТЕСТ СРАВНЕНИЯ ПРОИЗВОДИТЕЛЬНОСТИ СОРТИРОВОК: FRAMEWORK vs MANUAL");
        System.out.println("=".repeat(100));

        try {
            createRealisticTestData();

            List<ComparisonResult> comparisonResults = new ArrayList<>();

            comparisonResults.addAll(testSortingOperations());
            comparisonResults.addAll(testSearchOperations());
            comparisonResults.addAll(testComplexOperations());

            printComparisonTable(comparisonResults);
            saveComparisonTableToFile(comparisonResults);
            printFinalAnalysis(comparisonResults);

            System.out.println("=".repeat(100));
            System.out.println("ТЕСТИРОВАНИЕ ЗАВЕРШЕНО");
            System.out.println("=".repeat(100));

        } catch (Exception e) {
            System.err.println("Error during performance test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<ComparisonResult> testSortingOperations() {
        List<ComparisonResult> results = new ArrayList<>();
        ManualCollectionService manualService = new ManualCollectionService();
        FrameworkSearchService frameworkService = new FrameworkSearchService(userRepository, functionRepository, pointRepository);

        List<User> users = userRepository.findAll();
        List<Function> functions = functionRepository.findAll();
        List<Point> points = pointRepository.findAll();

        System.out.println("\n🔍 Тестирование с: " + users.size() + " пользователей, "
                + functions.size() + " функций, " + points.size() + " точек");

        // 1. Сортировка пользователей по логину (ASC)
        double frameworkTime = measureTimeAccurate(() ->
                frameworkService.sortUsers(new ArrayList<>(users), "login", "asc"), 5, 10);
        double manualTime = measureTimeAccurate(() ->
                manualService.sortUsersByLogin(new ArrayList<>(users), "asc"), 5, 10);
        results.add(new ComparisonResult(
                "Сортировка пользователей по логину (ASC)", "SORTING",
                frameworkTime, manualTime, users.size()
        ));

        // 2. Сортировка пользователей по логину (DESC)
        frameworkTime = measureTimeAccurate(() ->
                frameworkService.sortUsers(new ArrayList<>(users), "login", "desc"), 5, 10);
        manualTime = measureTimeAccurate(() ->
                manualService.sortUsersByLogin(new ArrayList<>(users), "desc"), 5, 10);
        results.add(new ComparisonResult(
                "Сортировка пользователей по логину (DESC)", "SORTING",
                frameworkTime, manualTime, users.size()
        ));

        // 3. Сортировка пользователей по роли (ASC)
        frameworkTime = measureTimeAccurate(() ->
                frameworkService.sortUsers(new ArrayList<>(users), "role", "asc"), 5, 10);
        manualTime = measureTimeAccurate(() ->
                manualService.sortUsersByRole(new ArrayList<>(users), "asc"), 5, 10);
        results.add(new ComparisonResult(
                "Сортировка пользователей по роли (ASC)", "SORTING",
                frameworkTime, manualTime, users.size()
        ));

        // 4. Сортировка функций по имени (ASC)
        frameworkTime = measureTimeAccurate(() ->
                frameworkService.sortFunctions(new ArrayList<>(functions), "name", "asc"), 5, 10);
        manualTime = measureTimeAccurate(() ->
                manualService.sortFunctionsByName(new ArrayList<>(functions), "asc"), 5, 10);
        results.add(new ComparisonResult(
                "Сортировка функций по имени (ASC)", "SORTING",
                frameworkTime, manualTime, functions.size()
        ));

        // 5. Сортировка функций по имени (DESC)
        frameworkTime = measureTimeAccurate(() ->
                frameworkService.sortFunctions(new ArrayList<>(functions), "name", "desc"), 5, 10);
        manualTime = measureTimeAccurate(() ->
                manualService.sortFunctionsByName(new ArrayList<>(functions), "desc"), 5, 10);
        results.add(new ComparisonResult(
                "Сортировка функций по имени (DESC)", "SORTING",
                frameworkTime, manualTime, functions.size()
        ));

        // 6. Сортировка точек по X (ASC)
        frameworkTime = measureTimeAccurate(() ->
                frameworkService.sortPoints(new ArrayList<>(points), "x", "asc"), 5, 10);
        manualTime = measureTimeAccurate(() ->
                manualService.sortPointsByX(new ArrayList<>(points), "asc"), 5, 10);
        results.add(new ComparisonResult(
                "Сортировка точек по X (ASC)", "SORTING",
                frameworkTime, manualTime, points.size()
        ));

        // 7. Сортировка точек по Y (ASC)
        frameworkTime = measureTimeAccurate(() ->
                frameworkService.sortPoints(new ArrayList<>(points), "y", "asc"), 5, 10);
        manualTime = measureTimeAccurate(() ->
                manualService.sortPointsByY(new ArrayList<>(points), "asc"), 5, 10);
        results.add(new ComparisonResult(
                "Сортировка точек по Y (ASC)", "SORTING",
                frameworkTime, manualTime, points.size()
        ));

        return results;
    }

    private List<ComparisonResult> testSearchOperations() {
        List<ComparisonResult> results = new ArrayList<>();
        ManualCollectionService manualService = new ManualCollectionService();

        List<User> users = userRepository.findAll();
        List<Function> functions = functionRepository.findAll();
        List<Point> points = pointRepository.findAll();

        // 8. Поиск пользователей по логину
        double frameworkTime = measureTimeAccurate(() -> {
            List<User> result = userRepository.findByUsernameContaining("user");
            // Гарантируем, что результат используется
            if (!result.isEmpty()) {
                result.get(0).getUsername();
            }
        }, 3, 5);

        double manualTime = measureTimeAccurate(() -> {
            List<User> result = manualService.searchUsersByLoginPattern(new ArrayList<>(users), "user");
            if (!result.isEmpty()) {
                result.get(0).getUsername();
            }
        }, 3, 5);

        results.add(new ComparisonResult(
                "Поиск пользователей по логину", "SEARCH",
                frameworkTime, manualTime, users.size()
        ));

        // 9. Поиск функций по имени
        frameworkTime = measureTimeAccurate(() -> {
            List<Function> result = functionRepository.findByNameContaining("sin");
            if (!result.isEmpty()) {
                result.get(0).getName();
            }
        }, 3, 5);

        manualTime = measureTimeAccurate(() -> {
            List<Function> result = manualService.searchFunctionsByNamePattern(new ArrayList<>(functions), "sin");
            if (!result.isEmpty()) {
                result.get(0).getName();
            }
        }, 3, 5);

        results.add(new ComparisonResult(
                "Поиск функций по имени", "SEARCH",
                frameworkTime, manualTime, functions.size()
        ));

        // 10. Фильтрация точек (X > 50)
        frameworkTime = measureTimeAccurate(() -> {
            List<Point> result = pointRepository.findByXValueGreaterThan(50.0);
            if (!result.isEmpty()) {
                result.get(0).getXValue();
            }
        }, 3, 5);

        manualTime = measureTimeAccurate(() -> {
            List<Point> result = manualService.filterPointsByX(new ArrayList<>(points), 50.0);
            if (!result.isEmpty()) {
                result.get(0).getXValue();
            }
        }, 3, 5);

        results.add(new ComparisonResult(
                "Фильтрация точек (X > 50)", "FILTER",
                frameworkTime, manualTime, points.size()
        ));

        return results;
    }

    private List<ComparisonResult> testComplexOperations() {
        List<ComparisonResult> results = new ArrayList<>();
        ManualCollectionService manualService = new ManualCollectionService();
        FrameworkSearchService frameworkService = new FrameworkSearchService(userRepository, functionRepository, pointRepository);

        List<User> users = userRepository.findAll();

        // 11. Множественная сортировка (роль+логин)
        Map<String, String> multiSort = new LinkedHashMap<>();
        multiSort.put("role", "asc");
        multiSort.put("login", "asc");

        double frameworkTime = measureTimeAccurate(() -> {
            // Сначала сортируем по роли
            List<User> sortedByRole = frameworkService.sortUsers(new ArrayList<>(users), "role", "asc");
            // Затем по логину (имитация множественной сортировки)
            frameworkService.sortUsers(sortedByRole, "login", "asc");
        }, 3, 5);

        double manualTime = measureTimeAccurate(() -> {
            manualService.sortUsersByMultipleFields(new ArrayList<>(users), multiSort);
        }, 3, 5);

        results.add(new ComparisonResult(
                "Множественная сортировка (роль+логин)", "COMPLEX",
                frameworkTime, manualTime, users.size()
        ));

        // 12. Подсчет пользователей
        double countTime = measureTimeAccurate(() -> {
            long count = userRepository.count();
            // Гарантируем использование результата
            if (count > 0) {
                // Ничего не делаем, просто "используем" результат
            }
        }, 3, 5);

        // Для подсчета Manual время 0, так как это встроенная операция
        results.add(new ComparisonResult(
                "Подсчет пользователей", "AGGREGATE",
                countTime, 0.0, (int)userRepository.count()
        ));

        return results;
    }

    private double measureTimeAccurate(Runnable operation, int warmupIterations, int measurementIterations) {
        try {
            // Прогрев JVM
            for (int i = 0; i < warmupIterations; i++) {
                operation.run();
            }

            // Даем JVM стабилизироваться
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Список для хранения измерений
            List<Long> measurements = new ArrayList<>();

            // Многократные измерения
            for (int i = 0; i < measurementIterations; i++) {
                long startTime = System.nanoTime();
                operation.run();
                long endTime = System.nanoTime();

                long duration = endTime - startTime;
                measurements.add(duration);

                // Небольшая пауза между измерениями
                if (i < measurementIterations - 1) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (measurements.isEmpty()) {
                return 0.0;
            }

            // Сортируем и берем медиану (устойчивая к выбросам)
            measurements.sort(Long::compareTo);
            long medianNanos = measurements.get(measurements.size() / 2);

            // Конвертируем в миллисекунды
            return medianNanos / 1_000_000.0;

        } catch (Exception e) {
            System.err.println("Error measuring time: " + e.getMessage());
            return -1.0;
        }
    }

    private void printComparisonTable(List<ComparisonResult> results) {
        System.out.println("\n📊 ТАБЛИЦА СРАВНЕНИЯ ПРОИЗВОДИТЕЛЬНОСТИ СОРТИРОВОК:");
        System.out.println("=".repeat(140));
        System.out.printf("| %-40s | %-10s | %-12s | %-12s | %-10s | %-8s | %-15s | %-15s | %-8s |%n",
                "Операция", "Тип", "Framework(мс)", "Manual(мс)", "Разница(мс)", "Быстрее", "Оп/сек Framework", "Оп/сек Manual", "Записей");
        System.out.println("|" + "-".repeat(42) + "|" + "-".repeat(12) + "|" + "-".repeat(14) + "|" +
                "-".repeat(14) + "|" + "-".repeat(12) + "|" + "-".repeat(10) + "|" +
                "-".repeat(17) + "|" + "-".repeat(17) + "|" + "-".repeat(10) + "|");

        for (ComparisonResult result : results) {
            System.out.printf("| %-40s | %-10s | %12.2f | %12.2f | %10.2f | %-8s | %15.0f | %15.0f | %8d |%n",
                    result.getOperationName(),
                    result.getOperationType(),
                    result.getFrameworkTime(),
                    result.getManualTime(),
                    result.getDifference(),
                    result.getFasterImplementation(),
                    result.getFrameworkOpsPerSec(),
                    result.getManualOpsPerSec(),
                    result.getRecordsProcessed());
        }
        System.out.println("=".repeat(140));

        long frameworkWins = 0;
        long manualWins = 0;
        long draws = 0;

        for (ComparisonResult result : results) {
            if (result.getManualTime() > 0 && result.getFrameworkTime() > 0) {
                if (result.getFasterImplementation().equals("Framework")) {
                    frameworkWins++;
                } else if (result.getFasterImplementation().equals("Manual")) {
                    manualWins++;
                }

                if (Math.abs(result.getFrameworkTime() - result.getManualTime()) < 0.1) {
                    draws++;
                }
            }
        }

        System.out.printf("%n📈 СТАТИСТИКА: Framework выиграл в %d тестах, Manual в %d тестах, ничья в %d тестах%n",
                frameworkWins, manualWins, draws);
    }

    private void saveComparisonTableToFile(List<ComparisonResult> results) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String filename = "sorting-performance-comparison-" + timestamp + ".md";

            StringBuilder table = new StringBuilder();
            table.append("# ТАБЛИЦА СРАВНЕНИЯ ПРОИЗВОДИТЕЛЬНОСТИ СОРТИРОВОК: FRAMEWORK vs MANUAL\n\n");
            table.append("**Дата тестирования:** ").append(DATE_FORMAT.format(new Date())).append("\n");
            table.append("**Размер набора данных:** ").append(DATASET_SIZE).append(" записей\n");
            table.append("**Платформа:** Java ").append(System.getProperty("java.version")).append("\n");
            table.append("**Итерации измерений:** 10 прогрев + 5 измерений\n\n");

            table.append("| Операция | Тип | Framework (мс) | Manual (мс) | Разница (мс) | Быстрее | Оп/сек Framework | Оп/сек Manual | Записей |\n");
            table.append("|----------|-----|----------------|-------------|--------------|---------|-----------------|---------------|---------|\n");

            for (ComparisonResult result : results) {
                table.append(String.format("| %s | %s | %.2f | %.2f | %.2f | %s | %.0f | %.0f | %d |\n",
                        result.getOperationName(),
                        result.getOperationType(),
                        result.getFrameworkTime(),
                        result.getManualTime(),
                        result.getDifference(),
                        result.getFasterImplementation(),
                        result.getFrameworkOpsPerSec(),
                        result.getManualOpsPerSec(),
                        result.getRecordsProcessed()));
            }

            Files.write(Paths.get(filename), table.toString().getBytes());
            System.out.println("📄 Таблица сравнения сохранена в файл: " + filename);

        } catch (Exception e) {
            System.err.println("Ошибка при сохранении таблицы сравнения: " + e.getMessage());
        }
    }

    private void printFinalAnalysis(List<ComparisonResult> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ИТОГОВЫЙ АНАЛИЗ ПРОИЗВОДИТЕЛЬНОСТИ");
        System.out.println("=".repeat(80));

        List<ComparisonResult> validResults = new ArrayList<>();
        for (ComparisonResult result : results) {
            if (result.getManualTime() > 0 && result.getFrameworkTime() > 0) {
                validResults.add(result);
            }
        }

        if (validResults.isEmpty()) {
            System.out.println("Нет валидных результатов для анализа");
            return;
        }

        System.out.printf("Проанализировано %d тестов производительности%n", validResults.size());

        // Собираем статистику по типам операций
        Map<String, List<ComparisonResult>> resultsByType = new HashMap<>();
        for (ComparisonResult result : validResults) {
            String type = result.getOperationType();
            List<ComparisonResult> typeList = resultsByType.get(type);
            if (typeList == null) {
                typeList = new ArrayList<>();
                resultsByType.put(type, typeList);
            }
            typeList.add(result);
        }

        for (Map.Entry<String, List<ComparisonResult>> entry : resultsByType.entrySet()) {
            String type = entry.getKey();
            List<ComparisonResult> typeResults = entry.getValue();

            double sumFrameworkTime = 0;
            double sumManualTime = 0;
            double sumFrameworkOps = 0;
            double sumManualOps = 0;

            for (ComparisonResult result : typeResults) {
                sumFrameworkTime += result.getFrameworkTime();
                sumManualTime += result.getManualTime();
                sumFrameworkOps += result.getFrameworkOpsPerSec();
                sumManualOps += result.getManualOpsPerSec();
            }

            double avgFrameworkTime = sumFrameworkTime / typeResults.size();
            double avgManualTime = sumManualTime / typeResults.size();
            double avgFrameworkOps = sumFrameworkOps / typeResults.size();
            double avgManualOps = sumManualOps / typeResults.size();

            System.out.printf("%n📈 %s (тестов: %d):%n", type, typeResults.size());
            System.out.printf("   • Среднее время Framework: %.2f мс (%.0f оп/сек)%n",
                    avgFrameworkTime, avgFrameworkOps);
            System.out.printf("   • Среднее время Manual: %.2f мс (%.0f оп/сек)%n",
                    avgManualTime, avgManualOps);

            if (avgManualTime > 0) {
                double ratio = avgFrameworkTime / avgManualTime;
                System.out.printf("   • Соотношение Framework/Manual: %.2fx%n", ratio);

                if (ratio < 1.0) {
                    System.out.printf("   • Вывод: Framework быстрее на %.1f%%%n", (1 - ratio) * 100);
                } else {
                    System.out.printf("   • Вывод: Manual быстрее на %.1f%%%n", (ratio - 1) * 100);
                }
            }
        }

        // Общие рекомендации
        System.out.println("\n💡 РЕКОМЕНДАЦИИ:");
        System.out.println("1. Для сортировок используйте встроенные Collections.sort()");
        System.out.println("2. Для поиска простых паттернов используйте ручную реализацию");
        System.out.println("3. Для сложных запросов используйте Framework (JPA/Hibernate)");
        System.out.println("4. Для больших данных (>10,000 записей) используйте пагинацию");
        System.out.println("\n⚠️  ПРИМЕЧАНИЕ: Реальные показатели оп/сек могут быть ЛЮБЫМИ,");
        System.out.println("   в зависимости от производительности вашей системы");
        System.out.println("   Пример: если сортировка 1000 записей занимает 2 мс,");
        System.out.println("   то производительность = (1000 / 0.002) = 500,000 оп/сек");
    }

    private void createRealisticTestData() {
        System.out.println("Создание тестовых данных...");

        try {
            userRepository.deleteAll();
            functionRepository.deleteAll();
            pointRepository.deleteAll();

            List<User> users = new ArrayList<>();
            List<Function> functions = new ArrayList<>();
            List<Point> points = new ArrayList<>();

            Random random = new Random();
            String[] roles = {"ADMIN", "USER", "MODERATOR", "GUEST", "EDITOR"};
            String[] functionNames = {"sin(x)", "cos(x)", "tan(x)", "log(x)", "exp(x)", "sqrt(x)", "x^2", "x^3"};

            // Создаем пользователей
            for (int i = 0; i < DATASET_SIZE; i++) {
                User user = new User();
                user.setUsername("user_" + i + "_" + String.format("%05d", random.nextInt(100000)));
                user.setPasswordHash(roles[random.nextInt(roles.length)]);
                users.add(user);
            }
            userRepository.saveAll(users);

            // Создаем функции для каждого пользователя
            for (User user : users) {
                for (int j = 0; j < 2; j++) {
                    Function function = new Function();
                    function.setName(functionNames[random.nextInt(functionNames.length)] + "_" + user.getId() + "_" + j);
                    function.setExpression("Math." + function.getName().split("\\(")[0] + "(x)");
                    function.setUser(user);
                    functions.add(function);
                }
            }
            functionRepository.saveAll(functions);

            // Создаем точки для каждой функции
            for (Function function : functions) {
                for (int k = 0; k < 3; k++) {
                    Point point = new Point();
                    point.setXValue(random.nextDouble() * 100);
                    point.setYValue(random.nextDouble() * 100);
                    point.setFunction(function);
                    point.setUser(function.getUser());
                    points.add(point);
                }
            }
            pointRepository.saveAll(points);

            System.out.println("✅ Тестовые данные созданы:");
            System.out.println("   • Пользователей: " + users.size());
            System.out.println("   • Функций: " + functions.size());
            System.out.println("   • Точек: " + points.size());

        } catch (Exception e) {
            System.err.println("Ошибка при создании тестовых данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
}