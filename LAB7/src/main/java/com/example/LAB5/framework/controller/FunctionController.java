package com.example.LAB5.framework.controller;

import com.example.LAB5.DTO.Response.FunctionResponse;
import com.example.LAB5.framework.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/functions")
public class FunctionController {

    // ✅ СТАРЫЙ функционал (in-memory) — ОСТАЁТСЯ БЕЗ ИЗМЕНЕНИЙ
    private final ConcurrentHashMap<Integer, Map<String, Object>> functions = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    // ✅ НОВЫЙ функционал (БД)
    private final FunctionService functionService;

    @Autowired
    public FunctionController(FunctionService functionService) {
        this.functionService = functionService;
        initializeDefaultData(); // старый функционал
    }

    // ============================================================================

    private void addFunction(String name, String type, Integer userId) {
        int id = idCounter.getAndIncrement();
        Map<String, Object> function = new HashMap<>();
        function.put("id", id);
        function.put("name", name);
        function.put("type", type);
        function.put("userId", userId);
        functions.put(id, function);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFunctions() {
        if (functions.isEmpty()) {
            initializeDefaultData();
        }
        return ResponseEntity.ok(new ArrayList<>(functions.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFunctionById(@PathVariable Integer id) {
        if (functions.isEmpty()) {
            initializeDefaultData();
        }
        Map<String, Object> function = functions.get(id);
        if (function == null) {
            Map<String, Object> defaultFunction = createDefaultFunction(id);
            functions.put(id, defaultFunction);
            return ResponseEntity.ok(defaultFunction);
        }
        return ResponseEntity.ok(new HashMap<>(function));
    }

    @PostMapping
    public ResponseEntity<?> createFunction(@RequestBody Map<String, Object> funcRequest) {
        try {
            Integer newId = idCounter.getAndIncrement();
            Object nameObj = funcRequest.get("name");
            Object typeObj = funcRequest.get("type");
            Object userIdObj = funcRequest.get("userId");

            if (nameObj == null || typeObj == null || userIdObj == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Missing required fields: name, type, userId")
                );
            }

            String name = nameObj.toString();
            String type = typeObj.toString();
            int userId = Integer.parseInt(userIdObj.toString());

            Map<String, Object> newFunction = new HashMap<>();
            newFunction.put("id", newId);
            newFunction.put("name", name);
            newFunction.put("type", type);
            newFunction.put("userId", userId);

            functions.put(newId, newFunction);
            return ResponseEntity.status(HttpStatus.CREATED).body(newFunction);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid number format for userId", "message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFunction(@PathVariable Integer id,
                                            @RequestBody Map<String, Object> updates) {
        if (functions.isEmpty()) {
            initializeDefaultData();
        }
        Map<String, Object> function = functions.get(id);
        if (function == null) {
            function = createDefaultFunction(id);
            functions.put(id, function);
        }
        try {
            Map<String, Object> updatedFunction = new HashMap<>(function);
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (!"id".equals(entry.getKey())) {
                    updatedFunction.put(entry.getKey(), entry.getValue());
                }
            }
            functions.put(id, updatedFunction);
            return ResponseEntity.ok(updatedFunction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Failed to update function", "message", e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Integer id) {
        try {
            if (functions.isEmpty()) {
                initializeDefaultData();
            }
            if (!functions.containsKey(id)) {
                Map<String, Object> function = createDefaultFunction(id);
                functions.put(id, function);
            }
            functions.remove(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Map<String, Object> createDefaultFunction(Integer id) {
        Map<String, Object> function = new HashMap<>();
        function.put("id", id);
        function.put("name", "default_function_" + id);
        function.put("type", "POLYNOMIAL");
        function.put("userId", 1);
        return function;
    }

    private void initializeDefaultData() {
        functions.clear();
        idCounter.set(1);
        addFunction("quadratic", "POLYNOMIAL", 1);
        addFunction("sin(x)", "TRIGONOMETRIC", 1);
        addFunction("x^2", "POLYNOMIAL", 2);
        addFunction("e^x", "EXPONENTIAL", 1);
    }

    //==========================================================================

    /**
     * Создание табулированной функции ПО ТОЧКАМ (xValues, yValues)
     *
     */
    @PostMapping("/tabulated/by-points")
    public ResponseEntity<FunctionResponse> createTabulatedByPoints(@RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<Double> xValues = (List<Double>) request.get("xValues");
        @SuppressWarnings("unchecked")
        List<Double> yValues = (List<Double>) request.get("yValues");

        FunctionResponse response = functionService.createTabulatedFunctionFromPoints(name, xValues, yValues);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Создание табулированной функции ПО МАТЕМАТИЧЕСКОЙ ФОРМУЛЕ
     * Выполняет задание №2!
     */
    @PostMapping("/tabulated/by-math-function")
    public ResponseEntity<FunctionResponse> createTabulatedByMath(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String mathFunctionType = (String) request.get("mathFunctionType");
        double fromX = ((Number) request.get("fromX")).doubleValue();
        double toX = ((Number) request.get("toX")).doubleValue();
        int pointsCount = ((Number) request.get("pointsCount")).intValue();

        FunctionResponse response = functionService.createTabulatedFunctionFromMath(name, mathFunctionType, fromX, toX, pointsCount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Список доступных математических функций
     */
    @GetMapping("/tabulated/math-functions")
    public ResponseEntity<List<MathFunctionInfo>> getMathFunctions() {
        List<MathFunctionInfo> functions = List.of(
                new MathFunctionInfo("LINEAR", "Линейная функция ax + b"),
                new MathFunctionInfo("QUADRATIC", "Квадратичная функция ax² + bx + c"),
                new MathFunctionInfo("SIN", "Синус a*sin(bx + c)"),
                new MathFunctionInfo("COS", "Косинус a*cos(bx + c)"),
                new MathFunctionInfo("EXP", "Экспонента a*e^(bx)"),
                new MathFunctionInfo("LOG", "Логарифм a*ln(bx) + c"),
                new MathFunctionInfo("POWER", "Степенная функция a*x^b"),
                new MathFunctionInfo("ROOT", "Корень a*x^(1/b)"),
                new MathFunctionInfo("IDENTITY", "Тождественная функция x")
        );
        return ResponseEntity.ok(functions);
    }


    //  ВНУТРЕННИЕ DTO — ПРОСТЫЕ И ЧИТАЕМЫЕ

    public record MathFunctionInfo(String key, String description) {}
}
