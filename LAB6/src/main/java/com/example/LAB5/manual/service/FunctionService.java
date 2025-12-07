package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DAO.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);
    private final FunctionDAO functionDAO;
    private final UserDAO userDAO;
    private final PointDAO pointDAO;

    public FunctionService() {
        this.functionDAO = new FunctionDAO();
        this.userDAO = new UserDAO();
        this.pointDAO = new PointDAO();
    }

    public FunctionService(FunctionDAO functionDAO, UserDAO userDAO, PointDAO pointDAO) {
        this.functionDAO = functionDAO;
        this.userDAO = userDAO;
        this.pointDAO = pointDAO;
    }

    public Long createFunction(String name, Long userId, String expression) {
        logger.info("Создание функции: name={}, user={}, expression={}", name, userId, expression);

        if (userDAO.findById(userId) == null) {
            logger.error("Пользователь с ID {} не существует", userId);
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        Long functionId = functionDAO.createFunction(name, userId, expression);
        logger.info("Создана функция с ID: {}", functionId);
        return functionId;
    }

    public Map<String, Object> getFunctionById(Long id) {
        logger.debug("Поиск функции по ID: {}", id);
        return functionDAO.findById(id);
    }

    public List<Map<String, Object>> getFunctionsByUserId(Long userId) {
        logger.debug("Поиск функций пользователя с ID: {}", userId);
        return functionDAO.findByUserId(userId);
    }

    public List<Map<String, Object>> getFunctionsByName(String name) {
        logger.debug("Поиск функций по имени: {}", name);
        return functionDAO.findByName(name);
    }

    public List<Map<String, Object>> getAllFunctions() {
        logger.debug("Получение всех функций");
        return functionDAO.findAll();
    }

    public List<Map<String, Object>> getAllFunctionsLimited(int limit) {
        logger.debug("Получение ограниченного списка функций: limit={}", limit);
        return functionDAO.getAllFunctionsLimited(limit);
    }

    // ОПТИМИЗИРОВАННЫЙ МЕТОД: пакетная загрузка точек вместо N+1 запросов
    public List<Map<String, Object>> getFunctionsWithPointCount() {
        logger.debug("Получение функций со статистикой по точкам");

        // Шаг 1: Получаем все функции (1 запрос)
        List<Map<String, Object>> functions = functionDAO.findAll();

        // Быстрая проверка - если функций нет, возвращаем пустой список
        if (functions.isEmpty()) {
            logger.debug("Функции не найдены");
            return functions;
        }

        // Шаг 2: Извлекаем все ID функций
        List<Long> functionIds = new ArrayList<>();
        for (Map<String, Object> function : functions) {
            Long functionId = (Long) function.get("id");
            functionIds.add(functionId);
        }

        logger.debug("Найдено {} функций, загружаем точки...", functionIds.size());

        // Шаг 3: ОДИН запрос для всех точек вместо N отдельных запросов
        List<Map<String, Object>> allPoints = pointDAO.findByFunctionIds(functionIds);
        logger.debug("Загружено {} точек для всех функций", allPoints.size());

        // Шаг 4: Группируем точки по function_id для быстрого поиска
        Map<Long, List<Map<String, Object>>> pointsByFunctionId = new HashMap<>();
        for (Map<String, Object> point : allPoints) {
            Long functionId = (Long) point.get("function_id");
            pointsByFunctionId
                    .computeIfAbsent(functionId, k -> new ArrayList<>())
                    .add(point);
        }

        // Шаг 5: Обогащаем функции информацией о точках
        for (Map<String, Object> function : functions) {
            Long functionId = (Long) function.get("id");
            String functionName = (String) function.get("name");

            List<Map<String, Object>> points = pointsByFunctionId.get(functionId);
            int pointCount = (points != null) ? points.size() : 0;

            logger.debug("Функция '{}' (ID: {}) имеет {} точек",
                    functionName, functionId, pointCount);

            // Добавляем количество точек в результат
            function.put("point_count", pointCount);
        }

        logger.info("Успешно обработано {} функций со статистикой точек", functions.size());
        return functions;
    }

    public boolean updateFunction(Long functionId, String name, Long userId, String expression) {
        logger.info("Обновление функции с ID: {}", functionId);

        Map<String, Object> existingFunction = functionDAO.findById(functionId);
        if (existingFunction != null) {
            if (userDAO.findById(userId) == null) {
                logger.error("Пользователь с ID {} не существует", userId);
                return false;
            }

            boolean updated = functionDAO.updateFunction(functionId, name, userId, expression);
            if (updated) {
                logger.info("Функция с ID {} успешно обновлена", functionId);
            }
            return updated;
        }

        logger.warn("Функция с ID {} не найдена для обновления", functionId);
        return false;
    }

    public boolean updateFunctionExpression(Long functionId, String newExpression) {
        logger.info("Обновление выражения функции с ID: {}", functionId);

        Map<String, Object> existingFunction = functionDAO.findById(functionId);
        if (existingFunction != null) {
            boolean updated = functionDAO.updateFunctionExpression(functionId, newExpression);
            if (updated) {
                logger.info("Выражение функции с ID {} обновлено", functionId);
            }
            return updated;
        }

        logger.warn("Функция с ID {} не найдена", functionId);
        return false;
    }

    public boolean deleteFunction(Long functionId) {
        logger.info("Удаление функции с ID: {}", functionId);

        // Метод deletePointsByFunctionId возвращает int (количество удаленных строк)
        int pointsDeleted = pointDAO.deletePointsByFunctionId(functionId);
        logger.info("Точки функции с ID {} удалены: {} точек", functionId, pointsDeleted);

        boolean deleted = functionDAO.deleteFunction(functionId);
        if (deleted) {
            logger.info("Функция с ID {} и все её точки удалены", functionId);
        } else {
            logger.warn("Функция с ID {} не найдена для удаления", functionId);
        }

        return deleted;
    }

    public int deleteFunctionsByUserId(Long userId) {
        logger.info("Удаление всех функций пользователя с ID: {}", userId);

        List<Map<String, Object>> userFunctions = functionDAO.findByUserId(userId);
        int totalDeleted = 0;

        for (Map<String, Object> function : userFunctions) {
            Long functionId = (Long) function.get("id");
            if (deleteFunction(functionId)) {
                totalDeleted++;
            }
        }

        logger.info("Удалено {} функций пользователя с ID: {}", totalDeleted, userId);
        return totalDeleted;
    }

    public boolean validateFunctionName(Long userId, String functionName) {
        logger.debug("Проверка уникальности имени функции для пользователя: {}", userId);

        List<Map<String, Object>> userFunctions = functionDAO.findByUserId(userId);
        return userFunctions.stream()
                .noneMatch(func -> {
                    String name = (String) func.get("name");
                    return name != null && name.equalsIgnoreCase(functionName);
                });
    }

    public FunctionStatistics getFunctionStatistics(Long functionId) {
        logger.debug("Получение статистики для функции с ID: {}", functionId);

        Map<String, Object> function = functionDAO.findById(functionId);
        if (function != null) {
            List<Map<String, Object>> points = pointDAO.findByFunctionId(functionId);

            double minX = points.stream().mapToDouble(p -> (Double) p.get("x_value")).min().orElse(0);
            double maxX = points.stream().mapToDouble(p -> (Double) p.get("x_value")).max().orElse(0);
            double minY = points.stream().mapToDouble(p -> (Double) p.get("y_value")).min().orElse(0);
            double maxY = points.stream().mapToDouble(p -> (Double) p.get("y_value")).max().orElse(0);
            double avgY = points.stream().mapToDouble(p -> (Double) p.get("y_value")).average().orElse(0);

            FunctionStatistics stats = new FunctionStatistics(
                    functionId,
                    (String) function.get("name"),
                    points.size(),
                    minX,
                    maxX,
                    minY,
                    maxY,
                    avgY
            );

            logger.info("Статистика функции {}: {} точек, x=[{}, {}], y=[{}, {}]",
                    function.get("name"), points.size(), minX, maxX, minY, maxY);

            return stats;
        }

        logger.warn("Функция с ID {} не найдена для статистики", functionId);
        return null;
    }

    // Дополнительные методы
    public List<Map<String, Object>> getFunctionsWithUsername() {
        logger.debug("Получение функций с именами пользователей");
        return functionDAO.getFunctionsWithUsername();
    }

    public List<Map<String, Object>> searchFunctionsByExpression(String expressionPattern) {
        logger.debug("Поиск функций по выражению: {}", expressionPattern);
        return functionDAO.findByExpression(expressionPattern);
    }

    public int getFunctionCountByUser(Long userId) {
        logger.debug("Получение количества функций пользователя с ID: {}", userId);
        List<Map<String, Object>> counts = functionDAO.getFunctionCountByUser();
        return counts.stream()
                .filter(count -> userId.equals(count.get("user_id")))
                .mapToInt(count -> (Integer) count.get("function_count"))
                .findFirst()
                .orElse(0);
    }

    public int getTotalFunctionsCount() {
        logger.debug("Получение общего количества функций");
        return functionDAO.getTotalFunctionsCount();
    }

    public static class FunctionStatistics {
        private final Long functionId;
        private final String functionName;
        private final int pointCount;
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;
        private final double averageY;

        public FunctionStatistics(Long functionId, String functionName, int pointCount,
                                  double minX, double maxX, double minY, double maxY, double averageY) {
            this.functionId = functionId;
            this.functionName = functionName;
            this.pointCount = pointCount;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.averageY = averageY;
        }

        public Long getFunctionId() { return functionId; }
        public String getFunctionName() { return functionName; }
        public int getPointCount() { return pointCount; }
        public double getMinX() { return minX; }
        public double getMaxX() { return maxX; }
        public double getMinY() { return minY; }
        public double getMaxY() { return maxY; }
        public double getAverageY() { return averageY; }

        @Override
        public String toString() {
            return String.format(
                    "FunctionStatistics{function='%s', points=%d, x=[%.2f, %.2f], y=[%.2f, %.2f], avgY=%.2f}",
                    functionName, pointCount, minX, maxX, minY, maxY, averageY
            );
        }
    }
}