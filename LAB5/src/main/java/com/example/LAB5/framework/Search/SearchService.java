package com.example.LAB5.framework.Search;

import com.example.LAB5.framework.repository.UserRepository;
import com.example.LAB5.framework.repository.FunctionRepository;
import com.example.LAB5.framework.repository.PointRepository;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

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

    public SearchService(UserRepository userRepository, FunctionRepository functionRepository, PointRepository pointRepository) {
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
        sorted.sort((u1, u2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "login": result = u1.getLogin().compareTo(u2.getLogin()); break;
                case "role": result = u1.getRole().compareTo(u2.getRole()); break;
                case "id": result = Long.compare(u1.getId(), u2.getId()); break;
                default: result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });
        return sorted;
    }

    public List<Function> sortFunctions(List<Function> functions, String field, String order) {
        if (functions == null || functions.isEmpty()) return functions;

        List<Function> sorted = new ArrayList<>(functions);
        sorted.sort((f1, f2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "name": result = f1.getName().compareTo(f2.getName()); break;
                case "userid": result = getUserId(f1).compareTo(getUserId(f2)); break;
                case "signature": result = f1.getSignature().compareTo(f2.getSignature()); break;
                case "id": result = Long.compare(f1.getId(), f2.getId()); break;
                default: result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });
        return sorted;
    }

    public List<Point> sortPoints(List<Point> points, String field, String order) {
        if (points == null || points.isEmpty()) return points;

        List<Point> sorted = new ArrayList<>(points);
        sorted.sort((p1, p2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "x": result = Double.compare(p1.getX(), p2.getX()); break;
                case "y": result = Double.compare(p1.getY(), p2.getY()); break;
                case "functionid": result = getFunctionId(p1).compareTo(getFunctionId(p2)); break;
                case "id": result = Long.compare(p1.getId(), p2.getId()); break;
                default: result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });
        return sorted;
    }

    // Вспомогательные методы для безопасного получения ID
    private Long getUserId(Function function) {
        return function.getUser() != null ? function.getUser().getId() : 0L;
    }

    private Long getFunctionId(Point point) {
        return point.getFunction() != null ? point.getFunction().getId() : 0L;
    }
}