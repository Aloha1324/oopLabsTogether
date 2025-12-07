package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PointService {
    private static final Logger logger = LoggerFactory.getLogger(PointService.class);
    private final PointDAO pointDAO;
    private final FunctionDAO functionDAO;

    public PointService() {
        this.pointDAO = new PointDAO();
        this.functionDAO = new FunctionDAO();
    }

    public PointService(PointDAO pointDAO, FunctionDAO functionDAO) {
        this.pointDAO = pointDAO;
        this.functionDAO = functionDAO;
    }

    public Long createPoint(Long functionId, Double xValue, Double yValue) {
        logger.info("Создание точки: function={}, x={}, y={}", functionId, xValue, yValue);

        if (functionDAO.findById(functionId) == null) {
            logger.error("Функция с ID {} не существует", functionId);
            throw new IllegalArgumentException("Function with ID " + functionId + " does not exist");
        }

        Long pointId = pointDAO.createPoint(functionId, xValue, yValue);
        logger.debug("Создана точка с ID: {}", pointId);
        return pointId;
    }

    public int createPointsBatch(Long functionId, List<Double> xValues, List<Double> yValues) {
        logger.info("Пакетное создание точек для функции {}: {} точек", functionId, xValues.size());

        if (xValues.size() != yValues.size()) {
            logger.error("Количество X и Y значений не совпадает: {} vs {}", xValues.size(), yValues.size());
            throw new IllegalArgumentException("X and Y values count must be equal");
        }

        if (functionDAO.findById(functionId) == null) {
            logger.error("Функция с ID {} не существует", functionId);
            throw new IllegalArgumentException("Function with ID " + functionId + " does not exist");
        }

        // Создаем список точек для batch вставки
        List<Object[]> points = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            points.add(new Object[]{functionId, xValues.get(i), yValues.get(i)});
        }

        int createdCount = pointDAO.createPointsBatch(points);
        logger.info("Создано {} точек для функции {}", createdCount, functionId);
        return createdCount;
    }

    public int generateFunctionPoints(Long functionId, String functionType, double start, double end, double step) {
        logger.info("Генерация точек для функции {}: type={}, range=[{}, {}], step={}",
                functionId, functionType, start, end, step);

        List<Object[]> points = new ArrayList<>();
        int pointCount = 0;

        for (double x = start; x <= end; x += step) {
            double y = calculateFunction(functionType, x);
            points.add(new Object[]{functionId, x, y});
            pointCount++;
        }

        if (!points.isEmpty()) {
            pointDAO.createPointsBatch(points);
            logger.info("Сгенерировано {} точек для функции {}", pointCount, functionId);
        }

        return pointCount;
    }

    private double calculateFunction(String functionType, double x) {
        return switch (functionType.toLowerCase()) {
            case "linear" -> x;
            case "quadratic" -> x * x;
            case "cubic" -> x * x * x;
            case "sin" -> Math.sin(x);
            case "cos" -> Math.cos(x);
            case "exp" -> Math.exp(x);
            case "log" -> Math.log(Math.abs(x) + 1e-10); // избегаем log(0)
            default -> x;
        };
    }

    public Map<String, Object> getPointById(Long id) {
        logger.debug("Поиск точки по ID: {}", id);
        return pointDAO.findById(id);
    }

    public List<Map<String, Object>> getPointsByFunctionId(Long functionId) {
        logger.debug("Поиск точек функции с ID: {}", functionId);
        return pointDAO.findByFunctionId(functionId);
    }

    public List<Map<String, Object>> getPointsByXRange(Long functionId, Double minX, Double maxX) {
        logger.debug("Поиск точек функции {} в диапазоне x=[{}, {}]", functionId, minX, maxX);
        return pointDAO.findByFunctionIdAndXRange(functionId, minX, maxX);
    }

    public List<Map<String, Object>> getPointsByYRange(Long functionId, Double minY, Double maxY) {
        logger.debug("Поиск точек функции {} в диапазоне y=[{}, {}]", functionId, minY, maxY);
        // Используем существующий метод и фильтруем результаты
        List<Map<String, Object>> allPoints = pointDAO.findByFunctionId(functionId);
        List<Map<String, Object>> filteredPoints = new ArrayList<>();

        for (Map<String, Object> point : allPoints) {
            Double yValue = (Double) point.get("y_value");
            if (yValue >= minY && yValue <= maxY) {
                filteredPoints.add(point);
            }
        }
        return filteredPoints;
    }

    public List<Map<String, Object>> getAllPoints() {
        logger.debug("Получение всех точек");
        return pointDAO.findAll();
    }

    public Map<String, Object> findMaxYPoint(Long functionId) {
        logger.debug("Поиск точки с максимальным Y для функции {}", functionId);
        List<Map<String, Object>> points = pointDAO.findByFunctionId(functionId);

        Map<String, Object> maxPoint = null;
        double maxY = Double.MIN_VALUE;

        for (Map<String, Object> point : points) {
            Double yValue = (Double) point.get("y_value");
            if (yValue > maxY) {
                maxY = yValue;
                maxPoint = point;
            }
        }

        return maxPoint;
    }

    public Map<String, Object> findMinYPoint(Long functionId) {
        logger.debug("Поиск точки с минимальным Y для функции {}", functionId);
        List<Map<String, Object>> points = pointDAO.findByFunctionId(functionId);

        Map<String, Object> minPoint = null;
        double minY = Double.MAX_VALUE;

        for (Map<String, Object> point : points) {
            Double yValue = (Double) point.get("y_value");
            if (yValue < minY) {
                minY = yValue;
                minPoint = point;
            }
        }

        return minPoint;
    }

    public List<Map<String, Object>> findRoots(Long functionId, double tolerance) {
        logger.debug("Поиск корней функции {} с точностью {}", functionId, tolerance);
        List<Map<String, Object>> points = pointDAO.findByFunctionId(functionId);
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Map<String, Object> point : points) {
            Double yValue = (Double) point.get("y_value");
            if (Math.abs(yValue) <= tolerance) {
                roots.add(point);
            }
        }

        logger.debug("Найдено {} корней функции {}", roots.size(), functionId);
        return roots;
    }

    public boolean updatePoint(Long pointId, Long functionId, Double xValue, Double yValue) {
        logger.info("Обновление точки с ID: {}", pointId);

        Map<String, Object> existingPoint = pointDAO.findById(pointId);
        if (existingPoint != null) {
            // Проверка существования функции
            if (functionDAO.findById(functionId) == null) {
                logger.error("Функция с ID {} не существует", functionId);
                return false;
            }

            boolean updated = pointDAO.updatePoint(pointId, xValue, yValue);
            if (updated) {
                logger.info("Точка с ID {} успешно обновлена", pointId);
            }
            return updated;
        }

        logger.warn("Точка с ID {} не найдена для обновления", pointId);
        return false;
    }

    public int recalculatePoints(Long functionId, String functionType) {
        logger.info("Пересчет точек для функции {} с типом {}", functionId, functionType);

        List<Map<String, Object>> points = pointDAO.findByFunctionId(functionId);
        int updatedCount = 0;

        for (Map<String, Object> point : points) {
            Double xValue = (Double) point.get("x_value");
            double newY = calculateFunction(functionType, xValue);

            // Всегда обновляем точку, даже если значение Y не изменилось
            // Это гарантирует, что все точки будут соответствовать новой функции
            Long pointId = (Long) point.get("id");
            if (pointDAO.updatePoint(pointId, xValue, newY)) {
                updatedCount++;
            }
        }

        logger.info("Пересчитано {} точек функции {}", updatedCount, functionId);
        return updatedCount;
    }

    public boolean deletePoint(Long pointId) {
        logger.info("Удаление точки с ID: {}", pointId);
        boolean deleted = pointDAO.deletePoint(pointId);

        if (deleted) {
            logger.info("Точка с ID {} удалена", pointId);
        } else {
            logger.warn("Точка с ID {} не найдена для удаления", pointId);
        }

        return deleted;
    }

    public int deletePointsByFunctionId(Long functionId) {
        logger.info("Удаление всех точек функции с ID: {}", functionId);

        // Получаем количество точек до удаления
        List<Map<String, Object>> pointsBefore = pointDAO.findByFunctionId(functionId);
        int countBefore = pointsBefore.size();

        if (countBefore == 0) {
            logger.info("Нет точек для удаления у функции {}", functionId);
            return 0;
        }

        // Выполняем удаление - метод возвращает int (количество удаленных строк)
        int deletedCount = pointDAO.deletePointsByFunctionId(functionId);

        if (deletedCount > 0) {
            logger.info("Удалено {} точек функции {}", deletedCount, functionId);
            return deletedCount;
        } else {
            logger.warn("Не удалось удалить точки функции {}", functionId);
            return 0;
        }
    }

    public int deletePointsByXRange(Long functionId, Double minX, Double maxX) {
        logger.info("Удаление точек функции {} в диапазоне x=[{}, {}]", functionId, minX, maxX);

        List<Map<String, Object>> pointsToDelete = pointDAO.findByFunctionIdAndXRange(functionId, minX, maxX);
        int deletedCount = 0;

        for (Map<String, Object> point : pointsToDelete) {
            Long pointId = (Long) point.get("id");
            if (pointDAO.deletePoint(pointId)) {
                deletedCount++;
            }
        }

        logger.info("Удалено {} точек в указанном диапазоне", deletedCount);
        return deletedCount;
    }

    public boolean isXValueUnique(Long functionId, Double xValue) {
        logger.debug("Проверка уникальности X={} для функции {}", xValue, functionId);
        List<Map<String, Object>> points = pointDAO.findByFunctionId(functionId);
        return points.stream().noneMatch(p -> {
            Double pointX = (Double) p.get("x_value");
            return pointX.equals(xValue);
        });
    }

    public PointStatistics getPointStatistics(Long functionId) {
        logger.debug("Получение статистики точек для функции {}", functionId);

        List<Map<String, Object>> points = pointDAO.findByFunctionId(functionId);
        if (points.isEmpty()) {
            logger.warn("Нет точек для функции {}", functionId);
            return null;
        }

        double minX = points.stream().mapToDouble(p -> (Double) p.get("x_value")).min().orElse(0);
        double maxX = points.stream().mapToDouble(p -> (Double) p.get("x_value")).max().orElse(0);
        double minY = points.stream().mapToDouble(p -> (Double) p.get("y_value")).min().orElse(0);
        double maxY = points.stream().mapToDouble(p -> (Double) p.get("y_value")).max().orElse(0);
        double avgX = points.stream().mapToDouble(p -> (Double) p.get("x_value")).average().orElse(0);
        double avgY = points.stream().mapToDouble(p -> (Double) p.get("y_value")).average().orElse(0);

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

        logger.info("Статистика точек функции {}: {} точек, x_avg={}, y_avg={}",
                functionId, points.size(), avgX, avgY);

        return stats;
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