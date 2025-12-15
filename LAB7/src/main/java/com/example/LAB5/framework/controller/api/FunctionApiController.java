package com.example.LAB5.framework.controller.api;

import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/functions")
public class FunctionApiController {

    @Autowired
    private FunctionService functionService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getUserFunctions(Authentication authentication) {
        try {
            String username = authentication.getName();
            var user = userService.getUserByUsernameOrNull(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден"));
            }

            var functions = functionService.getFunctionsByUserId(user.getId());
            List<Map<String, Object>> response = new ArrayList<>();

            for (var func : functions) {
                Map<String, Object> funcMap = new HashMap<>();
                funcMap.put("id", func.getId());
                funcMap.put("name", func.getName());
                funcMap.put("expression", func.getExpression());

                String type = "CUSTOM";
                if (func.getExpression() != null) {
                    if (func.getExpression().contains("FROM_ARRAYS")) {
                        type = "FROM_ARRAYS";
                    } else if (func.getExpression().contains("FROM_MATH")) {
                        type = "FROM_MATH";
                    } else if (func.getExpression().contains("FROM_EXPRESSION")) {
                        type = "FROM_EXPRESSION";
                    } else if (func.getExpression().contains("ТАБУЛИРОВАННАЯ")) {
                        type = "TABULATED";
                    }
                }
                funcMap.put("type", type);
                funcMap.put("createdAt", func.getCreatedAt());
                funcMap.put("userId", user.getId());

                if (func.getPoints() != null && !func.getPoints().isEmpty()) {
                    List<Map<String, Double>> points = new ArrayList<>();
                    for (var point : func.getPoints()) {
                        Map<String, Double> pointMap = new HashMap<>();
                        pointMap.put("x", point.getXValue());
                        pointMap.put("y", point.getYValue());
                        points.add(pointMap);
                    }
                    funcMap.put("points", points);
                } else {
                    funcMap.put("points", Collections.emptyList());
                }
                response.add(funcMap);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ошибка при получении функций: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createFunction(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String username = authentication.getName();
            var user = userService.getUserByUsernameOrNull(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден"));
            }

            String name = (String) request.get("name");
            String expression = (String) request.get("expression");

            if (name == null || name.trim().isEmpty()) {
                name = "Функция " + System.currentTimeMillis();
            }

            if (expression == null || expression.trim().isEmpty()) {
                expression = "Простая функция";
            }

            var function = functionService.createFunction(user, name, expression);

            Map<String, Object> response = new HashMap<>();
            response.put("id", function.getId());
            response.put("name", function.getName());
            response.put("expression", function.getExpression());
            response.put("createdAt", function.getCreatedAt());
            response.put("userId", user.getId());
            response.put("success", true);
            response.put("message", "Функция успешно создана");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFunction(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            var user = userService.getUserByUsernameOrNull(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден"));
            }

            var function = functionService.getFunctionById(id);
            if (function == null || !function.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Функция не найдена"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", function.getId());
            response.put("name", function.getName());
            response.put("expression", function.getExpression());
            response.put("createdAt", function.getCreatedAt());

            String type = "CUSTOM";
            if (function.getExpression() != null) {
                if (function.getExpression().contains("FROM_ARRAYS")) {
                    type = "FROM_ARRAYS";
                } else if (function.getExpression().contains("FROM_MATH")) {
                    type = "FROM_MATH";
                } else if (function.getExpression().contains("FROM_EXPRESSION")) {
                    type = "FROM_EXPRESSION";
                } else if (function.getExpression().contains("ТАБУЛИРОВАННАЯ")) {
                    type = "TABULATED";
                }
            }
            response.put("type", type);

            if (function.getPoints() != null && !function.getPoints().isEmpty()) {
                List<Map<String, Double>> points = new ArrayList<>();
                for (var point : function.getPoints()) {
                    Map<String, Double> pointMap = new HashMap<>();
                    pointMap.put("x", point.getXValue());
                    pointMap.put("y", point.getYValue());
                    points.add(pointMap);
                }
                response.put("points", points);
            } else {
                response.put("points", Collections.emptyList());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ошибка при получении функции: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFunction(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            var user = userService.getUserByUsernameOrNull(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден"));
            }

            var function = functionService.getFunctionById(id);
            if (function == null || !function.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Функция не найдена"));
            }

            boolean deleted = functionService.deleteFunction(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Функция успешно удалена"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ошибка при удалении функции"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ошибка при удалении функции: " + e.getMessage()));
        }
    }

    @PostMapping("/tabulated/by-points")
    public ResponseEntity<?> createTabulatedByPoints(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String username = authentication.getName();
            var user = userService.getUserByUsernameOrNull(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден"));
            }

            String name = (String) request.get("name");
            @SuppressWarnings("unchecked")
            List<Double> xValues = (List<Double>) request.get("xValues");
            @SuppressWarnings("unchecked")
            List<Double> yValues = (List<Double>) request.get("yValues");

            if (name == null || name.trim().isEmpty()) {
                name = "Табулированная функция";
            }

            var response = functionService.createTabulatedFunctionFromPoints(name, xValues, yValues);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/tabulated/by-math-function")
    public ResponseEntity<?> createTabulatedByMath(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String username = authentication.getName();
            var user = userService.getUserByUsernameOrNull(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден"));
            }

            String name = (String) request.get("name");
            String mathFunctionType = (String) request.get("mathFunctionType");
            double fromX = ((Number) request.get("fromX")).doubleValue();
            double toX = ((Number) request.get("toX")).doubleValue();
            int pointsCount = ((Number) request.get("pointsCount")).intValue();

            if (name == null || name.trim().isEmpty()) {
                name = mathFunctionType + " функция";
            }

            var response = functionService.createTabulatedFunctionFromMath(name, mathFunctionType, fromX, toX, pointsCount);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/types")
    public ResponseEntity<?> getFunctionTypes() {
        Map<String, String> types = new HashMap<>();
        types.put("FROM_ARRAYS", "Создать из массивов точек");
        types.put("FROM_MATH", "Создать из математической функции");
        types.put("FROM_EXPRESSION", "Создать из выражения");
        types.put("CUSTOM", "Простая функция");
        types.put("TABULATED", "Табулированная функция");
        return ResponseEntity.ok(types);
    }

    @GetMapping("/math-functions")
    public ResponseEntity<?> getMathFunctions() {
        List<Map<String, String>> functions = new ArrayList<>();

        functions.add(Map.of("key", "LINEAR", "name", "Линейная функция", "description", "y = ax + b"));
        functions.add(Map.of("key", "QUADRATIC", "name", "Квадратичная функция", "description", "y = ax² + bx + c"));
        functions.add(Map.of("key", "SIN", "name", "Синусоида", "description", "y = a*sin(bx + c)"));
        functions.add(Map.of("key", "COS", "name", "Косинусоида", "description", "y = a*cos(bx + c)"));
        functions.add(Map.of("key", "EXP", "name", "Экспонента", "description", "y = a*e^(bx)"));
        functions.add(Map.of("key", "LOG", "name", "Логарифм", "description", "y = a*ln(bx) + c"));
        functions.add(Map.of("key", "POWER", "name", "Степенная функция", "description", "y = a*x^b"));
        functions.add(Map.of("key", "ROOT", "name", "Корень", "description", "y = a*x^(1/b)"));
        functions.add(Map.of("key", "IDENTITY", "name", "Тождественная", "description", "y = x"));

        return ResponseEntity.ok(functions);
    }
}