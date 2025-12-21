package com.example.LAB5.framework.controller;

import com.example.LAB5.DTO.Request.UpdateFunctionRequest;
import com.example.LAB5.DTO.Response.FunctionResponse;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.TabulatedFunctionFactoryProvider;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;
import com.example.LAB5.io.FunctionsIO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/functions")
public class FunctionController {



    // функционал (БД)
    private final FunctionService functionService;
    private final TabulatedFunctionFactoryProvider factoryProvider;
    @Autowired
    public FunctionController(FunctionService functionService, TabulatedFunctionFactoryProvider factoryProvider) {
        this.functionService = functionService;
        this.factoryProvider = factoryProvider;
    }

    // ============================================================================


    private TabulatedFunction toTabulatedFunction(Function f) {
        var points = f.getPoints();
        double[] x = points.stream().mapToDouble(p -> p.getXValue()).toArray();
        double[] y = points.stream().mapToDouble(p -> p.getYValue()).toArray();
        return factoryProvider.getCurrentFactory().create(x, y);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunctionResponse> updateFunction(
            @PathVariable Long id,
            @RequestBody UpdateFunctionRequest request) {
        return ResponseEntity.ok(functionService.updateFunction(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunctionResponse> getFunctionById(@PathVariable Long id) {
        Function f = functionService.getFunctionById(id);
        return ResponseEntity.ok(functionService.convertToResponse(f));
    }
    //
    @GetMapping("/my")
    public ResponseEntity<List<FunctionResponse>> getAllFunctionsForCurrentUser() {
        List<Function> functions = functionService.getAllFunctionsForCurrentUser();
        return ResponseEntity.ok(functions.stream()
                .map(functionService::convertToResponse)
                .toList());
    }

    @PostMapping("/import")
    public ResponseEntity<FunctionResponse> importFunction(@RequestParam("file") MultipartFile file) {
        try (var bis = new BufferedInputStream(file.getInputStream())) {
            TabulatedFunction tf = FunctionsIO.readTabulatedFunction(bis, factoryProvider.getCurrentFactory());
            FunctionResponse resp = functionService.saveTabulatedFunction(tf, "Импортированная функция");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/export")
    public void exportFunction(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Function f = functionService.getFunctionById(id);
        TabulatedFunction tf = toTabulatedFunction(f);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=function_" + id + ".bin");
        try (var bos = new BufferedOutputStream(response.getOutputStream())) {
            FunctionsIO.writeTabulatedFunction(bos, tf);
        }
    }

    //==========================================================================

    /**
     * Создание табулированной функции ПО ТОЧКАМ (xValues, yValues)
     *
     */
    @PostMapping("/tabulated/by-points")
    public ResponseEntity<FunctionResponse> createTabulatedByPoints(@RequestBody Map<String, Object> request) {

        TabulatedFunctionFactory factory = factoryProvider.getCurrentFactory();
        System.out.println(">>> Текущая фабрика: " + factory); // ← добавьте это
        if (factory == null) {
            throw new IllegalStateException("Фабрика не установлена!");
        }

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
                new MathFunctionInfo("IDENTITY", "Тождественная функция (f(x) = x)"),
                new MathFunctionInfo("SQR", "Квадратичная функция (f(x) = x²)"),
                new MathFunctionInfo("UNIT", "Константа 1"),
                new MathFunctionInfo("ZERO", "Константа 0"),
                new MathFunctionInfo("CONST_2", "Константа 2")
        );
        // Сортируем по описанию (по русскому названию)
        List<MathFunctionInfo> sorted = functions.stream()
                .sorted(Comparator.comparing(MathFunctionInfo::description))
                .toList();
        return ResponseEntity.ok(sorted);
    }


    //  ВНУТРЕННИЕ DTO — ПРОСТЫЕ И ЧИТАЕМЫЕ

    public record MathFunctionInfo(String key, String description) {}
}
