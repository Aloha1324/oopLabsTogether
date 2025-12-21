package com.example.LAB5.framework.controller;

import com.example.LAB5.DTO.Response.FunctionResponse;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.TabulatedFunctionFactoryProvider;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.operations.TabulatedDifferentialOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/functions")
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
        TabulatedFunction original = toTabulatedFunction(functionService.getFunctionById(funcId));

        TabulatedDifferentialOperator operator =
                new TabulatedDifferentialOperator(factoryProvider.getCurrentFactory());

        TabulatedFunction derivative = operator.derive(original);

        return ResponseEntity.ok(functionService.saveTabulatedFunction(derivative, "Производная"));
    }

    private TabulatedFunction toTabulatedFunction(com.example.LAB5.framework.entity.Function f) {
        var points = f.getPoints();
        double[] x = points.stream().mapToDouble(p -> p.getXValue()).toArray();
        double[] y = points.stream().mapToDouble(p -> p.getYValue()).toArray();
        return factoryProvider.getCurrentFactory().create(x, y);
    }
}