package com.example.LAB5.manual.Search;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DAO.UserDAO;
import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import com.example.LAB5.manual.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final UserDAO userDAO;
    private final FunctionDAO functionDAO;
    private final PointDAO pointDAO;
    private final DepthFirstSearch dfs;
    private final BreadthFirstSearch bfs;
    private final HierarchySearch hierarchySearch;

    public SearchService(UserDAO userDAO, FunctionDAO functionDAO, PointDAO pointDAO) {
        this.userDAO = userDAO;
        this.functionDAO = functionDAO;
        this.pointDAO = pointDAO;
        this.dfs = new DepthFirstSearch(functionDAO, pointDAO);
        this.bfs = new BreadthFirstSearch(functionDAO, pointDAO);
        this.hierarchySearch = new HierarchySearch(userDAO, functionDAO, pointDAO);
    }

    // ========== МЕТОДЫ КОНВЕРТАЦИИ ==========

    private Optional<UserDTO> convertToUserDTO(Map<String, Object> map) {
        if (map == null) return Optional.empty();

        try {
            UserDTO user = new UserDTO(
                    map.get("id") != null ? ((Number) map.get("id")).longValue() : null,
                    (String) map.get("login"),
                    (String) map.get("password"),
                    (String) map.get("role")
            );
            return Optional.of(user);
        } catch (Exception e) {
            logger.error("Error converting Map to UserDTO: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<FunctionDTO> convertToFunctionDTO(Map<String, Object> map) {
        if (map == null) return Optional.empty();

        try {
            FunctionDTO function = new FunctionDTO(
                    map.get("id") != null ? ((Number) map.get("id")).longValue() : null,
                    map.get("user_id") != null ? ((Number) map.get("user_id")).longValue() : null,
                    (String) map.get("name"),
                    (String) map.get("signature")
            );
            return Optional.of(function);
        } catch (Exception e) {
            logger.error("Error converting Map to FunctionDTO: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<PointDTO> convertToPointDTO(Map<String, Object> map) {
        if (map == null) return Optional.empty();

        try {
            PointDTO point = new PointDTO(
                    map.get("id") != null ? ((Number) map.get("id")).longValue() : null,
                    map.get("function_id") != null ? ((Number) map.get("function_id")).longValue() : null,
                    map.get("x_value") != null ? ((Number) map.get("x_value")).doubleValue() : null,
                    map.get("y_value") != null ? ((Number) map.get("y_value")).doubleValue() : null
            );
            return Optional.of(point);
        } catch (Exception e) {
            logger.error("Error converting Map to PointDTO: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private List<FunctionDTO> convertToFunctionDTOList(List<Map<String, Object>> rawData) {
        if (rawData == null) return new ArrayList<>();

        return rawData.stream()
                .map(this::convertToFunctionDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<PointDTO> convertToPointDTOList(List<Map<String, Object>> rawData) {
        if (rawData == null) return new ArrayList<>();

        return rawData.stream()
                .map(this::convertToPointDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<UserDTO> convertToUserDTOList(List<Map<String, Object>> rawData) {
        if (rawData == null) return new ArrayList<>();

        return rawData.stream()
                .map(this::convertToUserDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // ========== ОДИНОЧНЫЙ ПОИСК ==========

    public Optional<UserDTO> findUserById(Long id) {
        logger.info("Single search: finding user by ID: {}", id);
        Map<String, Object> rawUser = userDAO.findById(id);
        return convertToUserDTO(rawUser);
    }

    public Optional<FunctionDTO> findFunctionById(Long id) {
        logger.info("Single search: finding function by ID: {}", id);
        Map<String, Object> rawFunction = functionDAO.findById(id);
        return convertToFunctionDTO(rawFunction);
    }

    public Optional<PointDTO> findPointById(Long id) {
        logger.info("Single search: finding point by ID: {}", id);
        Map<String, Object> rawPoint = pointDAO.findById(id);
        return convertToPointDTO(rawPoint);
    }

    // ========== МНОЖЕСТВЕННЫЙ ПОИСК ==========

    public List<UserDTO> findUsersByRole(String role) {
        logger.info("Multiple search: finding users by role: {}", role);
        List<Map<String, Object>> rawUsers = userDAO.findByRole(role);
        return convertToUserDTOList(rawUsers);
    }

    public List<FunctionDTO> findFunctionsByName(String name) {
        logger.info("Multiple search: finding functions by name: {}", name);
        List<Map<String, Object>> rawFunctions = functionDAO.findByName(name);
        return convertToFunctionDTOList(rawFunctions);
    }

    public List<PointDTO> findPointsByFunctionId(Long functionId) {
        logger.info("Multiple search: finding points by function ID: {}", functionId);
        List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(functionId);
        return convertToPointDTOList(rawPoints);
    }

    public List<PointDTO> findPointsByXRange(Long functionId, Double minX, Double maxX) {
        logger.info("Multiple search: finding points by X range [{}, {}] for function ID: {}", minX, maxX, functionId);
        List<Map<String, Object>> rawPoints = pointDAO.findByXRange(functionId, minX, maxX);
        return convertToPointDTOList(rawPoints);
    }

    public List<PointDTO> findPointsByYRange(Long functionId, Double minY, Double maxY) {
        logger.info("Multiple search: finding points by Y range [{}, {}] for function ID: {}", minY, maxY, functionId);
        List<Map<String, Object>> rawPoints = pointDAO.findByYRange(functionId, minY, maxY);
        return convertToPointDTOList(rawPoints);
    }

    public List<FunctionDTO> findFunctionsByUserId(Long userId) {
        logger.info("Multiple search: finding functions by user ID: {}", userId);
        List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
        return convertToFunctionDTOList(rawFunctions);
    }

    // ========== ПОИСК С СОРТИРОВКОЙ ==========

    public List<UserDTO> findAllUsersSorted(String sortBy, String direction) {
        logger.info("Sorted search: finding all users sorted by {} in {} direction", sortBy, direction);
        List<Map<String, Object>> rawUsers = userDAO.findAll();
        List<UserDTO> users = convertToUserDTOList(rawUsers);
        return sortUsers(users, sortBy, direction);
    }

    public List<FunctionDTO> findFunctionsByUserSorted(Long userId, String sortBy, String direction) {
        logger.info("Sorted search: finding functions for user {} sorted by {} in {} direction",
                userId, sortBy, direction);
        List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
        List<FunctionDTO> functions = convertToFunctionDTOList(rawFunctions);
        return sortFunctions(functions, sortBy, direction);
    }

    public List<PointDTO> findPointsByFunctionSorted(Long functionId, String sortBy, String direction) {
        logger.info("Sorted search: finding points for function {} sorted by {} in {} direction",
                functionId, sortBy, direction);
        List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(functionId);
        List<PointDTO> points = convertToPointDTOList(rawPoints);
        return sortPoints(points, sortBy, direction);
    }

    public List<UserDTO> findAllUsersSortedByMultipleFields(Map<String, String> sortCriteria) {
        logger.info("Multi-field sorted search: finding all users sorted by {}", sortCriteria);
        List<Map<String, Object>> rawUsers = userDAO.findAll();
        List<UserDTO> users = convertToUserDTOList(rawUsers);
        return sortUsersByMultipleFields(users, sortCriteria);
    }

    public List<FunctionDTO> findAllFunctionsSortedByMultipleFields(Map<String, String> sortCriteria) {
        logger.info("Multi-field sorted search: finding all functions sorted by {}", sortCriteria);
        List<Map<String, Object>> rawFunctions = functionDAO.findAll();
        List<FunctionDTO> functions = convertToFunctionDTOList(rawFunctions);
        return sortFunctionsByMultipleFields(functions, sortCriteria);
    }

    // ========== ПОИСК В ГЛУБИНУ (DFS) ==========

    public List<FunctionDTO> dfsSearchUserFunctions(Long userId) {
        logger.info("DFS search: finding all functions in user hierarchy starting from user ID: {}", userId);
        return dfs.searchFunctionsByUserHierarchy(userId, new HashSet<>());
    }

    public List<PointDTO> dfsSearchFunctionPoints(Long functionId) {
        logger.info("DFS search: finding all points in function hierarchy starting from function ID: {}", functionId);
        return dfs.searchPointsByFunctionHierarchy(functionId, new HashSet<>());
    }

    // ========== ПОИСК В ШИРИНУ (BFS) ==========

    public List<FunctionDTO> bfsSearchUserFunctions(Long userId) {
        logger.info("BFS search: finding functions using BFS starting from user ID: {}", userId);
        return bfs.searchFunctionsByUserBFS(userId);
    }

    public List<PointDTO> bfsSearchFunctionPoints(FunctionDTO function) {
        logger.info("BFS search: finding points using BFS starting from function: {}", function.getId());
        return bfs.searchPointsByFunctionBFS(function);
    }

    public List<PointDTO> bfsSearchPointsInRange(Long functionId, double minX, double maxX, double minY, double maxY) {
        logger.info("BFS search: finding points in range using BFS for function ID: {}", functionId);
        return bfs.searchPointsInRangeBFS(functionId, minX, maxX, minY, maxY);
    }

    // ========== ПОИСК ПО ИЕРАРХИИ ==========

    public Map<String, Object> searchUserFullHierarchy(Long userId) {
        logger.info("Hierarchy search: full hierarchy for user ID: {}", userId);
        return hierarchySearch.searchUserHierarchy(userId);
    }

    // ========== КОМБИНИРОВАННЫЙ ПОИСК ==========

    public List<PointDTO> advancedSearch(Map<String, Object> searchCriteria) {
        logger.info("Advanced search with criteria: {}", searchCriteria);

        Long functionId = (Long) searchCriteria.get("functionId");
        Double minX = (Double) searchCriteria.get("minX");
        Double maxX = (Double) searchCriteria.get("maxX");
        Double minY = (Double) searchCriteria.get("minY");
        Double maxY = (Double) searchCriteria.get("maxY");
        String sortBy = (String) searchCriteria.get("sortBy");
        String direction = (String) searchCriteria.get("direction");

        List<PointDTO> results = new ArrayList<>();

        if (functionId != null) {
            List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(functionId);
            List<PointDTO> allPoints = convertToPointDTOList(rawPoints);

            if (minX != null && maxX != null && minY != null && maxY != null) {
                // Поиск по полному диапазону X и Y
                results = allPoints.stream()
                        .filter(p -> p.getXValue() != null && p.getYValue() != null &&
                                p.getXValue() >= minX && p.getXValue() <= maxX &&
                                p.getYValue() >= minY && p.getYValue() <= maxY)
                        .collect(Collectors.toList());
                logger.debug("Filtered {} points by full range", results.size());
            } else if (minX != null && maxX != null) {
                // Поиск только по диапазону X
                results = allPoints.stream()
                        .filter(p -> p.getXValue() != null &&
                                p.getXValue() >= minX && p.getXValue() <= maxX)
                        .collect(Collectors.toList());
            } else if (minY != null && maxY != null) {
                // Поиск только по диапазону Y
                results = allPoints.stream()
                        .filter(p -> p.getYValue() != null &&
                                p.getYValue() >= minY && p.getYValue() <= maxY)
                        .collect(Collectors.toList());
            } else {
                // Все точки функции
                results = allPoints;
            }
        }

        if (sortBy != null && direction != null && !results.isEmpty()) {
            results = sortPoints(results, sortBy, direction);
        }

        logger.info("Advanced search completed, found {} results", results.size());
        return results;
    }

    public List<FunctionDTO> advancedFunctionSearch(Map<String, Object> searchCriteria) {
        logger.info("Advanced function search with criteria: {}", searchCriteria);

        Long userId = (Long) searchCriteria.get("userId");
        String namePattern = (String) searchCriteria.get("name");
        String signaturePattern = (String) searchCriteria.get("signature");
        String sortBy = (String) searchCriteria.get("sortBy");
        String direction = (String) searchCriteria.get("direction");

        List<FunctionDTO> results = new ArrayList<>();

        if (userId != null) {
            List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
            results = convertToFunctionDTOList(rawFunctions);
            logger.debug("Found {} functions for user ID {}", results.size(), userId);
        } else {
            List<Map<String, Object>> rawFunctions = functionDAO.findAll();
            results = convertToFunctionDTOList(rawFunctions);
            logger.debug("Found all {} functions", results.size());
        }

        // Применяем фильтры по имени и сигнатуре
        if (namePattern != null && !namePattern.trim().isEmpty()) {
            results = results.stream()
                    .filter(func -> func.getName() != null &&
                            func.getName().toLowerCase().contains(namePattern.toLowerCase()))
                    .collect(Collectors.toList());
            logger.debug("Filtered to {} functions by name pattern '{}'", results.size(), namePattern);
        }

        if (signaturePattern != null && !signaturePattern.trim().isEmpty()) {
            results = results.stream()
                    .filter(func -> func.getSignature() != null &&
                            func.getSignature().toLowerCase().contains(signaturePattern.toLowerCase()))
                    .collect(Collectors.toList());
            logger.debug("Filtered to {} functions by signature pattern '{}'", results.size(), signaturePattern);
        }

        if (sortBy != null && direction != null && !results.isEmpty()) {
            results = sortFunctions(results, sortBy, direction);
        }

        logger.info("Advanced function search completed, found {} results", results.size());
        return results;
    }

    // ========== МЕТОДЫ СОРТИРОВКИ (остаются без изменений) ==========

    public List<UserDTO> sortUsers(List<UserDTO> users, String sortBy, String direction) {
        logger.info("Sorting {} users by field: '{}' in {} order", users.size(), sortBy, direction);

        if (users == null || users.isEmpty()) {
            logger.warn("Attempted to sort empty or null users list");
            return users;
        }

        Comparator<UserDTO> comparator = getFieldComparatorForUser(sortBy);

        // Применяем направление сортировки
        if ("desc".equalsIgnoreCase(direction) || "descending".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
            logger.debug("Applied descending order");
        } else {
            logger.debug("Applied ascending order");
        }

        List<UserDTO> sortedUsers = users.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        logger.info("Successfully sorted {} users", sortedUsers.size());
        return sortedUsers;
    }

    public List<FunctionDTO> sortFunctions(List<FunctionDTO> functions, String sortBy, String direction) {
        logger.info("Sorting {} functions by field: '{}' in {} order", functions.size(), sortBy, direction);

        if (functions == null || functions.isEmpty()) {
            logger.warn("Attempted to sort empty or null functions list");
            return functions;
        }

        Comparator<FunctionDTO> comparator = getFieldComparatorForFunction(sortBy);

        // Применяем направление сортировки
        if ("desc".equalsIgnoreCase(direction) || "descending".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
            logger.debug("Applied descending order");
        } else {
            logger.debug("Applied ascending order");
        }

        List<FunctionDTO> sortedFunctions = functions.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        logger.info("Successfully sorted {} functions", sortedFunctions.size());
        return sortedFunctions;
    }

    public List<PointDTO> sortPoints(List<PointDTO> points, String sortBy, String direction) {
        logger.info("Sorting {} points by field: '{}' in {} order", points.size(), sortBy, direction);

        if (points == null || points.isEmpty()) {
            logger.warn("Attempted to sort empty or null points list");
            return points;
        }

        Comparator<PointDTO> comparator = getFieldComparatorForPoint(sortBy);

        // Применяем направление сортировки
        if ("desc".equalsIgnoreCase(direction) || "descending".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
            logger.debug("Applied descending order");
        } else {
            logger.debug("Applied ascending order");
        }

        List<PointDTO> sortedPoints = points.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        logger.info("Successfully sorted {} points", sortedPoints.size());
        return sortedPoints;
    }

    public List<UserDTO> sortUsersByMultipleFields(List<UserDTO> users, Map<String, String> sortCriteria) {
        logger.info("Sorting {} users by multiple fields: {}", users.size(), sortCriteria);

        if (users == null || users.isEmpty() || sortCriteria == null || sortCriteria.isEmpty()) {
            return users;
        }

        Comparator<UserDTO> comparator = null;

        for (Map.Entry<String, String> entry : sortCriteria.entrySet()) {
            String field = entry.getKey();
            String dir = entry.getValue();

            Comparator<UserDTO> fieldComparator = getFieldComparatorForUser(field);
            if ("desc".equalsIgnoreCase(dir)) {
                fieldComparator = fieldComparator.reversed();
            }

            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }

        List<UserDTO> sortedUsers = users.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        logger.info("Multi-field sorting completed for {} users", sortedUsers.size());
        return sortedUsers;
    }

    public List<FunctionDTO> sortFunctionsByMultipleFields(List<FunctionDTO> functions, Map<String, String> sortCriteria) {
        logger.info("Sorting {} functions by multiple fields: {}", functions.size(), sortCriteria);

        if (functions == null || functions.isEmpty() || sortCriteria == null || sortCriteria.isEmpty()) {
            return functions;
        }

        Comparator<FunctionDTO> comparator = null;

        for (Map.Entry<String, String> entry : sortCriteria.entrySet()) {
            String field = entry.getKey();
            String dir = entry.getValue();

            Comparator<FunctionDTO> fieldComparator = getFieldComparatorForFunction(field);
            if ("desc".equalsIgnoreCase(dir)) {
                fieldComparator = fieldComparator.reversed();
            }

            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }

        List<FunctionDTO> sortedFunctions = functions.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        logger.info("Multi-field sorting completed for {} functions", sortedFunctions.size());
        return sortedFunctions;
    }

    public List<PointDTO> sortPointsByMultipleFields(List<PointDTO> points, Map<String, String> sortCriteria) {
        logger.info("Sorting {} points by multiple fields: {}", points.size(), sortCriteria);

        if (points == null || points.isEmpty() || sortCriteria == null || sortCriteria.isEmpty()) {
            return points;
        }

        Comparator<PointDTO> comparator = null;

        for (Map.Entry<String, String> entry : sortCriteria.entrySet()) {
            String field = entry.getKey();
            String dir = entry.getValue();

            Comparator<PointDTO> fieldComparator = getFieldComparatorForPoint(field);
            if ("desc".equalsIgnoreCase(dir)) {
                fieldComparator = fieldComparator.reversed();
            }

            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }

        List<PointDTO> sortedPoints = points.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        logger.info("Multi-field sorting completed for {} points", sortedPoints.size());
        return sortedPoints;
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ КОМПАРАТОРОВ ==========

    private Comparator<UserDTO> getFieldComparatorForUser(String field) {
        switch (field.toLowerCase()) {
            case "id":
                return Comparator.comparing(UserDTO::getId, Comparator.nullsLast(Long::compareTo));
            case "login":
                return Comparator.comparing(UserDTO::getLogin, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "role":
                return Comparator.comparing(UserDTO::getRole, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "password":
                return Comparator.comparing(UserDTO::getPassword, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default:
                logger.warn("Unknown user sort field '{}', defaulting to ID", field);
                return Comparator.comparing(UserDTO::getId, Comparator.nullsLast(Long::compareTo));
        }
    }

    private Comparator<FunctionDTO> getFieldComparatorForFunction(String field) {
        switch (field.toLowerCase()) {
            case "id":
                return Comparator.comparing(FunctionDTO::getId, Comparator.nullsLast(Long::compareTo));
            case "name":
                return Comparator.comparing(FunctionDTO::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "signature":
                return Comparator.comparing(FunctionDTO::getSignature, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "userid":
            case "user_id":
            case "u_id":
                return Comparator.comparing(FunctionDTO::getUserId, Comparator.nullsLast(Long::compareTo));
            default:
                logger.warn("Unknown function sort field '{}', defaulting to ID", field);
                return Comparator.comparing(FunctionDTO::getId, Comparator.nullsLast(Long::compareTo));
        }
    }

    private Comparator<PointDTO> getFieldComparatorForPoint(String field) {
        switch (field.toLowerCase()) {
            case "id":
                return Comparator.comparing(PointDTO::getId, Comparator.nullsLast(Long::compareTo));
            case "x":
            case "xvalue":
            case "x_value":
                return Comparator.comparing(PointDTO::getXValue, Comparator.nullsLast(Double::compareTo));
            case "y":
            case "yvalue":
            case "y_value":
                return Comparator.comparing(PointDTO::getYValue, Comparator.nullsLast(Double::compareTo));
            case "functionid":
            case "function_id":
            case "f_id":
                return Comparator.comparing(PointDTO::getFunctionId, Comparator.nullsLast(Long::compareTo));
            default:
                logger.warn("Unknown point sort field '{}', defaulting to ID", field);
                return Comparator.comparing(PointDTO::getId, Comparator.nullsLast(Long::compareTo));
        }
    }

    // ========== СТАТИСТИЧЕСКИЕ МЕТОДЫ ==========

    public Map<String, Object> getSearchStatistics() {
        logger.info("Generating search statistics");

        Map<String, Object> stats = new HashMap<>();

        try {
            List<Map<String, Object>> rawUsers = userDAO.findAll();
            List<Map<String, Object>> rawFunctions = functionDAO.findAll();
            List<Map<String, Object>> rawPoints = pointDAO.findAll();

            int userCount = rawUsers.size();
            int functionCount = rawFunctions.size();
            int pointCount = rawPoints.size();

            stats.put("totalUsers", userCount);
            stats.put("totalFunctions", functionCount);
            stats.put("totalPoints", pointCount);
            stats.put("timestamp", new Date());
            stats.put("availableAlgorithms", Arrays.asList("DFS", "BFS", "HIERARCHY"));

            logger.info("Search statistics generated: {} users, {} functions, {} points",
                    userCount, functionCount, pointCount);

        } catch (Exception e) {
            logger.error("Error generating search statistics: {}", e.getMessage(), e);
            stats.put("error", "Failed to generate statistics");
        }

        return stats;
    }

    // ========== УТИЛИТНЫЕ МЕТОДЫ ==========

    public List<String> getAvailableAlgorithms() {
        return Arrays.asList("DFS", "BFS", "HIERARCHY");
    }

    public boolean isAlgorithmAvailable(String algorithm) {
        return getAvailableAlgorithms().contains(algorithm.toUpperCase());
    }
}