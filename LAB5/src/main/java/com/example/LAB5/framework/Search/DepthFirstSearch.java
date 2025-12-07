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

public class DepthFirstSearch {
    private static final Logger logger = LoggerFactory.getLogger(DepthFirstSearch.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public DepthFirstSearch(UserRepository userRepository, FunctionRepository functionRepository, PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    public List<Object> search(Object root, FrameworkSearchService.SearchCriteria criteria) {
        logger.info("Starting Depth-First Search");
        List<Object> results = new ArrayList<>();
        Set<Object> visited = new HashSet<>();

        dfs(root, criteria, results, visited, 0);

        logger.info("DFS completed. Found {} results", results.size());
        return results;
    }

    private void dfs(Object node, FrameworkSearchService.SearchCriteria criteria, List<Object> results, Set<Object> visited, int depth) {
        if (node == null || visited.contains(node) || (criteria.maxDepth() > 0 && depth > criteria.maxDepth())) {
            return;
        }

        visited.add(node);

        // Проверяем критерии поиска
        if (matchesCriteria(node, criteria)) {
            results.add(node);
        }

        // Рекурсивно обходим связанные объекты
        for (Object child : getChildren(node, criteria.includeChildren())) {
            dfs(child, criteria, results, visited, depth + 1);
        }
    }

    private boolean matchesCriteria(Object node, FrameworkSearchService.SearchCriteria criteria) {
        if (criteria.name() == null && criteria.type() == null) {
            return true;
        }

        if (node instanceof User) {
            User user = (User) node;
            return (criteria.name() == null || user.getLogin().contains(criteria.name())) &&
                    (criteria.type() == null || "user".equalsIgnoreCase(criteria.type()));
        } else if (node instanceof Function) {
            Function function = (Function) node;
            return (criteria.name() == null || function.getName().contains(criteria.name())) &&
                    (criteria.type() == null || "function".equalsIgnoreCase(criteria.type()));
        } else if (node instanceof Point) {
            Point point = (Point) node;
            return (criteria.name() == null || String.valueOf(point.getX()).contains(criteria.name())) &&
                    (criteria.type() == null || "point".equalsIgnoreCase(criteria.type()));
        }

        return false;
    }

    private List<Object> getChildren(Object node, boolean includeChildren) {
        List<Object> children = new ArrayList<>();

        if (node instanceof User && includeChildren) {
            User user = (User) node;
            children.addAll(user.getFunctions());
        } else if (node instanceof Function && includeChildren) {
            Function function = (Function) node;
            children.addAll(function.getPoints());
        }

        return children;
    }
}