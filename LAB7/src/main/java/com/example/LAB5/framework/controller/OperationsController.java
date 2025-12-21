package com.example.LAB5.framework.controller;

import com.example.LAB5.DTO.Response.CalculationResponse;
import com.example.LAB5.DTO.Response.FunctionResponse;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.TabulatedFunctionFactoryProvider;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.operations.TabulatedFunctionOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/functions")
public class OperationsController {

    private final FunctionService functionService;
    private final TabulatedFunctionFactoryProvider factoryProvider;

    @Autowired
    public OperationsController(FunctionService functionService,
                                TabulatedFunctionFactoryProvider factoryProvider) {
        this.functionService = functionService;
        this.factoryProvider = factoryProvider;
    }

    @PostMapping("/operations/value")
    public ResponseEntity<CalculationResponse> calculateValue(@RequestBody Map<String, Object> request) {
        Long functionId = ((Number) request.get("functionId")).longValue();
        Double x = ((Number) request.get("x")).doubleValue();

        // Проверяем права доступа и получаем функцию
        Function function = functionService.getFunctionById(functionId);

        // Преобразуем в TabulatedFunction для вычисления
        TabulatedFunction tf = toTabulatedFunction(function);

        // Вычисляем значение (TabulatedFunction уже имеет apply(double x))
        double result = tf.apply(x);

        // Возвращаем ответ в формате, который ожидает фронтенд
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("result", result);
        response.put("message", "Успешно");

        return ResponseEntity.ok((CalculationResponse) response);
    }

    @PostMapping("/operations/{operation}")
    public ResponseEntity<FunctionResponse> performOperation(
            @PathVariable String operation,
            @RequestBody Map<String, Object> request) {

        Long funcAId = ((Number) request.get("functionAId")).longValue();
        Long funcBId = ((Number) request.get("functionBId")).longValue();

        // Преобразуем Function → TabulatedFunction
        TabulatedFunction a = toTabulatedFunction(functionService.getFunctionById(funcAId));
        TabulatedFunction b = toTabulatedFunction(functionService.getFunctionById(funcBId));

        TabulatedFunctionOperationService opService =
                new TabulatedFunctionOperationService(factoryProvider.getCurrentFactory());

        TabulatedFunction result = switch (operation) {
            case "add" -> opService.add(a, b);
            case "sub" -> opService.subtract(a, b);
            case "mul" -> opService.multiply(a, b);
            case "div" -> opService.divide(a, b);
            default -> throw new IllegalArgumentException("Неизвестная операция: " + operation);
        };

        return ResponseEntity.ok(functionService.saveTabulatedFunction(result, "Результат " + operation));
    }

    // Вспомогательный метод
    private TabulatedFunction toTabulatedFunction(com.example.LAB5.framework.entity.Function f) {
        var points = f.getPoints();
        double[] x = points.stream().mapToDouble(p -> p.getXValue()).toArray();
        double[] y = points.stream().mapToDouble(p -> p.getYValue()).toArray();
        return factoryProvider.getCurrentFactory().create(x, y);
    }
}