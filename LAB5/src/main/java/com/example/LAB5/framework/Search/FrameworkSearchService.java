package com.example.LAB5.framework.Search;

import com.example.LAB5.framework.repository.UserRepository;
import com.example.LAB5.framework.repository.FunctionRepository;
import com.example.LAB5.framework.repository.PointRepository;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FrameworkSearchService {
    private static final Logger logger = LoggerFactory.getLogger(FrameworkSearchService.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;
    private final DepthFirstSearch depthFirstSearch;
    private final BreadthFirstSearch breadthFirstSearch;
    private final HierarchySearch hierarchySearch;

    public enum SearchType { SINGLE, MULTIPLE, HIERARCHICAL }
    public enum SearchAlgorithm { DEPTH_FIRST, BREADTH_FIRST, HIERARCHY }

    public static class SearchCriteria {
        private final String name;
        private final String type;
        private final boolean includeChildren;
        private final SearchType searchType;
        private final int maxDepth;

        public SearchCriteria(String name, String type, boolean includeChildren,
                              SearchType searchType, int maxDepth) {
            this.name = name;
            this.type = type;
            this.includeChildren = includeChildren;
            this.searchType = searchType;
            this.maxDepth = maxDepth;
        }

        public String name() { return name; }
        public String type() { return type; }
        public boolean includeChildren() { return includeChildren; }
        public SearchType searchType() { return searchType; }
        public int maxDepth() { return maxDepth; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private String type;
            private boolean includeChildren = false;
            private SearchType searchType = SearchType.MULTIPLE;
            private int maxDepth = -1;

            public Builder name(String name) { this.name = name; return this; }
            public Builder type(String type) { this.type = type; return this; }
            public Builder includeChildren(boolean includeChildren) { this.includeChildren = includeChildren; return this; }
            public Builder searchType(SearchType searchType) { this.searchType = searchType; return this; }
            public Builder maxDepth(int maxDepth) { this.maxDepth = maxDepth; return this; }
            public SearchCriteria build() { return new SearchCriteria(name, type, includeChildren, searchType, maxDepth); }
        }
    }

    public FrameworkSearchService(UserRepository userRepository, FunctionRepository functionRepository, PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
        this.depthFirstSearch = new DepthFirstSearch(userRepository, functionRepository, pointRepository);
        this.breadthFirstSearch = new BreadthFirstSearch(userRepository, functionRepository, pointRepository);
        this.hierarchySearch = new HierarchySearch(userRepository, functionRepository, pointRepository);
    }

    public List<Object> search(Object root, SearchCriteria criteria, SearchAlgorithm algorithm) {
        // Валидация входных параметров
        if (criteria == null) {
            logger.error("Search criteria cannot be null");
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        if (root == null) {
            logger.warn("Root object is null, returning empty results");
            return new ArrayList<>();
        }

        // Обработка случая, когда algorithm = null
        SearchAlgorithm actualAlgorithm = algorithm != null ? algorithm : SearchAlgorithm.DEPTH_FIRST;

        logger.info("Starting search with algorithm: {}, type: {}, maxDepth: {}, includeChildren: {}",
                actualAlgorithm, criteria.searchType(), criteria.maxDepth(), criteria.includeChildren());

        List<Object> results;
        switch (actualAlgorithm) {
            case DEPTH_FIRST:
                results = depthFirstSearch.search(root, criteria);
                break;
            case BREADTH_FIRST:
                results = breadthFirstSearch.search(root, criteria);
                break;
            case HIERARCHY:
                results = hierarchySearch.search(root, criteria);
                break;
            default:
                logger.warn("Unknown algorithm: {}, using depth-first", actualAlgorithm);
                results = depthFirstSearch.search(root, criteria);
        }

        // Применяем логику одиночного/множественного поиска
        if (criteria.searchType() == SearchType.SINGLE && !results.isEmpty()) {
            results = List.of(results.get(0));
            logger.info("Single search mode - returning first result from {} total results", results.size());
        }

        logger.info("Search completed. Found {} results", results.size());
        return results;
    }

    public List<User> sortUsers(List<User> users, String field, String order) {
        if (users == null || users.isEmpty()) return users;

        List<User> sorted = new ArrayList<>(users);
        Comparator<User> comparator = getUsersComparator(field);

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        sorted.sort(comparator);
        return sorted;
    }

    private Comparator<User> getUsersComparator(String field) {
        return switch (field.toLowerCase()) {
            case "username", "login" -> Comparator.comparing(User::getUsername);
            case "passwordhash", "role" -> Comparator.comparing(User::getPasswordHash);
            case "id" -> Comparator.comparing(User::getId);
            default -> (u1, u2) -> 0; // нейтральный компаратор для неизвестного поля
        };
    }

    public List<Function> sortFunctions(List<Function> functions, String field, String order) {
        if (functions == null || functions.isEmpty()) return functions;

        List<Function> sorted = new ArrayList<>(functions);
        Comparator<Function> comparator = getFunctionsComparator(field);

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        sorted.sort(comparator);
        return sorted;
    }

    private Comparator<Function> getFunctionsComparator(String field) {
        return switch (field.toLowerCase()) {
            case "name" -> Comparator.comparing(Function::getName);
            case "expression" -> Comparator.comparing(Function::getExpression);
            case "userid" -> Comparator.comparing(f -> getUserId(f));
            case "id" -> Comparator.comparing(Function::getId);
            default -> (f1, f2) -> 0; // нейтральный компаратор для неизвестного поля
        };
    }

    public List<Point> sortPoints(List<Point> points, String field, String order) {
        if (points == null || points.isEmpty()) return points;

        List<Point> sorted = new ArrayList<>(points);
        Comparator<Point> comparator = getPointsComparator(field);

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        sorted.sort(comparator);
        return sorted;
    }

    private Comparator<Point> getPointsComparator(String field) {
        return switch (field.toLowerCase()) {
            case "x", "xvalue" -> Comparator.comparing(Point::getXValue);
            case "y", "yvalue" -> Comparator.comparing(Point::getYValue);
            case "functionid" -> Comparator.comparing(p -> getFunctionId(p));
            case "userid" -> Comparator.comparing(p -> getUserId(p));
            case "id" -> Comparator.comparing(Point::getId);
            default -> (p1, p2) -> 0; // нейтральный компаратор для неизвестного поля
        };
    }

    // Вспомогательные методы для безопасного получения ID
    private Long getUserId(Function function) {
        return function.getUser() != null ? function.getUser().getId() : 0L;
    }

    private Long getFunctionId(Point point) {
        return point.getFunction() != null ? point.getFunction().getId() : 0L;
    }

    private Long getUserId(Point point) {
        return point.getUser() != null ? point.getUser().getId() : 0L;
    }

    // Оптимизированные методы поиска (работают с кешированными данными)
    public List<User> searchUsersByUsername(String pattern) {
        logger.info("Searching users by username pattern: {}", pattern);
        // Получаем все пользователи один раз и фильтруем в памяти
        List<User> allUsers = userRepository.findAll();
        List<User> results = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getUsername().toLowerCase().contains(pattern.toLowerCase())) {
                results.add(user);
            }
        }

        logger.info("Found {} users matching pattern '{}'", results.size(), pattern);
        return results;
    }

    public List<Function> searchFunctionsByName(String pattern) {
        logger.info("Searching functions by name pattern: {}", pattern);
        // Получаем все функции один раз и фильтруем в памяти
        List<Function> allFunctions = functionRepository.findAll();
        List<Function> results = new ArrayList<>();

        for (Function function : allFunctions) {
            if (function.getName().toLowerCase().contains(pattern.toLowerCase())) {
                results.add(function);
            }
        }

        logger.info("Found {} functions matching pattern '{}'", results.size(), pattern);
        return results;
    }

    public List<Point> filterPointsByXGreaterThan(double minX) {
        logger.info("Filtering points where X > {}", minX);
        // Получаем все точки один раз и фильтруем в памяти
        List<Point> allPoints = pointRepository.findAll();
        List<Point> results = new ArrayList<>();

        for (Point point : allPoints) {
            if (point.getXValue() > minX) {
                results.add(point);
            }
        }

        logger.info("Found {} points where X > {}", results.size(), minX);
        return results;
    }

    // Метод для быстрого подсчета
    public long countUsers() {
        return userRepository.count();
    }

    // Метод для множественной сортировки
    public List<User> sortUsersByMultipleFields(List<User> users, Map<String, String> sortFields) {
        if (users == null || users.isEmpty() || sortFields == null || sortFields.isEmpty()) {
            return users;
        }

        List<User> sorted = new ArrayList<>(users);
        Comparator<User> comparator = null;

        for (Map.Entry<String, String> entry : sortFields.entrySet()) {
            String field = entry.getKey();
            String order = entry.getValue();

            Comparator<User> fieldComparator = getUsersComparator(field);

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

    // Метод для тестирования производительности
    public PerformanceMetrics measureSortingPerformance(List<?> items, String operationName, String frameworkType) {
        long startTime = System.nanoTime();

        // Имитация работы сортировки
        if (items != null && !items.isEmpty()) {
            if (items.get(0) instanceof User) {
                @SuppressWarnings("unchecked")
                List<User> users = (List<User>) items;
                sortUsers(users, "username", "asc");
            } else if (items.get(0) instanceof Function) {
                @SuppressWarnings("unchecked")
                List<Function> functions = (List<Function>) items;
                sortFunctions(functions, "name", "asc");
            } else if (items.get(0) instanceof Point) {
                @SuppressWarnings("unchecked")
                List<Point> points = (List<Point>) items;
                sortPoints(points, "x", "asc");
            }
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        return new PerformanceMetrics(operationName, durationMs, items != null ? items.size() : 0, frameworkType);
    }

    // Класс для метрик производительности
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

        public String getOperationName() { return operationName; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public int getRecordsProcessed() { return recordsProcessed; }
        public String getFrameworkType() { return frameworkType; }

        public double getRecordsPerSecond() {
            return executionTimeMs > 0 ? (recordsProcessed * 1000.0) / executionTimeMs : 0;
        }

        @Override
        public String toString() {
            return String.format("%s: %d records in %d ms (%.0f records/sec) [%s]",
                    operationName, recordsProcessed, executionTimeMs, getRecordsPerSecond(), frameworkType);
        }
    }
}