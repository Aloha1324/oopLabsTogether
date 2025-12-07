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

public class HierarchySearch {
    private static final Logger logger = LoggerFactory.getLogger(HierarchySearch.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public HierarchySearch(UserRepository userRepository, FunctionRepository functionRepository, PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    public List<Object> search(Object root, FrameworkSearchService.SearchCriteria criteria) {
        logger.info("Starting Hierarchy Search");
        List<Object> results = new ArrayList<>();

        if (root instanceof User) {
            searchUserHierarchy((User) root, criteria, results, 0);
        } else if (root instanceof Function) {
            searchFunctionHierarchy((Function) root, criteria, results, 0);
        } else if (root instanceof Point) {
            searchPointHierarchy((Point) root, criteria, results, 0);
        }

        logger.info("Hierarchy search completed. Found {} results", results.size());
        return results;
    }

    private void searchUserHierarchy(User user, FrameworkSearchService.SearchCriteria criteria, List<Object> results, int depth) {
        if (depth > criteria.maxDepth() && criteria.maxDepth() > 0) {
            return;
        }

        if (matchesCriteria(user, criteria)) {
            results.add(user);
        }

        if (criteria.includeChildren() && (criteria.maxDepth() <= 0 || depth < criteria.maxDepth())) {
            for (Function function : user.getFunctions()) {
                searchFunctionHierarchy(function, criteria, results, depth + 1);
            }
        }
    }

    private void searchFunctionHierarchy(Function function, FrameworkSearchService.SearchCriteria criteria, List<Object> results, int depth) {
        if (depth > criteria.maxDepth() && criteria.maxDepth() > 0) {
            return;
        }

        if (matchesCriteria(function, criteria)) {
            results.add(function);
        }

        if (criteria.includeChildren() && (criteria.maxDepth() <= 0 || depth < criteria.maxDepth())) {
            for (Point point : function.getPoints()) {
                searchPointHierarchy(point, criteria, results, depth + 1);
            }
        }
    }

    private void searchPointHierarchy(Point point, FrameworkSearchService.SearchCriteria criteria, List<Object> results, int depth) {
        if (depth > criteria.maxDepth() && criteria.maxDepth() > 0) {
            return;
        }

        if (matchesCriteria(point, criteria)) {
            results.add(point);
        }
    }

    private boolean matchesCriteria(Object node, FrameworkSearchService.SearchCriteria criteria) {
        // Такая же реализация как в других классах поиска
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
}