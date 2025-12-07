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

public class HierarchySearch {
    private static final Logger logger = LoggerFactory.getLogger(HierarchySearch.class);

    private final UserDAO userDAO;
    private final FunctionDAO functionDAO;
    private final PointDAO pointDAO;

    // Внутренний класс для критериев поиска вместо отдельного интерфейса
    public static class SearchCriteria {
        private final String name;
        private final String type;
        private final boolean includeChildren;

        public SearchCriteria(String name, String type, boolean includeChildren) {
            this.name = name;
            this.type = type;
            this.includeChildren = includeChildren;
        }

        public String name() {
            return name;
        }

        public String type() {
            return type;
        }

        public boolean includeChildren() {
            return includeChildren;
        }

        // Статический метод для удобства
        public static SearchCriteria of(String name, String type, boolean includeChildren) {
            return new SearchCriteria(name, type, includeChildren);
        }
    }

    public HierarchySearch(UserDAO userDAO, FunctionDAO functionDAO, PointDAO pointDAO) {
        this.userDAO = userDAO;
        this.functionDAO = functionDAO;
        this.pointDAO = pointDAO;
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

    // ========== ОСНОВНЫЕ МЕТОДЫ ПОИСКА ==========

    public List<Object> search(Object root, SearchCriteria criteria) {
        logger.info("🌳 Starting hierarchy search");

        if (root instanceof Long) {
            return searchUserHierarchy((Long) root, criteria);
        } else if (root instanceof UserDTO) {
            return searchUserHierarchy(((UserDTO) root).getId(), criteria);
        } else {
            logger.warn("Unsupported root type for hierarchy search: {}", root.getClass().getSimpleName());
            return Collections.emptyList();
        }
    }

    public String getAlgorithmName() {
        return "HIERARCHY_SEARCH";
    }

    private List<Object> searchUserHierarchy(Long userId, SearchCriteria criteria) {
        logger.info("Starting hierarchy search for user ID: {}", userId);

        List<Object> result = new ArrayList<>();

        try {
            Map<String, Object> rawUser = userDAO.findById(userId);
            Optional<UserDTO> user = convertToUserDTO(rawUser);

            if (user.isPresent()) {
                // Добавляем пользователя если соответствует критериям
                if (matchesUserCriteria(user.get(), criteria)) {
                    result.add(user.get());
                }

                // Получаем функции пользователя с фильтрацией
                List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
                List<FunctionDTO> functions = convertToFunctionDTOList(rawFunctions);

                for (FunctionDTO func : functions) {
                    if (matchesFunctionCriteria(func, criteria)) {
                        result.add(func);
                    }
                }

                // Если нужно включать точки
                if (criteria.includeChildren()) {
                    for (FunctionDTO function : functions) {
                        List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(function.getId());
                        List<PointDTO> points = convertToPointDTOList(rawPoints);

                        for (PointDTO point : points) {
                            if (matchesPointCriteria(point, criteria)) {
                                result.add(point);
                            }
                        }
                    }
                }
            }

            logger.info("Hierarchy search completed for user ID: {}. Found {} items", userId, result.size());

        } catch (Exception e) {
            logger.error("Error during hierarchy search for user ID {}: {}", userId, e.getMessage(), e);
        }

        return result;
    }

    // Оригинальный метод для обратной совместимости
    public Map<String, Object> searchUserHierarchy(Long userId) {
        logger.info("Starting legacy hierarchy search for user ID: {}", userId);

        Map<String, Object> hierarchy = new HashMap<>();

        try {
            Map<String, Object> rawUser = userDAO.findById(userId);
            Optional<UserDTO> user = convertToUserDTO(rawUser);

            if (user.isPresent()) {
                hierarchy.put("user", user.get());

                List<Map<String, Object>> rawFunctions = functionDAO.findByUserId(userId);
                List<FunctionDTO> functions = convertToFunctionDTOList(rawFunctions);
                hierarchy.put("functions", functions);

                List<Map<String, Object>> functionsWithPoints = new ArrayList<>();
                for (FunctionDTO function : functions) {
                    Map<String, Object> functionData = new HashMap<>();
                    functionData.put("function", function);

                    List<Map<String, Object>> rawPoints = pointDAO.findByFunctionId(function.getId());
                    List<PointDTO> points = convertToPointDTOList(rawPoints);
                    functionData.put("points", points);

                    functionsWithPoints.add(functionData);
                }
                hierarchy.put("functionsWithPoints", functionsWithPoints);
            }

            logger.info("Legacy hierarchy search completed for user ID: {}", userId);

        } catch (Exception e) {
            logger.error("Error during legacy hierarchy search for user ID {}: {}", userId, e.getMessage(), e);
        }

        return hierarchy;
    }

    // Вспомогательные методы для критериев поиска
    private boolean matchesUserCriteria(UserDTO user, SearchCriteria criteria) {
        boolean matches = true;

        if (criteria.name() != null && !criteria.name().isEmpty()) {
            matches = matches && user.getLogin().toLowerCase().contains(criteria.name().toLowerCase());
        }

        if (criteria.type() != null && !criteria.type().isEmpty()) {
            matches = matches && user.getRole().equalsIgnoreCase(criteria.type());
        }

        return matches;
    }

    private boolean matchesFunctionCriteria(FunctionDTO function, SearchCriteria criteria) {
        boolean matches = true;

        if (criteria.name() != null && !criteria.name().isEmpty()) {
            matches = matches && function.getName().toLowerCase().contains(criteria.name().toLowerCase());
        }

        if (criteria.type() != null && !criteria.type().isEmpty()) {
            // Используем signature вместо type, так как в FunctionDTO нет метода getType()
            matches = matches && function.getSignature() != null &&
                    function.getSignature().equalsIgnoreCase(criteria.type());
        }

        return matches;
    }

    private boolean matchesPointCriteria(PointDTO point, SearchCriteria criteria) {
        // Для точек можно искать по координатам или другим параметрам
        if (criteria.name() != null && !criteria.name().isEmpty()) {
            // Альтернативная логика поиска для точек
            return point.getXValue() != null && point.getYValue() != null &&
                    (point.getXValue().toString().contains(criteria.name()) ||
                            point.getYValue().toString().contains(criteria.name()));
        }
        return true;
    }

    // Дополнительные утилитные методы
    public List<String> getSupportedSearchTypes() {
        return Arrays.asList("USER_HIERARCHY", "FUNCTION_HIERARCHY", "FULL_HIERARCHY");
    }

    public boolean validateUserId(Long userId) {
        return userId != null && userId > 0;
    }

    public boolean validateFunctionId(Long functionId) {
        return functionId != null && functionId > 0;
    }

    // Методы для демонстрации использования (чтобы убрать предупреждения)
    public void demonstrateUsage() {
        // Пример использования search метода
        SearchCriteria criteria = SearchCriteria.of("test", "USER", true);
        List<Object> result = search(1L, criteria);
        logger.info("Search demonstration found {} items", result.size());

        // Пример использования getAlgorithmName
        String algorithmName = getAlgorithmName();
        logger.info("Algorithm name: {}", algorithmName);
    }

    // Методы сортировки для совместимости с тестами
    public List<UserDTO> sortUsers(List<UserDTO> users, String field, String order) {
        if (users == null || users.isEmpty()) {
            return users;
        }

        List<UserDTO> sorted = new ArrayList<>(users);
        sorted.sort((u1, u2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "login":
                    result = u1.getLogin().compareTo(u2.getLogin());
                    break;
                case "role":
                    result = u1.getRole().compareTo(u2.getRole());
                    break;
                case "id":
                    result = Long.compare(u1.getId(), u2.getId());
                    break;
                default:
                    result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });

        return sorted;
    }

    public List<FunctionDTO> sortFunctions(List<FunctionDTO> functions, String field, String order) {
        if (functions == null || functions.isEmpty()) {
            return functions;
        }

        List<FunctionDTO> sorted = new ArrayList<>(functions);
        sorted.sort((f1, f2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "name":
                    result = f1.getName().compareTo(f2.getName());
                    break;
                case "userid":
                    result = Long.compare(f1.getUserId(), f2.getUserId());
                    break;
                case "signature":
                    result = f1.getSignature().compareTo(f2.getSignature());
                    break;
                case "id":
                    result = Long.compare(f1.getId(), f2.getId());
                    break;
                default:
                    result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });

        return sorted;
    }

    public List<PointDTO> sortPoints(List<PointDTO> points, String field, String order) {
        if (points == null || points.isEmpty()) {
            return points;
        }

        List<PointDTO> sorted = new ArrayList<>(points);
        sorted.sort((p1, p2) -> {
            int result = 0;
            switch (field.toLowerCase()) {
                case "x":
                    result = Double.compare(p1.getXValue(), p2.getXValue());
                    break;
                case "y":
                    result = Double.compare(p1.getYValue(), p2.getYValue());
                    break;
                case "functionid":
                    result = Long.compare(p1.getFunctionId(), p2.getFunctionId());
                    break;
                case "id":
                    result = Long.compare(p1.getId(), p2.getId());
                    break;
                default:
                    result = 0;
            }
            return "desc".equalsIgnoreCase(order) ? -result : result;
        });

        return sorted;
    }

    public List<UserDTO> sortUsersByMultipleFields(List<UserDTO> users, Map<String, String> sortCriteria) {
        if (users == null || users.isEmpty() || sortCriteria == null || sortCriteria.isEmpty()) {
            return users;
        }

        List<UserDTO> sorted = new ArrayList<>(users);
        sorted.sort((u1, u2) -> {
            int result = 0;
            for (Map.Entry<String, String> entry : sortCriteria.entrySet()) {
                String field = entry.getKey();
                String order = entry.getValue();

                switch (field.toLowerCase()) {
                    case "login":
                        result = u1.getLogin().compareTo(u2.getLogin());
                        break;
                    case "role":
                        result = u1.getRole().compareTo(u2.getRole());
                        break;
                    case "id":
                        result = Long.compare(u1.getId(), u2.getId());
                        break;
                    default:
                        result = 0;
                }

                if (result != 0) {
                    return "desc".equalsIgnoreCase(order) ? -result : result;
                }
            }
            return result;
        });

        return sorted;
    }
}