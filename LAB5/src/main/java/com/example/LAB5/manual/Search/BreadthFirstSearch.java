package com.example.LAB5.manual.Search;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class BreadthFirstSearch {
    private static final Logger logger = LoggerFactory.getLogger(BreadthFirstSearch.class);

    private final FunctionDAO functionDAO;
    private final PointDAO pointDAO;

    public BreadthFirstSearch(FunctionDAO functionDAO, PointDAO pointDAO) {
        this.functionDAO = functionDAO;
        this.pointDAO = pointDAO;
    }

    // ========== МЕТОДЫ КОНВЕРТАЦИИ ==========

    private FunctionDTO convertToFunctionDTO(Map<String, Object> map) {
        if (map == null) return null;

        try {
            return new FunctionDTO(
                    map.get("id") != null ? ((Number) map.get("id")).longValue() : null,
                    map.get("user_id") != null ? ((Number) map.get("user_id")).longValue() : null,
                    (String) map.get("name"),
                    (String) map.get("signature")
            );
        } catch (Exception e) {
            logger.error("Error converting Map to FunctionDTO: {}", e.getMessage());
            return null;
        }
    }

    private PointDTO convertToPointDTO(Map<String, Object> map) {
        if (map == null) return null;

        try {
            return new PointDTO(
                    map.get("id") != null ? ((Number) map.get("id")).longValue() : null,
                    map.get("function_id") != null ? ((Number) map.get("function_id")).longValue() : null,
                    map.get("x_value") != null ? ((Number) map.get("x_value")).doubleValue() : null,
                    map.get("y_value") != null ? ((Number) map.get("y_value")).doubleValue() : null
            );
        } catch (Exception e) {
            logger.error("Error converting Map to PointDTO: {}", e.getMessage());
            return null;
        }
    }

    private List<FunctionDTO> convertToFunctionDTOList(List<Map<String, Object>> rawData) {
        if (rawData == null) return new ArrayList<>();

        return rawData.stream()
                .map(this::convertToFunctionDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<PointDTO> convertToPointDTOList(List<Map<String, Object>> rawData) {
        if (rawData == null) return new ArrayList<>();

        return rawData.stream()
                .map(this::convertToPointDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ========== ОСНОВНЫЕ МЕТОДЫ BFS ==========

    public List<FunctionDTO> searchFunctionsByUserBFS(Long startUserId) {
        logger.info("Starting BFS search for functions from user ID: {}", startUserId);

        List<FunctionDTO> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        queue.offer(startUserId);
        visited.add(startUserId);

        while (!queue.isEmpty()) {
            Long currentUserId = queue.poll();
            logger.debug("Processing user ID: {}", currentUserId);

            try {
                List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(currentUserId);
                List<FunctionDTO> userFunctions = convertToFunctionDTOList(rawFunctions);

                result.addAll(userFunctions);
                logger.debug("Found {} functions for user ID {}", userFunctions.size(), currentUserId);

                // Если нужно искать по связанным пользователям
                expandUserSearch(currentUserId, queue, visited);

            } catch (Exception e) {
                logger.error("Error during BFS search for user ID {}: {}", currentUserId, e.getMessage(), e);
            }
        }

        logger.info("BFS search completed, found {} functions", result.size());
        return result;
    }

    public List<PointDTO> searchPointsByFunctionBFS(FunctionDTO startFunction) {
        logger.info("Starting BFS search for points from function ID: {}", startFunction.getId());

        List<PointDTO> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        queue.offer(startFunction.getId());
        visited.add(startFunction.getId());

        while (!queue.isEmpty()) {
            Long currentFunctionId = queue.poll();
            logger.debug("Processing function ID: {}", currentFunctionId);

            try {
                List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(currentFunctionId);
                List<PointDTO> functionPoints = convertToPointDTOList(rawPoints);

                result.addAll(functionPoints);
                logger.debug("Found {} points for function ID {}", functionPoints.size(), currentFunctionId);

                // Если нужно искать по связанным функциям
                expandFunctionSearch(currentFunctionId, queue, visited);

            } catch (Exception e) {
                logger.error("Error during BFS points search for function ID {}: {}", currentFunctionId, e.getMessage(), e);
            }
        }

        logger.info("BFS points search completed, found {} points", result.size());
        return result;
    }

    // Метод для поиска точек в диапазоне
    public List<PointDTO> searchPointsInRangeBFS(Long startFunctionId, double minX, double maxX, double minY, double maxY) {
        logger.info("Starting BFS search for points in range: x[{}, {}], y[{}, {}] for function ID: {}",
                minX, maxX, minY, maxY, startFunctionId);

        List<PointDTO> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        queue.offer(startFunctionId);
        visited.add(startFunctionId);

        while (!queue.isEmpty()) {
            Long currentFunctionId = queue.poll();
            logger.debug("Processing function ID: {}", currentFunctionId);

            try {
                List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(currentFunctionId);
                List<PointDTO> allPoints = convertToPointDTOList(rawPoints);

                List<PointDTO> pointsInRange = allPoints.stream()
                        .filter(p -> p.getXValue() != null && p.getYValue() != null &&
                                p.getXValue() >= minX && p.getXValue() <= maxX &&
                                p.getYValue() >= minY && p.getYValue() <= maxY)
                        .collect(Collectors.toList());

                result.addAll(pointsInRange);
                logger.debug("Found {} points in range for function ID {}", pointsInRange.size(), currentFunctionId);

            } catch (Exception e) {
                logger.error("Error during BFS range search for function ID {}: {}", currentFunctionId, e.getMessage(), e);
            }
        }

        logger.info("BFS range search completed, found {} points", result.size());
        return result;
    }

    // ========== РАСШИРЕННЫЙ ПОИСК С ФИЛЬТРАМИ ==========

    public List<FunctionDTO> searchFunctionsWithFilters(Long startUserId, String nameFilter, String signatureFilter) {
        logger.info("Starting BFS search for functions with filters - name: {}, signature: {}", nameFilter, signatureFilter);

        List<FunctionDTO> allFunctions = searchFunctionsByUserBFS(startUserId);

        return allFunctions.stream()
                .filter(func ->
                        (nameFilter == null || nameFilter.isEmpty() ||
                                (func.getName() != null && func.getName().toLowerCase().contains(nameFilter.toLowerCase()))) &&
                                (signatureFilter == null || signatureFilter.isEmpty() ||
                                        (func.getSignature() != null && func.getSignature().toLowerCase().contains(signatureFilter.toLowerCase())))
                )
                .collect(Collectors.toList());
    }

    public List<PointDTO> searchPointsWithFilters(FunctionDTO startFunction, Double minX, Double maxX, Double minY, Double maxY) {
        logger.info("Starting BFS search for points with coordinate filters");

        List<PointDTO> allPoints = searchPointsByFunctionBFS(startFunction);

        return allPoints.stream()
                .filter(point ->
                        point.getXValue() != null && point.getYValue() != null &&
                                (minX == null || point.getXValue() >= minX) &&
                                (maxX == null || point.getXValue() <= maxX) &&
                                (minY == null || point.getYValue() >= minY) &&
                                (maxY == null || point.getYValue() <= maxY)
                )
                .collect(Collectors.toList());
    }

    // ========== УНИКАЛЬНЫЙ ПОИСК ==========

    public List<FunctionDTO> searchUniqueFunctionsByName(Long startUserId) {
        logger.info("Starting BFS search for unique functions by name");

        List<FunctionDTO> allFunctions = searchFunctionsByUserBFS(startUserId);

        Map<String, FunctionDTO> uniqueFunctions = new LinkedHashMap<>();
        for (FunctionDTO function : allFunctions) {
            if (function.getName() != null) {
                uniqueFunctions.putIfAbsent(function.getName().toLowerCase(), function);
            }
        }

        return new ArrayList<>(uniqueFunctions.values());
    }

    // ========== СТАТИСТИКА ==========

    public Map<String, Object> getSearchStatistics(Long userId) {
        logger.info("Generating BFS search statistics for user ID: {}", userId);

        Map<String, Object> stats = new HashMap<>();

        try {
            List<FunctionDTO> functions = searchFunctionsByUserBFS(userId);
            int totalPoints = 0;

            for (FunctionDTO function : functions) {
                List<PointDTO> points = searchPointsByFunctionBFS(function);
                totalPoints += points.size();
            }

            stats.put("userId", userId);
            stats.put("totalFunctions", functions.size());
            stats.put("totalPoints", totalPoints);
            stats.put("averagePointsPerFunction", functions.isEmpty() ? 0 : totalPoints / functions.size());
            stats.put("searchAlgorithm", "Breadth First Search");
            stats.put("timestamp", new Date());

        } catch (Exception e) {
            logger.error("Error generating BFS statistics: {}", e.getMessage(), e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void expandUserSearch(Long userId, Queue<Long> queue, Set<Long> visited) {
        logger.debug("Expanding search from user ID: {}", userId);
        // Здесь можно добавить логику для поиска связанных пользователей
        // если в вашей модели данных есть связи между пользователями
    }

    private void expandFunctionSearch(Long functionId, Queue<Long> queue, Set<Long> visited) {
        logger.debug("Expanding search from function ID: {}", functionId);
        // Здесь можно добавить логику для поиска связанных функций
        // если в вашей модели данных есть связи между функциями
    }

    // ========== УТИЛИТНЫЕ МЕТОДЫ ==========

    public String getAlgorithmName() {
        return "BREADTH_FIRST_SEARCH";
    }

    public List<String> getAvailableSearchMethods() {
        return Arrays.asList(
                "searchFunctionsByUserBFS",
                "searchPointsByFunctionBFS",
                "searchPointsInRangeBFS",
                "searchFunctionsWithFilters",
                "searchPointsWithFilters",
                "searchUniqueFunctionsByName"
        );
    }

    // ========== ВАЛИДАЦИЯ ==========

    private boolean validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            logger.error("Invalid user ID: {}", userId);
            return false;
        }
        return true;
    }

    private boolean validateFunctionId(Long functionId) {
        if (functionId == null || functionId <= 0) {
            logger.error("Invalid function ID: {}", functionId);
            return false;
        }
        return true;
    }
}