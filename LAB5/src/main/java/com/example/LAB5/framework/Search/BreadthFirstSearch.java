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

public class BreadthFirstSearch {
    private static final Logger logger = LoggerFactory.getLogger(BreadthFirstSearch.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public BreadthFirstSearch(UserRepository userRepository, FunctionRepository functionRepository, PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    public List<Object> search(Object root, FrameworkSearchService.SearchCriteria criteria) {
        logger.info("Starting Breadth-First Search");
        List<Object> results = new ArrayList<>();
        Set<Object> visited = new HashSet<>();
        Queue<Object> queue = new LinkedList<>();

        if (root != null) {
            queue.offer(root);
            visited.add(root);
        }

        int currentDepth = 0;
        int nodesAtCurrentLevel = queue.size();

        while (!queue.isEmpty()) {
            if (criteria.maxDepth() > 0 && currentDepth > criteria.maxDepth()) {
                break;
            }

            Object node = queue.poll();
            nodesAtCurrentLevel--;

            // Проверяем критерии поиска
            if (matchesCriteria(node, criteria)) {
                results.add(node);
            }

            // Добавляем детей в очередь
            for (Object child : getChildren(node, criteria.includeChildren())) {
                if (!visited.contains(child)) {
                    visited.add(child);
                    queue.offer(child);
                }
            }

            if (nodesAtCurrentLevel == 0) {
                currentDepth++;
                nodesAtCurrentLevel = queue.size();
            }
        }

        logger.info("BFS completed. Found {} results", results.size());
        return results;
    }

    private boolean matchesCriteria(Object node, FrameworkSearchService.SearchCriteria criteria) {
        // Такая же реализация как в DepthFirstSearch
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