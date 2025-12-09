package com.example.LAB5.framework.service;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.repository.FunctionRepository;
import com.example.LAB5.framework.repository.PointRepository;
import com.example.LAB5.framework.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class PointService {
    private static final Logger logger = LoggerFactory.getLogger(PointService.class);

    private final PointRepository pointRepository;
    private final FunctionRepository functionRepository;
    private final UserRepository userRepository;

    // Конструкторное внедрение зависимостей
    @Autowired
    public PointService(PointRepository pointRepository, FunctionRepository functionRepository, UserRepository userRepository) {
        this.pointRepository = pointRepository;
        this.functionRepository = functionRepository;
        this.userRepository = userRepository;
    }

    // Класс для измерения производительности
    public static class PerformanceMetrics {
        private final String operationName;
        private final long executionTimeMs;
        private final int recordsProcessed;
        private final String frameworkType;

        public PerformanceMetrics(String operationName, long executionTimeMs, int recordsProcessed, String frameworkType) {
            this.operationName = operationName;
            this.executionTimeMs = executionTimeMs;
            this.recordsProcessed = recordsProcessed;
            this.frameworkType = frameworkType;
        }

        // Геттеры
        public String getOperationName() { return operationName; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public int getRecordsProcessed() { return recordsProcessed; }
        public String getFrameworkType() { return frameworkType; }

        public double getRecordsPerSecond() {
            return executionTimeMs > 0 ? (recordsProcessed * 1000.0) / executionTimeMs : 0;
        }

        @Override
        public String toString() {
            return String.format(
                    "PerformanceMetrics{operation='%s', time=%dms, records=%d, records/sec=%.2f, framework=%s}",
                    operationName, executionTimeMs, recordsProcessed, getRecordsPerSecond(), frameworkType
            );
        }
    }

    private final List<PerformanceMetrics> performanceMetrics = new ArrayList<>();

    public Point createPoint(Long functionId, Long userId, Double xValue, Double yValue) {
        long startTime = System.nanoTime();

        logger.info("Создание точки: function={}, user={}, x={}, y={}", functionId, userId, xValue, yValue);

        Optional<Function> function = functionRepository.findById(functionId);
        if (!function.isPresent()) {
            logger.error("Функция с ID {} не существует", functionId);
            throw new IllegalArgumentException("Function with ID " + functionId + " does not exist");
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("Пользователь с ID {} не существует", userId);
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        Point point = new Point(xValue, yValue, function.get(), user.get());
        Point savedPoint = pointRepository.save(point);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("CREATE_POINT", durationMs, 1, "SPRING_DATA_JPA"));

        logger.debug("Создана точка с ID: {} за {} мс", savedPoint.getId(), durationMs);
        return savedPoint;
    }

    // Пакетное создание точек
    public int createPointsBatch(Long functionId, Long userId, List<Double> xValues, List<Double> yValues) {
        long startTime = System.nanoTime();
        logger.info("Пакетное создание точек для функции {} пользователя {}: {} точек",
                functionId, userId, xValues.size());

        if (xValues.size() != yValues.size()) {
            logger.error("Количество X и Y значений не совпадает: {} vs {}", xValues.size(), yValues.size());
            throw new IllegalArgumentException("X and Y values count must be equal");
        }

        Optional<Function> function = functionRepository.findById(functionId);
        if (!function.isPresent()) {
            logger.error("Функция с ID {} не существует", functionId);
            throw new IllegalArgumentException("Function with ID " + functionId + " does not exist");
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("Пользователь с ID {} не существует", userId);
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            points.add(new Point(xValues.get(i), yValues.get(i), function.get(), user.get()));
        }

        pointRepository.saveAll(points);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("BATCH_CREATE_POINTS", durationMs, points.size(), "SPRING_DATA_JPA"));

        logger.info("Создано {} точек для функции {} за {} мс", points.size(), functionId, durationMs);
        return points.size();
    }

    public int generateFunctionPoints(Long functionId, Long userId, String functionType, double start, double end, double step) {
        long startTime = System.nanoTime();

        logger.info("Генерация точек для функции {} пользователя {}: type={}, range=[{}, {}], step={}",
                functionId, userId, functionType, start, end, step);

        Optional<Function> function = functionRepository.findById(functionId);
        if (!function.isPresent()) {
            logger.error("Функция с ID {} не существует", functionId);
            return 0;
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            logger.error("Пользователь с ID {} не существует", userId);
            return 0;
        }

        List<Point> points = new ArrayList<>();
        int pointCount = 0;

        for (double x = start; x <= end; x += step) {
            double y = calculateFunction(functionType, x);
            points.add(new Point(x, y, function.get(), user.get()));
            pointCount++;
        }

        if (!points.isEmpty()) {
            pointRepository.saveAll(points);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("GENERATE_FUNCTION_POINTS", durationMs, pointCount, "SPRING_DATA_JPA"));

            logger.info("Сгенерировано {} точек для функции {} за {} мс", pointCount, functionId, durationMs);
        }

        return pointCount;
    }

    private double calculateFunction(String functionType, double x) {
        String type = functionType.toLowerCase();

        if ("linear".equals(type)) {
            return x;
        } else if ("quadratic".equals(type)) {
            return x * x;
        } else if ("cubic".equals(type)) {
            return x * x * x;
        } else if ("sin".equals(type)) {
            return Math.sin(x);
        } else if ("cos".equals(type)) {
            return Math.cos(x);
        } else if ("exp".equals(type)) {
            return Math.exp(x);
        } else if ("log".equals(type)) {
            return Math.log(Math.abs(x) + 1e-10); // избегаем log(0)
        } else {
            return x; // значение по умолчанию
        }
    }

    public Optional<Point> getPointById(Long id) {
        long startTime = System.nanoTime();
        logger.debug("Поиск точки по ID: {}", id);

        Optional<Point> result = pointRepository.findById(id);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_POINT_BY_ID", durationMs, result.isPresent() ? 1 : 0, "SPRING_DATA_JPA"));

        return result;
    }

    public List<Point> getPointsByFunctionId(Long functionId) {
        long startTime = System.nanoTime();
        logger.debug("Поиск точек функции с ID: {}", functionId);

        List<Point> result = pointRepository.findByFunctionId(functionId);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_POINTS_BY_FUNCTION", durationMs, result.size(), "SPRING_DATA_JPA"));

        return result;
    }

    public List<Point> getPointsByUserId(Long userId) {
        long startTime = System.nanoTime();
        logger.debug("Поиск точек пользователя с ID: {}", userId);

        List<Point> result = pointRepository.findByUserId(userId);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_POINTS_BY_USER", durationMs, result.size(), "SPRING_DATA_JPA"));

        return result;
    }

    public List<Point> getAllPoints() {
        long startTime = System.nanoTime();
        logger.debug("Получение всех точек");

        List<Point> result = pointRepository.findAll();

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_ALL_POINTS", durationMs, result.size(), "SPRING_DATA_JPA"));

        return result;
    }

    // Дополнительные методы для тестирования производительности

    public List<Point> getPointsByXRange(Long functionId, Double minX, Double maxX) {
        long startTime = System.nanoTime();
        logger.debug("Поиск точек функции {} в диапазоне x=[{}, {}]", functionId, minX, maxX);

        List<Point> result = pointRepository.findByFunctionIdAndXValueBetween(functionId, minX, maxX);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_POINTS_BY_X_RANGE", durationMs, result.size(), "SPRING_DATA_JPA"));

        return result;
    }

    public List<Point> getPointsByYRange(Long functionId, Double minY, Double maxY) {
        long startTime = System.nanoTime();
        logger.debug("Поиск точек функции {} в диапазоне y=[{}, {}]", functionId, minY, maxY);

        List<Point> result = pointRepository.findByFunctionIdAndYValueBetween(functionId, minY, maxY);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_POINTS_BY_Y_RANGE", durationMs, result.size(), "SPRING_DATA_JPA"));

        return result;
    }

    public boolean updatePoint(Long pointId, Double xValue, Double yValue) {
        long startTime = System.nanoTime();
        logger.info("Обновление точки с ID: {}", pointId);

        Optional<Point> existingPoint = pointRepository.findById(pointId);
        if (existingPoint.isPresent()) {
            Point point = existingPoint.get();
            point.setXValue(xValue);
            point.setYValue(yValue);

            pointRepository.save(point);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("UPDATE_POINT", durationMs, 1, "SPRING_DATA_JPA"));

            logger.info("Точка с ID {} успешно обновлена за {} мс", pointId, durationMs);
            return true;
        }

        logger.warn("Точка с ID {} не найдена для обновления", pointId);
        return false;
    }

    public boolean deletePoint(Long pointId) {
        long startTime = System.nanoTime();
        logger.info("Удаление точки с ID: {}", pointId);

        if (pointRepository.existsById(pointId)) {
            pointRepository.deleteById(pointId);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("DELETE_POINT", durationMs, 1, "SPRING_DATA_JPA"));

            logger.info("Точка с ID {} удалена за {} мс", pointId, durationMs);
            return true;
        }

        logger.warn("Точка с ID {} не найдена для удаления", pointId);
        return false;
    }

    public int deletePointsByFunctionId(Long functionId) {
        long startTime = System.nanoTime();
        logger.info("Удаление всех точек функции с ID: {}", functionId);

        // Получаем количество точек до удаления для метрик
        int pointsCount = pointRepository.findByFunctionId(functionId).size();

        pointRepository.deleteByFunctionId(functionId);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("DELETE_POINTS_BY_FUNCTION", durationMs, pointsCount, "SPRING_DATA_JPA"));

        logger.info("Удалено {} точек функции {} за {} мс", pointsCount, functionId, durationMs);
        return pointsCount;
    }

    public int deletePointsByUserId(Long userId) {
        long startTime = System.nanoTime();
        logger.info("Удаление всех точек пользователя с ID: {}", userId);

        // Получаем количество точек до удаления для метрик
        int pointsCount = pointRepository.findByUserId(userId).size();

        pointRepository.deleteByUserId(userId);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("DELETE_POINTS_BY_USER", durationMs, pointsCount, "SPRING_DATA_JPA"));

        logger.info("Удалено {} точек пользователя {} за {} мс", pointsCount, userId, durationMs);
        return pointsCount;
    }

    // Методы для работы с метриками производительности
    public List<PerformanceMetrics> getPerformanceMetrics() {
        return new ArrayList<>(performanceMetrics);
    }

    public void clearPerformanceMetrics() {
        performanceMetrics.clear();
        logger.info("Метрики производительности очищены");
    }

    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== POINT SERVICE PERFORMANCE REPORT (SPRING DATA JPA) ===\n");

        for (PerformanceMetrics metric : performanceMetrics) {
            report.append(metric.toString()).append("\n");
        }

        // Сводная статистика
        report.append("\n=== SUMMARY ===\n");
        report.append(String.format("Total operations: %d\n", performanceMetrics.size()));
        report.append(String.format("Total points processed: %d\n",
                performanceMetrics.stream().mapToInt(PerformanceMetrics::getRecordsProcessed).sum()));
        report.append(String.format("Total execution time: %d ms\n",
                performanceMetrics.stream().mapToLong(PerformanceMetrics::getExecutionTimeMs).sum()));

        return report.toString();
    }

    // Дополнительные методы для тестирования

    public Point findMaxYPoint(Long functionId) {
        long startTime = System.nanoTime();
        logger.debug("Поиск точки с максимальным Y для функции {}", functionId);

        List<Point> points = pointRepository.findByFunctionId(functionId);
        Point maxPoint = null;
        double maxY = Double.MIN_VALUE;

        for (Point point : points) {
            if (point.getYValue() > maxY) {
                maxY = point.getYValue();
                maxPoint = point;
            }
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("FIND_MAX_Y_POINT", durationMs, points.size(), "SPRING_DATA_JPA"));

        return maxPoint;
    }

    public Point findMinYPoint(Long functionId) {
        long startTime = System.nanoTime();
        logger.debug("Поиск точки с минимальным Y для функции {}", functionId);

        List<Point> points = pointRepository.findByFunctionId(functionId);
        Point minPoint = null;
        double minY = Double.MAX_VALUE;

        for (Point point : points) {
            if (point.getYValue() < minY) {
                minY = point.getYValue();
                minPoint = point;
            }
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("FIND_MIN_Y_POINT", durationMs, points.size(), "SPRING_DATA_JPA"));

        return minPoint;
    }

    public List<Point> findRoots(Long functionId, double tolerance) {
        long startTime = System.nanoTime();
        logger.debug("Поиск корней функции {} с точностью {}", functionId, tolerance);

        List<Point> points = pointRepository.findByFunctionId(functionId);
        List<Point> roots = new ArrayList<>();

        for (Point point : points) {
            if (Math.abs(point.getYValue()) <= tolerance) {
                roots.add(point);
            }
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("FIND_ROOTS", durationMs, points.size(), "SPRING_DATA_JPA"));

        logger.debug("Найдено {} корней функции {}", roots.size(), functionId);
        return roots;
    }

    public PointStatistics getPointStatistics(Long functionId) {
        long startTime = System.nanoTime();
        logger.debug("Получение статистики точек для функции {}", functionId);

        List<Point> points = pointRepository.findByFunctionId(functionId);
        if (points.isEmpty()) {
            logger.warn("Нет точек для функции {}", functionId);
            return null;
        }

        double minX = points.stream().mapToDouble(Point::getXValue).min().orElse(0);
        double maxX = points.stream().mapToDouble(Point::getXValue).max().orElse(0);
        double minY = points.stream().mapToDouble(Point::getYValue).min().orElse(0);
        double maxY = points.stream().mapToDouble(Point::getYValue).max().orElse(0);
        double avgX = points.stream().mapToDouble(Point::getXValue).average().orElse(0);
        double avgY = points.stream().mapToDouble(Point::getYValue).average().orElse(0);

        PointStatistics stats = new PointStatistics(
                functionId,
                points.size(),
                minX,
                maxX,
                minY,
                maxY,
                avgX,
                avgY
        );

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_POINT_STATISTICS", durationMs, points.size(), "SPRING_DATA_JPA"));

        logger.info("Статистика точек функции {}: {} точек, x_avg={}, y_avg={} за {} мс",
                functionId, points.size(), avgX, avgY, durationMs);

        return stats;
    }

    public int getTotalPointsCount() {
        long startTime = System.nanoTime();
        logger.debug("Получение общего количества точек");

        long count = pointRepository.count();

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_TOTAL_POINTS_COUNT", durationMs, (int)count, "SPRING_DATA_JPA"));

        logger.info("Общее количество точек: {} (получено за {} мс)", count, durationMs);
        return (int) count;
    }

    public static class PointStatistics {
        private final Long functionId;
        private final int pointCount;
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;
        private final double averageX;
        private final double averageY;

        public PointStatistics(Long functionId, int pointCount,
                               double minX, double maxX, double minY, double maxY,
                               double averageX, double averageY) {
            this.functionId = functionId;
            this.pointCount = pointCount;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.averageX = averageX;
            this.averageY = averageY;
        }

        public Long getFunctionId() { return functionId; }
        public int getPointCount() { return pointCount; }
        public double getMinX() { return minX; }
        public double getMaxX() { return maxX; }
        public double getMinY() { return minY; }
        public double getMaxY() { return maxY; }
        public double getAverageX() { return averageX; }
        public double getAverageY() { return averageY; }

        @Override
        public String toString() {
            return String.format(
                    "PointStatistics{function=%d, points=%d, x=[%.2f, %.2f], y=[%.2f, %.2f], avgX=%.2f, avgY=%.2f}",
                    functionId, pointCount, minX, maxX, minY, maxY, averageX, averageY
            );
        }
    }
}