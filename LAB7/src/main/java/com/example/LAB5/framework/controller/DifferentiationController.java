// src/main/java/com/example/LAB5/framework/controller/DifferentiationController.java

package com.example.LAB5.framework.controller;

import com.example.LAB5.DTO.Response.FunctionResponse;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.TabulatedFunctionFactoryProvider;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.operations.TabulatedDifferentialOperator;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory; // ← Добавьте этот импорт!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/functions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DifferentiationController {

    private final FunctionService functionService;
    private final TabulatedFunctionFactoryProvider factoryProvider;

    @Autowired
    public DifferentiationController(FunctionService functionService,
                                     TabulatedFunctionFactoryProvider factoryProvider) {
        this.functionService = functionService;
        this.factoryProvider = factoryProvider;
    }

    @PostMapping("/differentiate")
    public ResponseEntity<FunctionResponse> differentiate(@RequestBody Map<String, Object> request) {
        Long funcId = ((Number) request.get("functionId")).longValue();
        Function f = functionService.getFunctionById(funcId);
        TabulatedFunction original = toTabulatedFunction(f);

        // Получаем имя текущего пользователя
        String username = getCurrentUsername();

        // Получаем фабрику для пользователя → это объект типа TabulatedFunctionFactory
        TabulatedFunctionFactory factory = factoryProvider.getFactoryForUser(username);

        // Создаем оператор дифференцирования с правильной фабрикой
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        TabulatedFunction derivative = operator.derive(original);

        return ResponseEntity.ok(functionService.saveTabulatedFunction(derivative, "Производная"));
    }

    private TabulatedFunction toTabulatedFunction(Function f) {
        var points = f.getPoints();
        double[] x = points.stream().mapToDouble(p -> p.getXValue()).toArray();
        double[] y = points.stream().mapToDouble(p -> p.getYValue()).toArray();
        String username = getCurrentUsername();
        return factoryProvider.getFactoryForUser(username).create(x, y);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }
        return auth.getName();
    }
}