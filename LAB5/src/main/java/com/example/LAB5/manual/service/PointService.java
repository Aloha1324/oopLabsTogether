package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DTO.PointDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PointService {

    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    private final PointDAO pointDao;
    private final FunctionDAO functionDao;

    public PointService() {
        this(new PointDAO(), new FunctionDAO());
    }

    public PointService(PointDAO pointDao, FunctionDAO functionDao) {
        this.pointDao = pointDao;
        this.functionDao = functionDao;
    }

    public Long createPoint(Long functionId, Double xValue, Double yValue) {
        log.info("Создание точки: functionId={}, x={}, y={}", functionId, xValue, yValue);

        if (functionDao.findById(functionId).isEmpty()) {
            log.error("Функция {} не существует, точка не создаётся", functionId);
            throw new IllegalArgumentException("Function with ID " + functionId + " does not exist");
        }

        PointDTO dto = new PointDTO(functionId, xValue, yValue);
        Long id = pointDao.createPoint(dto);
        log.debug("Создана точка id={}", id);
        return id;
    }

    public int createPointsBatch(Long functionId, List<Double> xValues, List<Double> yValues) {
        log.info("Пакетное создание точек для функции {} ({} штук)",
                functionId, xValues.size());

        if (xValues.size() != yValues.size()) {
            log.error("Размеры списков X и Y не совпадают: {} и {}",
                    xValues.size(), yValues.size());
            throw new IllegalArgumentException("X and Y values count must be equal");
        }

        if (functionDao.findById(functionId).isEmpty()) {
            log.error("Функция {} не найдена", functionId);
            throw new IllegalArgumentException("Function with ID " + functionId + " does not exist");
        }

        List<PointDTO> batch = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            batch.add(new PointDTO(functionId, xValues.get(i), yValues.get(i)));
        }

        int created = pointDao.createPoints(batch);
        log.info("Для функции {} создано {} точек", functionId, created);
        return created;
    }

    public int generateFunctionPoints(Long functionId,
                                      String functionType,
                                      double start,
                                      double end,
                                      double step) {
        log.info("Генерация точек: funcId={}, type={}, range=[{}, {}], step={}",
                functionId, functionType, start, end, step);

        List<PointDTO> generated = new ArrayList<>();
        for (double x = start; x <= end; x += step) {
            double y = calculateFunction(functionType, x);
            generated.add(new PointDTO(functionId, x, y));
        }

        if (!generated.isEmpty()) {
            pointDao.createPoints(generated);
            log.info("Сгенерировано {} точек для функции {}", generated.size(), functionId);
        }

        return generated.size();
    }

    private double calculateFunction(String type, double x) {
        String key = type == null ? "" : type.toLowerCase();
        switch (key) {
            case "linear":
                return x;
            case "quadratic":
                return x * x;
            case "cubic":
                return x * x * x;
            case "sin":
                return Math.sin(x);
            case "cos":
                return Math.cos(x);
            case "exp":
                return Math.exp(x);
            case "log":
                return Math.log(x);
            default:
                return x;
        }
    }

    public Optional<PointDTO> getPointById(Long id) {
        log.debug("Поиск точки по id {}", id);
        return pointDao.findById(id);
    }

    public List<PointDTO> getPointsByFunctionId(Long functionId) {
        log.debug("Поиск точек функции {}", functionId);
        return pointDao.findByFunctionId(functionId);
    }

    public List<PointDTO> getPointsByXRange(Long functionId, Double minX, Double maxX) {
        log.debug("Поиск точек: funcId={}, x∈[{}, {}]", functionId, minX, maxX);
        return pointDao.findByXRange(functionId, minX, maxX);
    }

    public List<PointDTO> getPointsByYRange(Long functionId, Double minY, Double maxY) {
        log.debug("Поиск точек: funcId={}, y∈[{}, {}]", functionId, minY, maxY);
        return pointDao.findByYRange(functionId, minY, maxY);
    }

    public List<PointDTO> getAllPoints() {
        log.debug("Чтение всех точек");
        return pointDao.findAll();
    }

    public PointDTO findMaxYPoint(Long functionId) {
        log.debug("Поиск точки с максимальным Y, funcId={}", functionId);
        return pointDao.findByFunctionId(functionId).stream()
                .max(Comparator.comparingDouble(PointDTO::getYValue))
                .orElse(null);
    }

    public PointDTO findMinYPoint(Long functionId) {
        log.debug("Поиск точки с минимальным Y, funcId={}", functionId);
        return pointDao.findByFunctionId(functionId).stream()
                .min(Comparator.comparingDouble(PointDTO::getYValue))
                .orElse(null);
    }

    public List<PointDTO> findRoots(Long functionId, double tolerance) {
        log.debug("Поиск корней функции {}, допуск={}", functionId, tolerance);

        List<PointDTO> result = new ArrayList<>();
        for (PointDTO p : pointDao.findByFunctionId(functionId)) {
            if (Math.abs(p.getYValue()) <= tolerance) {
                result.add(p);
            }
        }

        log.debug("Найдено {} корней для функции {}", result.size(), functionId);
        return result;
    }

    public boolean updatePoint(Long pointId,
                               Long functionId,
                               Double xValue,
                               Double yValue) {
        log.info("Обновление точки id={}", pointId);

        Optional<PointDTO> current = pointDao.findById(pointId);
        if (current.isEmpty()) {
            log.warn("Точка id={} не найдена для обновления", pointId);
            return false;
        }

        if (functionDao.findById(functionId).isEmpty()) {
            log.error("Не найдена функция {} для обновления точки {}", functionId, pointId);
            return false;
        }

        PointDTO dto = current.get();
        dto.setFunctionId(functionId);
        dto.setXValue(xValue);
        dto.setYValue(yValue);

        boolean updated = pointDao.updatePoint(dto);
        if (updated) {
            log.info("Точка id={} успешно обновлена", pointId);
        }
        return updated;
    }

    public int recalculatePoints(Long functionId, String functionType) {
        log.info("Пересчёт точек функции {}, тип={}", functionId, functionType);

        List<PointDTO> points = pointDao.findByFunctionId(functionId);
        int changed = 0;

        for (PointDTO p : points) {
            double newY = calculateFunction(functionType, p.getXValue());
            if (!p.getYValue().equals(newY)) {
                p.setYValue(newY);
                if (pointDao.updatePoint(p)) {
                    changed++;
                }
            }
        }

        log.info("Для функции {} пересчитано {} точек", functionId, changed);
        return changed;
    }

    public boolean deletePoint(Long pointId) {
        log.info("Удаление точки id={}", pointId);
        boolean removed = pointDao.deletePoint(pointId);

        if (removed) {
            log.info("Точка id={} удалена", pointId);
        } else {
            log.warn("Точка id={} не найдена для удаления", pointId);
        }
        return removed;
    }

    public int deletePointsByFunctionId(Long functionId) {
        log.info("Удаление всех точек функции id={}", functionId);
        int count = pointDao.deleteByFunctionId(functionId);
        log.info("Удалено {} точек функции {}", count, functionId);
        return count;
    }

    public int deletePointsByXRange(Long functionId, Double minX, Double maxX) {
        log.info("Удаление точек funcId={}, x∈[{}, {}]", functionId, minX, maxX);

        List<PointDTO> toRemove = pointDao.findByXRange(functionId, minX, maxX);
        int deleted = 0;

        for (PointDTO p : toRemove) {
            if (pointDao.deletePoint(p.getId())) {
                deleted++;
            }
        }

        log.info("Удалено {} точек в диапазоне", deleted);
        return deleted;
    }

    public boolean isXValueUnique(Long functionId, Double xValue) {
        log.debug("Проверка уникальности x={} для функции {}", xValue, functionId);
        return pointDao.findByFunctionId(functionId).stream()
                .noneMatch(p -> p.getXValue().equals(xValue));
    }

    public PointStatistics getPointStatistics(Long functionId) {
        log.debug("Получение статистики по точкам функции {}", functionId);

        List<PointDTO> points = pointDao.findByFunctionId(functionId);
        if (points.isEmpty()) {
            log.warn("Для функции {} нет точек", functionId);
            return null;
        }

        double minX = points.stream().mapToDouble(PointDTO::getXValue).min().orElse(0);
        double maxX = points.stream().mapToDouble(PointDTO::getXValue).max().orElse(0);
        double minY = points.stream().mapToDouble(PointDTO::getYValue).min().orElse(0);
        double maxY = points.stream().mapToDouble(PointDTO::getYValue).max().orElse(0);
        double avgX = points.stream().mapToDouble(PointDTO::getXValue).average().orElse(0);
        double avgY = points.stream().mapToDouble(PointDTO::getYValue).average().orElse(0);

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

        log.info("Статистика по точкам функции {}: count={}, avgX={}, avgY={}",
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

        public PointStatistics(Long functionId,
                               int pointCount,
                               double minX,
                               double maxX,
                               double minY,
                               double maxY,
                               double averageX,
                               double averageY) {
            this.functionId = functionId;
            this.pointCount = pointCount;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.averageX = averageX;
            this.averageY = averageY;
        }

        public Long getFunctionId() {
            return functionId;
        }

        public int getPointCount() {
            return pointCount;
        }

        public double getMinX() {
            return minX;
        }

        public double getMaxX() {
            return maxX;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getAverageX() {
            return averageX;
        }

        public double getAverageY() {
            return averageY;
        }

        @Override
        public String toString() {
            return String.format(
                    "PointStatistics{functionId=%d, points=%d, x=[%.2f, %.2f], y=[%.2f, %.2f], avgX=%.2f, avgY=%.2f}",
                    functionId, pointCount, minX, maxX, minY, maxY, averageX, averageY
            );
        }
    }
}
