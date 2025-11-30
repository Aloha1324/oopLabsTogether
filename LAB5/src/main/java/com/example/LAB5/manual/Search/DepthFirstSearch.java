package com.example.LAB5.manual.Search;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DepthFirstSearch {
    private static final Logger logger = LoggerFactory.getLogger(DepthFirstSearch.class);

    private final FunctionDAO functionDAO;
    private final PointDAO pointDAO;

    public DepthFirstSearch(FunctionDAO functionDAO, PointDAO pointDAO) {
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

    // ========== ОСНОВНЫЕ МЕТОДЫ DFS ==========

    public List<FunctionDTO> searchFunctionsByUserHierarchy(Long startUserId, Set<Long> visited) {
        logger.info("Starting DFS search for functions by user hierarchy from user ID: {}", startUserId);

        if (!validateUserId(startUserId)) {
            return new ArrayList<>();
        }

        if (visited == null) {
            visited = new HashSet<>();
        }

        if (visited.contains(startUserId)) {
            logger.debug("User ID {} already visited, skipping", startUserId);
            return new ArrayList<>();
        }

        visited.add(startUserId);
        List<FunctionDTO> result = new ArrayList<>();

        try {
            List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(startUserId);
            List<FunctionDTO> userFunctions = convertToFunctionDTOList(rawFunctions);
            result.addAll(userFunctions);
            logger.debug("Found {} functions for user ID {}", userFunctions.size(), startUserId);

        } catch (Exception e) {
            logger.error("Error during DFS search for user ID {}: {}", startUserId, e.getMessage(), e);
        }

        logger.info("DFS search completed for user ID: {}, found {} functions", startUserId, result.size());
        return result;
    }

    public List<PointDTO> searchPointsByFunctionHierarchy(Long startFunctionId, Set<Long> visited) {
        logger.info("Starting DFS search for points by function hierarchy from function ID: {}", startFunctionId);

        if (!validateFunctionId(startFunctionId)) {
            return new ArrayList<>();
        }

        if (visited == null) {
            visited = new HashSet<>();
        }

        if (visited.contains(startFunctionId)) {
            logger.debug("Function ID {} already visited, skipping", startFunctionId);
            return new ArrayList<>();
        }

        visited.add(startFunctionId);
        List<PointDTO> result = new ArrayList<>();

        try {
            List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(startFunctionId);
            List<PointDTO> functionPoints = convertToPointDTOList(rawPoints);
            result.addAll(functionPoints);
            logger.debug("Found {} points for function ID {}", functionPoints.size(), startFunctionId);

        } catch (Exception e) {
            logger.error("Error during DFS search for function ID {}: {}", startFunctionId, e.getMessage(), e);
        }

        logger.info("DFS search completed for function ID: {}, found {} points", startFunctionId, result.size());
        return result;
    }

    // ========== РАСШИРЕННЫЙ ПОИСК ФУНКЦИЙ ==========

    public List<FunctionDTO> searchFunctionsWithNameFilter(Long userId, String nameFilter) {
        logger.info("Searching functions for user {} with name filter: {}", userId, nameFilter);

        if (!validateUserId(userId)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
        List<FunctionDTO> allFunctions = convertToFunctionDTOList(rawFunctions);

        if (nameFilter == null || nameFilter.trim().isEmpty()) {
            return allFunctions;
        }

        List<FunctionDTO> filteredFunctions = new ArrayList<>();
        String filterLower = nameFilter.toLowerCase();

        for (FunctionDTO function : allFunctions) {
            if (function.getName() != null && function.getName().toLowerCase().contains(filterLower)) {
                filteredFunctions.add(function);
            }
        }

        logger.info("Found {} functions after name filtering", filteredFunctions.size());
        return filteredFunctions;
    }

    public List<FunctionDTO> searchFunctionsWithSignatureFilter(Long userId, String signatureFilter) {
        logger.info("Searching functions for user {} with signature filter: {}", userId, signatureFilter);

        if (!validateUserId(userId)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
        List<FunctionDTO> allFunctions = convertToFunctionDTOList(rawFunctions);

        if (signatureFilter == null || signatureFilter.trim().isEmpty()) {
            return allFunctions;
        }

        List<FunctionDTO> filteredFunctions = new ArrayList<>();
        String filterLower = signatureFilter.toLowerCase();

        for (FunctionDTO function : allFunctions) {
            if (function.getSignature() != null && function.getSignature().toLowerCase().contains(filterLower)) {
                filteredFunctions.add(function);
            }
        }

        logger.info("Found {} functions after signature filtering", filteredFunctions.size());
        return filteredFunctions;
    }

    public List<FunctionDTO> searchFunctionsByIdRange(Long userId, Long minId, Long maxId) {
        logger.info("Searching functions for user {} with ID range: {} - {}", userId, minId, maxId);

        if (!validateUserId(userId)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
        List<FunctionDTO> allFunctions = convertToFunctionDTOList(rawFunctions);

        if (minId == null && maxId == null) {
            return allFunctions;
        }

        List<FunctionDTO> filteredFunctions = new ArrayList<>();

        for (FunctionDTO function : allFunctions) {
            boolean matches = true;

            if (minId != null) {
                matches = matches && function.getId() >= minId;
            }

            if (maxId != null) {
                matches = matches && function.getId() <= maxId;
            }

            if (matches) {
                filteredFunctions.add(function);
            }
        }

        logger.info("Found {} functions in ID range", filteredFunctions.size());
        return filteredFunctions;
    }

    // ========== РАСШИРЕННЫЙ ПОИСК ТОЧЕК ==========

    public List<PointDTO> searchPointsByXRange(Long functionId, Double minX, Double maxX) {
        logger.info("Searching points for function {} with X range: {} - {}", functionId, minX, maxX);

        if (!validateFunctionId(functionId)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(functionId);
        List<PointDTO> allPoints = convertToPointDTOList(rawPoints);

        if (minX == null && maxX == null) {
            return allPoints;
        }

        List<PointDTO> filteredPoints = new ArrayList<>();

        for (PointDTO point : allPoints) {
            boolean matches = true;

            if (minX != null) {
                matches = matches && point.getXValue() >= minX;
            }

            if (maxX != null) {
                matches = matches && point.getXValue() <= maxX;
            }

            if (matches) {
                filteredPoints.add(point);
            }
        }

        logger.info("Found {} points in X range", filteredPoints.size());
        return filteredPoints;
    }

    public List<PointDTO> searchPointsByYRange(Long functionId, Double minY, Double maxY) {
        logger.info("Searching points for function {} with Y range: {} - {}", functionId, minY, maxY);

        if (!validateFunctionId(functionId)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(functionId);
        List<PointDTO> allPoints = convertToPointDTOList(rawPoints);

        if (minY == null && maxY == null) {
            return allPoints;
        }

        List<PointDTO> filteredPoints = new ArrayList<>();

        for (PointDTO point : allPoints) {
            boolean matches = true;

            if (minY != null) {
                matches = matches && point.getYValue() >= minY;
            }

            if (maxY != null) {
                matches = matches && point.getYValue() <= maxY;
            }

            if (matches) {
                filteredPoints.add(point);
            }
        }

        logger.info("Found {} points in Y range", filteredPoints.size());
        return filteredPoints;
    }

    public List<PointDTO> searchPointsByCoordinateRange(Long functionId, Double minX, Double maxX, Double minY, Double maxY) {
        logger.info("Searching points for function {} with coordinate range: x[{},{}], y[{},{}]",
                functionId, minX, maxX, minY, maxY);

        if (!validateFunctionId(functionId)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(functionId);
        List<PointDTO> allPoints = convertToPointDTOList(rawPoints);

        if ((minX == null && maxX == null && minY == null && maxY == null)) {
            return allPoints;
        }

        List<PointDTO> filteredPoints = new ArrayList<>();

        for (PointDTO point : allPoints) {
            boolean matches = true;

            if (minX != null) {
                matches = matches && point.getXValue() >= minX;
            }
            if (maxX != null) {
                matches = matches && point.getXValue() <= maxX;
            }
            if (minY != null) {
                matches = matches && point.getYValue() >= minY;
            }
            if (maxY != null) {
                matches = matches && point.getYValue() <= maxY;
            }

            if (matches) {
                filteredPoints.add(point);
            }
        }

        logger.info("Found {} points in coordinate range", filteredPoints.size());
        return filteredPoints;
    }

    // ========== УНИКАЛЬНЫЙ ПОИСК ==========

    public List<FunctionDTO> searchUniqueFunctionsByName(Long userId) {
        logger.info("Searching unique functions by name for user: {}", userId);

        if (!validateUserId(userId)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
        List<FunctionDTO> allFunctions = convertToFunctionDTOList(rawFunctions);
        Map<String, FunctionDTO> uniqueFunctions = new LinkedHashMap<>();

        for (FunctionDTO function : allFunctions) {
            if (function.getName() != null) {
                uniqueFunctions.putIfAbsent(function.getName().toLowerCase(), function);
            }
        }

        List<FunctionDTO> result = new ArrayList<>(uniqueFunctions.values());
        logger.info("Found {} unique functions out of {}", result.size(), allFunctions.size());
        return result;
    }

    // ========== ГЛУБОКИЙ ПОИСК ==========

    public List<FunctionDTO> deepSearchFunctions(Long startUserId, int maxDepth) {
        logger.info("Starting deep DFS search for functions with max depth: {}", maxDepth);

        if (!validateUserId(startUserId) || maxDepth <= 0) {
            return new ArrayList<>();
        }

        return deepSearchFunctionsRecursive(startUserId, new HashSet<>(), 0, maxDepth);
    }

    private List<FunctionDTO> deepSearchFunctionsRecursive(Long userId, Set<Long> visited, int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth || visited.contains(userId)) {
            return new ArrayList<>();
        }

        visited.add(userId);
        List<FunctionDTO> result = new ArrayList<>();

        try {
            List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
            List<FunctionDTO> functions = convertToFunctionDTOList(rawFunctions);
            result.addAll(functions);

            // Здесь можно добавить рекурсивный вызов для связанных пользователей
            // если в вашей модели данных есть связи между пользователями

        } catch (Exception e) {
            logger.error("Error in deep search for user ID {}: {}", userId, e.getMessage());
        }

        return result;
    }

    // ========== СТАТИСТИКА ==========

    public Map<String, Object> getSearchStatistics(Long userId) {
        logger.info("Generating DFS search statistics for user ID: {}", userId);

        Map<String, Object> stats = new HashMap<>();

        try {
            if (!validateUserId(userId)) {
                stats.put("error", "Invalid user ID");
                return stats;
            }

            List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
            List<FunctionDTO> functions = convertToFunctionDTOList(rawFunctions);
            int totalPoints = 0;

            for (FunctionDTO function : functions) {
                List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(function.getId());
                List<PointDTO> points = convertToPointDTOList(rawPoints);
                totalPoints += points.size();
            }

            stats.put("userId", userId);
            stats.put("totalFunctions", functions.size());
            stats.put("totalPoints", totalPoints);
            stats.put("averagePointsPerFunction", functions.isEmpty() ? 0 : totalPoints / functions.size());
            stats.put("searchAlgorithm", "Depth First Search");
            stats.put("timestamp", new Date());

        } catch (Exception e) {
            logger.error("Error generating DFS statistics: {}", e.getMessage(), e);
            stats.put("error", e.getMessage());
        }

        return stats;
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

    // ========== УТИЛИТНЫЕ МЕТОДЫ ==========

    public String getAlgorithmName() {
        return "DEPTH_FIRST_SEARCH";
    }

    public List<String> getAvailableSearchMethods() {
        return Arrays.asList(
                "searchFunctionsByUserHierarchy",
                "searchPointsByFunctionHierarchy",
                "searchFunctionsWithNameFilter",
                "searchFunctionsWithSignatureFilter",
                "searchFunctionsByIdRange",
                "searchPointsByXRange",
                "searchPointsByYRange",
                "searchPointsByCoordinateRange",
                "searchUniqueFunctionsByName",
                "deepSearchFunctions"
        );
    }
}