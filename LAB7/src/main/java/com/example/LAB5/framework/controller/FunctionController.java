package com.example.LAB5.framework.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.example.LAB5.DTO.Request.FunctionOperationRequest;
import com.example.LAB5.DTO.Request.UpdateFunctionRequest;
import com.example.LAB5.DTO.Response.FunctionResponse;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.service.FunctionService;
import com.example.LAB5.framework.service.TabulatedFunctionFactoryProvider;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.FunctionScanner;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;
import com.example.LAB5.io.FunctionsIO;
import com.example.LAB5.DTO.Request.CompositeRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.LAB5.DTO.Request.IntegrationRequest;
import com.example.LAB5.DTO.Response.CalculationResponse;
import jakarta.validation.Valid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/functions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FunctionController {

    private final FunctionService functionService;
    private final TabulatedFunctionFactoryProvider factoryProvider;

    @Autowired
    public FunctionController(FunctionService functionService, TabulatedFunctionFactoryProvider factoryProvider) {
        this.functionService = functionService;
        this.factoryProvider = factoryProvider;
    }

    //

    private String getCurrentUsername(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }
        return auth.getName();
    }

    private TabulatedFunction toTabulatedFunction(Function f, Authentication auth) {
        var points = f.getPoints();
        double[] x = points.stream().mapToDouble(p -> p.getXValue()).toArray();
        double[] y = points.stream().mapToDouble(p -> p.getYValue()).toArray();
        String username = getCurrentUsername(auth);
        return factoryProvider.getFactoryForUser(username).create(x, y);
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

    // Экспорт функции в JSON
    @GetMapping("/{id}/export/json")
    public ResponseEntity<String> exportFunctionToJson(@PathVariable Long id) {
        String json = functionService.exportFunctionToJson(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=function_" + id + ".json")
                .header("Content-Type", "application/json")
                .body(json);
    }



    // Импорт функции из JSON
    @PostMapping("/import/json")
    public ResponseEntity<FunctionResponse> importFunctionFromJson(@RequestBody String json) {
        FunctionResponse response = functionService.importFunctionFromJson(json);
        return ResponseEntity.ok(response);
    }

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
            String username = getCurrentUsername(SecurityContextHolder.getContext().getAuthentication());
            TabulatedFunction tf = FunctionsIO.readTabulatedFunction(bis, factoryProvider.getFactoryForUser(username));
            FunctionResponse resp = functionService.saveTabulatedFunction(tf, "Импортированная функция");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/export")
    public void exportFunction(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Function f = functionService.getFunctionById(id);
        TabulatedFunction tf = toTabulatedFunction(f, SecurityContextHolder.getContext().getAuthentication());
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=function_" + id + ".bin");
        try (var bos = new BufferedOutputStream(response.getOutputStream())) {
            FunctionsIO.writeTabulatedFunction(bos, tf);
        }
    }

    @PostMapping("/{id}/insert")
    public ResponseEntity<?> insertPoint(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        double x = request.get("x");
        double y = request.get("y");
        functionService.insertPoint(id, x, y);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/composite")
    public ResponseEntity<FunctionResponse> createComposite(@RequestBody CompositeRequest request) {
        FunctionResponse response = functionService.createCompositeFunction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<?> removePoint(@PathVariable Long id, @RequestParam int index) {
        functionService.removePoint(id, index);
        return ResponseEntity.ok().build();
    }

    //
    /**
     * Создание табулированной функции ПО ТОЧКАМ (xValues, yValues)
     *
     */
    @PostMapping("/tabulated/by-points")
    public ResponseEntity<FunctionResponse> createTabulatedByPoints(@RequestBody Map<String, Object> request, Authentication auth) {
        String username = getCurrentUsername(auth);
        TabulatedFunctionFactory factory = factoryProvider.getFactoryForUser(username);
        System.out.println(">>> Текущая фабрика: " + factory);

        if (factory == null) {
            throw new IllegalStateException("Фабрика не установлена!");
        }

        String name = (String) request.get("name");

        @SuppressWarnings("unchecked")
        List<?> rawX = (List<?>) request.get("xValues");
        @SuppressWarnings("unchecked")
        List<?> rawY = (List<?>) request.get("yValues");

        List<Double> xValues = rawX.stream()
                .map(obj -> {
                    if (obj instanceof Number) {
                        return ((Number) obj).doubleValue();
                    } else if (obj instanceof String) {
                        try {
                            return Double.parseDouble((String) obj);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Неверный формат числа: " + obj);
                        }
                    } else {
                        throw new IllegalArgumentException("Неожиданный тип значения: " + obj.getClass());
                    }
                })
                .collect(Collectors.toList());

        List<Double> yValues = rawY.stream()
                .map(obj -> {
                    if (obj instanceof Number) {
                        return ((Number) obj).doubleValue();
                    } else if (obj instanceof String) {
                        try {
                            return Double.parseDouble((String) obj);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Неверный формат числа: " + obj);
                        }
                    } else {
                        throw new IllegalArgumentException("Неожиданный тип значения: " + obj.getClass());
                    }
                })
                .collect(Collectors.toList());

        if (rawX == null || rawY == null || rawX.size() != rawY.size()) {
            throw new IllegalArgumentException("Некорректные или пустые x/y массивы");
        }

        FunctionResponse response = functionService.createTabulatedFunctionFromPoints(name, xValues, yValues);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/integrate")
    public ResponseEntity<CalculationResponse> integrate(@Valid @RequestBody IntegrationRequest request) {
        CalculationResponse response = functionService.integrate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Создание табулированной функции ПО МАТЕМАТИЧЕСКОЙ ФОРМУЛы
     */
    @PostMapping("/tabulated/by-math-function")
    public ResponseEntity<FunctionResponse> createTabulatedByMath(@RequestBody Map<String, Object> request, Authentication auth) {
        String username = getCurrentUsername(auth);
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
        List<com.example.LAB5.functions.FunctionScanner.MathFunctionInfo> scanned = functionService.getMathFunctions();
        List<MathFunctionInfo> response = scanned.stream()
                .map(info -> new MathFunctionInfo(
                        info.clazz().getSimpleName(),
                        info.displayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    public static record MathFunctionInfo(String key, String description) {}
}