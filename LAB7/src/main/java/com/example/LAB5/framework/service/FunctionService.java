package com.example.LAB5.framework.service;


import com.example.LAB5.DTO.FunctionDTO;
import com.example.LAB5.DTO.Request.*;
import com.example.LAB5.DTO.Response.*;
import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.Point;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.repository.FunctionRepository;
import com.example.LAB5.framework.repository.PointRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class FunctionService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);

    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<PerformanceMetrics> performanceMetrics = new ArrayList<>();

    @Autowired
    public FunctionService(FunctionRepository functionRepository,
                           PointRepository pointRepository,
                           UserService userService) {
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
        this.userService = userService;
    }

    public static class PerformanceMetrics {
        private final String operationName;
        private final long executionTimeMs;
        private final int recordsProcessed;
        private final String frameworkType;

        public PerformanceMetrics(String operationName, long executionTimeMs, int recordsProcessed, String frameworkType) {
            this.operationName = operationName;
            this.executionTimeMs = executionTimeMs;
            this.recordsProcessed = recordsProcessed;
            this.frameworkType = frameworkType;
        }

        public long getExecutionTimeMs() { return executionTimeMs; }
        public int getRecordsProcessed() { return recordsProcessed; }
        public String getFrameworkType() { return frameworkType; }
        public double getRecordsPerSecond() {
            return executionTimeMs > 0 ? (recordsProcessed * 1000.0) / executionTimeMs : 0;
        }

        @Override
        public String toString() {
            return String.format(
                    "PerformanceMetrics{operation='%s', time=%dms, records=%d, records/sec=%.2f, framework=%s}",
                    operationName, executionTimeMs, recordsProcessed, getRecordsPerSecond(), frameworkType
            );
        }
    }

    public static class StreamingProcessingResult {
        private final int processedCount;
        private final int errorCount;
        private final long processingTime;

        public StreamingProcessingResult(int processedCount, int errorCount, long processingTime) {
            this.processedCount = processedCount;
            this.errorCount = errorCount;
            this.processingTime = processingTime;
        }

        public int getProcessedCount() { return processedCount; }
        public int getErrorCount() { return errorCount; }
        public long getProcessingTime() { return processingTime; }
        public double getSuccessRate() {
            return processedCount > 0 ? (double) (processedCount - errorCount) / processedCount * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format("Обработано: %d, Ошибок: %d, Время: %d мс, Успех: %.1f%%",
                    processedCount, errorCount, processingTime, getSuccessRate());
        }
    }

    @FunctionalInterface
    public interface FunctionProcessor {
        void process(Function function);
    }

    public Function getFunctionByIdOrNull(Long id) {
        long startTime = System.nanoTime();
        Optional<Function> result = functionRepository.findById(id);
        Function function = result.orElse(null);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("GET_FUNCTION_BY_ID_OR_NULL", durationMs, function != null ? 1 : 0, "SPRING_DATA_JPA"));
        return function;
    }

    public Function getFunctionById(Long id) {
        long startTime = System.nanoTime();
        Optional<Function> functionOpt = functionRepository.findById(id);
        if (functionOpt.isPresent()) {
            Function function = functionOpt.get();
            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            performanceMetrics.add(new PerformanceMetrics("GET_FUNCTION_BY_ID", durationMs, 1, "SPRING_DATA_JPA"));
            return function;
        }
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("GET_FUNCTION_BY_ID", durationMs, 0, "SPRING_DATA_JPA"));
        throw new RuntimeException("Function not found with id: " + id);
    }

    public Optional<Function> findFunctionById(Long id) {
        long startTime = System.nanoTime();
        Optional<Function> result = functionRepository.findById(id);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("FIND_FUNCTION_BY_ID", durationMs, result.isPresent() ? 1 : 0, "SPRING_DATA_JPA"));
        return result;
    }

    public Function createFunction(User user, String name, String expression) {
        long startTime = System.nanoTime();
        Function function = new Function();
        function.setName(name);
        function.setExpression(expression);
        function.setUser(user);
        function.setCreatedAt(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("CREATE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));
        return savedFunction;
    }

    public Function saveFunction(Function function) {
        long startTime = System.nanoTime();
        Function savedFunction = functionRepository.save(function);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("SAVE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));
        return savedFunction;
    }

    public List<Function> getAllFunctions() {
        long startTime = System.nanoTime();
        List<Function> result = functionRepository.findAll();
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("GET_ALL_FUNCTIONS", durationMs, result.size(), "SPRING_DATA_JPA"));
        return result;
    }

    public List<Function> getFunctionsByUserId(Long userId) {
        long startTime = System.nanoTime();
        List<Function> result = functionRepository.findByUserId(userId);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("GET_FUNCTIONS_BY_USER_ID", durationMs, result.size(), "SPRING_DATA_JPA"));
        return result;
    }

    public Function updateFunction(Long id, String name, String expression) {
        long startTime = System.nanoTime();
        Optional<Function> existingFunction = functionRepository.findById(id);
        if (existingFunction.isPresent()) {
            Function function = existingFunction.get();
            function.setName(name);
            function.setExpression(expression);
            Function updatedFunction = functionRepository.save(function);
            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            performanceMetrics.add(new PerformanceMetrics("UPDATE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));
            return updatedFunction;
        }
        return null;
    }

    public boolean deleteFunction(Long id) {
        long startTime = System.nanoTime();
        Optional<Function> function = functionRepository.findById(id);
        if (function.isPresent()) {
            functionRepository.delete(function.get());
            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            performanceMetrics.add(new PerformanceMetrics("DELETE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));
            return true;
        }
        return false;
    }

    public List<Function> getFunctionsByName(String name) {
        return functionRepository.findByNameContainingIgnoreCase(name);
    }

    public int getTotalFunctionsCount() {
        return (int) functionRepository.count();
    }

    public boolean functionExists(Long id) {
        long startTime = System.nanoTime();
        boolean exists = functionRepository.existsById(id);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("FUNCTION_EXISTS", durationMs, exists ? 1 : 0, "SPRING_DATA_JPA"));
        return exists;
    }

    public List<PerformanceMetrics> getPerformanceMetrics() {
        return new ArrayList<>(performanceMetrics);
    }

    public void clearPerformanceMetrics() {
        performanceMetrics.clear();
    }

    @Transactional(readOnly = true)
    public List<Function> getAllFunctionsStreaming() {
        long startTime = System.nanoTime();
        List<Function> result;
        try (Stream<Function> functionStream = functionRepository.findAll().stream()) {
            result = functionStream.collect(Collectors.toList());
        } catch (Exception e) {
            return getAllFunctions();
        }
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("GET_ALL_FUNCTIONS_STREAMING", durationMs, result.size(), "SPRING_DATA_JPA"));
        return result;
    }

    @Transactional(readOnly = true)
    public List<Function> getAllFunctionsStreamingWithBatch(int batchSize) {
        long startTime = System.nanoTime();
        List<Function> result = new ArrayList<>();
        AtomicInteger totalProcessed = new AtomicInteger(0);
        try (Stream<Function> functionStream = functionRepository.findAll().stream()) {
            List<Function> batch = new ArrayList<>(batchSize);
            functionStream.forEach(function -> {
                batch.add(function);
                int currentCount = totalProcessed.incrementAndGet();
                if (batch.size() >= batchSize) {
                    result.addAll(batch);
                    batch.clear();
                }
            });
            if (!batch.isEmpty()) {
                result.addAll(batch);
            }
        } catch (Exception e) {
            return getAllFunctionsStreaming();
        }
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("GET_ALL_FUNCTIONS_STREAMING_BATCH", durationMs, result.size(), "SPRING_DATA_JPA"));
        return result;
    }

    public StreamingProcessingResult processFunctionsStreaming(FunctionProcessor processor) {
        long startTime = System.nanoTime();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        try (Stream<Function> functionStream = functionRepository.findAll().stream()) {
            functionStream.forEach(function -> {
                try {
                    processor.process(function);
                    processedCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            });
        } catch (Exception e) {
        }
        long endTime = System.nanoTime();
        long processingTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        StreamingProcessingResult result = new StreamingProcessingResult(
                processedCount.get(),
                errorCount.get(),
                processingTime
        );
        performanceMetrics.add(new PerformanceMetrics("PROCESS_FUNCTIONS_STREAMING", processingTime, processedCount.get(), "SPRING_DATA_JPA"));
        return result;
    }

    public FunctionResponse createFunctionFromArrays(CreateFunctionRequest request) {
        long startTime = System.nanoTime();
        String functionName = getFieldValue(request, "name", String.class);
        if (functionName == null || functionName.trim().isEmpty()) {
            functionName = "Unnamed Function " + System.currentTimeMillis();
        }
        User currentUser = getCurrentUser();
        List<Double> xValues = getFieldValue(request, "xValues", List.class);
        List<Double> yValues = getFieldValue(request, "yValues", List.class);
        if (xValues == null || yValues == null || xValues.isEmpty() || yValues.isEmpty()) {
            throw new IllegalArgumentException("X and Y arrays cannot be empty");
        }
        if (xValues.size() != yValues.size()) {
            throw new IllegalArgumentException("X and Y arrays must have the same size");
        }
        if (xValues.size() < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }
        for (int i = 1; i < xValues.size(); i++) {
            if (xValues.get(i) <= xValues.get(i - 1)) {
                throw new IllegalArgumentException("X values must be strictly increasing");
            }
        }
        Function function = new Function();
        function.setName(functionName);
        function.setExpression("FROM_ARRAYS: Created from arrays with " + xValues.size() + " points");
        function.setUser(currentUser);
        function.setCreatedAt(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            Point point = new Point();
            point.setXValue(xValues.get(i));
            point.setYValue(yValues.get(i));
            point.setFunction(savedFunction);
            point.setUser(currentUser);
            points.add(point);
        }
        pointRepository.saveAll(points);
        savedFunction.setPoints(points);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("CREATE_FROM_ARRAYS", durationMs, points.size(), "SPRING_DATA_JPA"));
        return convertToResponse(savedFunction);
    }

    public FunctionResponse createFunctionFromMathFunction(CreateFunctionRequest request) {
        long startTime = System.nanoTime();
        String functionName = getFieldValue(request, "name", String.class);
        if (functionName == null || functionName.trim().isEmpty()) {
            functionName = "Math Function " + System.currentTimeMillis();
        }
        String mathFunctionType = getFieldValue(request, "mathFunctionType", String.class);
        User currentUser = getCurrentUser();
        Double fromX = getFieldValue(request, "fromX", Double.class);
        Double toX = getFieldValue(request, "toX", Double.class);
        Integer pointsCount = getFieldValue(request, "pointsCount", Integer.class);
        if (fromX == null || toX == null) {
            throw new IllegalArgumentException("fromX and toX are required");
        }
        if (fromX >= toX) {
            throw new IllegalArgumentException("fromX must be less than toX");
        }
        if (pointsCount == null || pointsCount < 2) {
            throw new IllegalArgumentException("pointsCount must be at least 2");
        }
        Function function = new Function();
        function.setName(functionName);
        function.setExpression("FROM_MATH: " + String.format("%s on [%.2f, %.2f]",
                mathFunctionType, fromX, toX));
        function.setUser(currentUser);
        function.setCreatedAt(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        List<Point> points = new ArrayList<>();
        double step = (toX - fromX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = fromX + i * step;
            double y = calculateMathFunction(
                    mathFunctionType,
                    x,
                    getFieldValue(request, "coefficientA", Double.class),
                    getFieldValue(request, "coefficientB", Double.class),
                    getFieldValue(request, "coefficientC", Double.class)
            );
            Point point = new Point();
            point.setXValue(x);
            point.setYValue(y);
            point.setFunction(savedFunction);
            point.setUser(currentUser);
            points.add(point);
        }
        pointRepository.saveAll(points);
        savedFunction.setPoints(points);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("CREATE_FROM_MATH", durationMs, points.size(), "SPRING_DATA_JPA"));
        return convertToResponse(savedFunction);
    }

    private double calculateMathFunction(String type, double x, Double a, Double b, Double c) {
        double coefficientA = a != null ? a : 1.0;
        double coefficientB = b != null ? b : 0.0;
        double coefficientC = c != null ? c : 0.0;
        return switch (type.toUpperCase()) {
            case "LINEAR" -> coefficientA * x + coefficientB;
            case "QUADRATIC" -> coefficientA * x * x + coefficientB * x + coefficientC;
            case "SIN" -> coefficientA * Math.sin(coefficientB * x + coefficientC);
            case "COS" -> coefficientA * Math.cos(coefficientB * x + coefficientC);
            case "EXP" -> coefficientA * Math.exp(coefficientB * x);
            case "LOG" -> {
                if (x <= 0) throw new IllegalArgumentException("Log requires x > 0");
                yield coefficientA * Math.log(coefficientB * x) + coefficientC;
            }
            case "POWER" -> coefficientA * Math.pow(x, coefficientB);
            case "ROOT" -> {
                if (x < 0 && coefficientB % 2 == 0) throw new IllegalArgumentException("Even root of negative number");
                yield coefficientA * Math.pow(x, 1.0/coefficientB);
            }
            case "IDENTITY" -> x;
            default -> throw new IllegalArgumentException("Unknown function type: " + type);
        };
    }

    public FunctionResponse createFunctionFromExpression(CreateFunctionRequest request) {
        long startTime = System.nanoTime();
        String functionName = getFieldValue(request, "name", String.class);
        if (functionName == null || functionName.trim().isEmpty()) {
            functionName = "Expression Function " + System.currentTimeMillis();
        }
        String expression = getFieldValue(request, "expression", String.class);
        User currentUser = getCurrentUser();
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }
        Function function = new Function();
        function.setName(functionName);
        function.setExpression("FROM_EXPRESSION: " + expression);
        function.setUser(currentUser);
        function.setCreatedAt(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("CREATE_FROM_EXPRESSION", durationMs, 1, "SPRING_DATA_JPA"));
        return convertToResponse(savedFunction);
    }

    public FunctionResponse updateFunction(Long id, UpdateFunctionRequest request) {
        long startTime = System.nanoTime();
        Function function = getFunctionById(id);
        User currentUser = getCurrentUser();
        if (!function.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to update this function");
        }
        String newName = request.getName();
        if (newName != null && !newName.isEmpty()) {
            function.setName(newName);
        }
        List<UpdateFunctionRequest.PointUpdateRequest> pointsList = request.getPoints();
        if (pointsList != null && !pointsList.isEmpty()) {
            pointRepository.deleteByFunctionId(function.getId());
            List<Point> newPoints = new ArrayList<>();
            for (UpdateFunctionRequest.PointUpdateRequest pointReq : pointsList) {
                Point point = new Point();
                point.setXValue(pointReq.getX());
                point.setYValue(pointReq.getY());
                point.setFunction(function);
                point.setUser(currentUser);
                newPoints.add(point);
            }
            pointRepository.saveAll(newPoints);
            function.setPoints(newPoints);
        }
        Function updatedFunction = functionRepository.save(function);
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("UPDATE_FUNCTION_DTO", durationMs,
                pointsList != null ? pointsList.size() : 0, "SPRING_DATA_JPA"));
        return convertToResponse(updatedFunction);
    }

    public List<FunctionResponse> searchFunctions(String keyword) {
        long startTime = System.nanoTime();
        User currentUser = getCurrentUser();
        List<Function> functionsByName = functionRepository.findByNameContainingIgnoreCase(keyword);
        List<Function> functions = functionsByName.stream()
                .filter(func -> func.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("SEARCH_FUNCTIONS", durationMs, functions.size(), "SPRING_DATA_JPA"));
        return functions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CalculationResponse calculateAtPoint(FunctionOperationRequest request) {
        long startTime = System.nanoTime();
        try {
            Long functionId = request.getFunctionId();
            Function function = getFunctionById(functionId);
            List<Point> points = function.getPoints();
            if (points == null || points.isEmpty()) {
                CalculationResponse errorResponse = new CalculationResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Функция не содержит точек");
                errorResponse.setFunctionId(functionId);
                return errorResponse;
            }
            if (request.isBatchRequest()) {
                List<Double> xValues = request.getXValues();
                List<Double> results = new ArrayList<>();
                for (Double x : xValues) {
                    double result = interpolateFunction(points, x);
                    results.add(result);
                }
                long endTime = System.nanoTime();
                long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                performanceMetrics.add(new PerformanceMetrics("CALCULATE_AT_POINT_BATCH", durationMs, xValues.size(), "SPRING_DATA_JPA"));
                CalculationResponse response = new CalculationResponse();
                response.setSuccess(true);
                response.setFunctionId(functionId);
                response.setXValues(xValues);
                response.setResults(results);
                response.setCalculationTime(durationMs);
                response.setOperationType(request.getOperationType());
                response.setMethod(request.getMethod());
                response.setMessage("Пакетное вычисление успешно");
                return response;
            } else {
                double result = interpolateFunction(points, request.getX());
                long endTime = System.nanoTime();
                long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                performanceMetrics.add(new PerformanceMetrics("CALCULATE_AT_POINT", durationMs, 1, "SPRING_DATA_JPA"));
                CalculationResponse response = new CalculationResponse();
                response.setSuccess(true);
                response.setFunctionId(functionId);
                response.setX(request.getX());
                response.setResult(result);
                response.setCalculationTime(durationMs);
                response.setOperationType(request.getOperationType());
                response.setMethod(request.getMethod());
                response.setMessage("Вычисление успешно");
                return response;
            }
        } catch (Exception e) {
            CalculationResponse errorResponse = new CalculationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Ошибка при вычислении: " + e.getMessage());
            errorResponse.setFunctionId(request.getFunctionId());
            return errorResponse;
        }
    }

    private double interpolateFunction(List<Point> points, double x) {
        if (points.size() < 2) {
            throw new IllegalArgumentException("Function must have at least 2 points");
        }
        if (x < points.get(0).getXValue()) {
            return points.get(0).getYValue();
        }
        if (x > points.get(points.size() - 1).getXValue()) {
            return points.get(points.size() - 1).getYValue();
        }
        for (int i = 0; i < points.size() - 1; i++) {
            double x1 = points.get(i).getXValue();
            double x2 = points.get(i + 1).getXValue();
            if (x >= x1 && x <= x2) {
                double y1 = points.get(i).getYValue();
                double y2 = points.get(i + 1).getYValue();
                return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            }
        }
        return points.get(points.size() - 1).getYValue();
    }

    public FunctionResponse differentiate(DifferentialRequest request) {
        long startTime = System.nanoTime();
        try {
            Long functionId = request.getFunctionId();
            Function function = getFunctionById(functionId);
            List<Point> points = function.getPoints();
            if (points.size() < 2) {
                throw new IllegalArgumentException("Function must have at least 2 points for differentiation");
            }
            User currentUser = getCurrentUser();
            Function derivativeFunction = new Function();
            derivativeFunction.setName(function.getName() + "' (derivative)");
            derivativeFunction.setExpression("DERIVATIVE: " + function.getExpression());
            derivativeFunction.setUser(currentUser);
            derivativeFunction.setCreatedAt(LocalDateTime.now());
            Function savedDerivative = functionRepository.save(derivativeFunction);
            List<Point> derivativePoints = new ArrayList<>();
            for (int i = 0; i < points.size() - 1; i++) {
                double x1 = points.get(i).getXValue();
                double y1 = points.get(i).getYValue();
                double x2 = points.get(i + 1).getXValue();
                double y2 = points.get(i + 1).getYValue();
                double xMid = (x1 + x2) / 2;
                double derivative = (y2 - y1) / (x2 - x1);
                Point point = new Point();
                point.setXValue(xMid);
                point.setYValue(derivative);
                point.setFunction(savedDerivative);
                point.setUser(currentUser);
                derivativePoints.add(point);
            }
            pointRepository.saveAll(derivativePoints);
            savedDerivative.setPoints(derivativePoints);
            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            performanceMetrics.add(new PerformanceMetrics("DIFFERENTIATE", durationMs, derivativePoints.size(), "SPRING_DATA_JPA"));
            return convertToResponse(savedDerivative);
        } catch (Exception e) {
            throw new RuntimeException("Error in differentiate: " + e.getMessage(), e);
        }
    }

    public CalculationResponse integrate(IntegrationRequest request) {
        long startTime = System.nanoTime();
        try {
            Long functionId = request.getFunctionId();
            Function function = getFunctionById(functionId);
            List<Point> points = function.getPoints();
            if (points.size() < 2) {
                CalculationResponse errorResponse = new CalculationResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Function must have at least 2 points for integration");
                errorResponse.setFunctionId(functionId);
                return errorResponse;
            }
            double result = parallelTrapezoidalIntegration(points, 1);
            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            performanceMetrics.add(new PerformanceMetrics("INTEGRATE", durationMs, points.size(), "SPRING_DATA_JPA"));
            CalculationResponse response = new CalculationResponse();
            response.setSuccess(true);
            response.setFunctionId(functionId);
            response.setResult(result);
            response.setCalculationTime(durationMs);
            response.setMessage("Интегрирование успешно");
            return response;
        } catch (Exception e) {
            CalculationResponse errorResponse = new CalculationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Ошибка при интегрировании: " + e.getMessage());
            errorResponse.setFunctionId(request.getFunctionId());
            return errorResponse;
        }
    }

    private double parallelTrapezoidalIntegration(List<Point> points, int threads) {
        if (threads <= 1) {
            return sequentialTrapezoidalIntegration(points);
        }
        int n = points.size() - 1;
        double total = 0.0;
        int chunkSize = n / threads;
        List<Thread> threadList = new ArrayList<>();
        List<Double> partialSums = Collections.synchronizedList(new ArrayList<>());
        for (int t = 0; t < threads; t++) {
            final int threadIndex = t;
            Thread thread = new Thread(() -> {
                double partialSum = 0.0;
                int start = threadIndex * chunkSize;
                int end = (threadIndex == threads - 1) ? n : (threadIndex + 1) * chunkSize;
                for (int i = start; i < end; i++) {
                    double x1 = points.get(i).getXValue();
                    double y1 = points.get(i).getYValue();
                    double x2 = points.get(i + 1).getXValue();
                    double y2 = points.get(i + 1).getYValue();
                    partialSum += (y1 + y2) * (x2 - x1) / 2;
                }
                partialSums.add(partialSum);
            });
            threadList.add(thread);
            thread.start();
        }
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Integration interrupted", e);
            }
        }
        total = partialSums.stream().mapToDouble(Double::doubleValue).sum();
        return total;
    }

    private double sequentialTrapezoidalIntegration(List<Point> points) {
        double total = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            double x1 = points.get(i).getXValue();
            double y1 = points.get(i).getYValue();
            double x2 = points.get(i + 1).getXValue();
            double y2 = points.get(i + 1).getYValue();
            total += (y1 + y2) * (x2 - x1) / 2;
        }
        return total;
    }

    public String exportFunctionToJson(Long functionId) {
        long startTime = System.nanoTime();
        Function function = getFunctionById(functionId);
        try {
            String json = objectMapper.writeValueAsString(convertToDTO(function));
            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            performanceMetrics.add(new PerformanceMetrics("EXPORT_TO_JSON", durationMs, 1, "SPRING_DATA_JPA"));
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error exporting function to JSON", e);
        }
    }

    public FunctionResponse importFunctionFromJson(String json) {
        long startTime = System.nanoTime();
        try {
            FunctionDTO functionDTO = objectMapper.readValue(json, FunctionDTO.class);
            User currentUser = getCurrentUser();
            Function function = new Function();
            function.setName(functionDTO.getName());
            function.setExpression(functionDTO.getExpression());
            function.setUser(currentUser);
            function.setCreatedAt(LocalDateTime.now());
            Function savedFunction = functionRepository.save(function);
            if (functionDTO.getPoints() != null) {
                List<Point> points = new ArrayList<>();
                for (com.example.LAB5.DTO.PointDTO pointDTO : functionDTO.getPoints()) {
                    Point point = new Point();
                    point.setXValue(pointDTO.getX());
                    point.setYValue(pointDTO.getY());
                    point.setFunction(savedFunction);
                    point.setUser(currentUser);
                    points.add(point);
                }
                pointRepository.saveAll(points);
                savedFunction.setPoints(points);
            }
            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            performanceMetrics.add(new PerformanceMetrics("IMPORT_FROM_JSON", durationMs,
                    functionDTO.getPoints() != null ? functionDTO.getPoints().size() : 0, "SPRING_DATA_JPA"));
            return convertToResponse(savedFunction);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error importing function from JSON", e);
        }
    }

    // ============================================================================

    /**
     *СОЗДАНИЕ ТАБУЛИРОВАННОЙ ФУНКЦИИ ПО ТОЧКАМ (xValues, yValues)
     */
    public FunctionResponse createTabulatedFunctionFromPoints(String name, List<Double> xValues, List<Double> yValues) {
        long startTime = System.nanoTime();

        // Валидация (как в оригинале)
        if (xValues == null || yValues == null || xValues.isEmpty() || yValues.isEmpty()) {
            throw new IllegalArgumentException("X и Y массивы не могут быть пустыми");
        }
        if (xValues.size() != yValues.size()) {
            throw new IllegalArgumentException("X и Y массивы должны иметь одинаковый размер");
        }
        if (xValues.size() < 2) {
            throw new IllegalArgumentException("Требуется минимум 2 точки");
        }
        for (int i = 1; i < xValues.size(); i++) {
            if (xValues.get(i) <= xValues.get(i - 1)) {
                throw new IllegalArgumentException("X значения должны строго возрастать");
            }
        }

        User currentUser = getCurrentUser();
        String functionName = (name != null && !name.trim().isEmpty()) ? name : "Табулированная " + System.currentTimeMillis();

        // Создаём функцию (как в оригинале)
        Function function = new Function();
        function.setName(functionName);
        function.setExpression("ТАБУЛИРОВАННАЯ ФУНКЦИЯ: " + xValues.size() + " точек из массивов");
        function.setUser(currentUser);
        function.setCreatedAt(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);

        // Создаём точки (как в оригинале)
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            Point point = new Point();
            point.setXValue(xValues.get(i));
            point.setYValue(yValues.get(i));
            point.setFunction(savedFunction);
            point.setUser(currentUser);
            points.add(point);
        }
        pointRepository.saveAll(points);
        savedFunction.setPoints(points);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("TABULATED_FROM_POINTS", durationMs, points.size(), "SPRING_DATA_JPA"));

        return convertToResponse(savedFunction);
    }

    /**
     * СОЗДАНИЕ ТАБУЛИРОВАННОЙ ФУНКЦИИ ПО МАТЕМАТИЧЕСКОЙ ФОРМУЛЕ
     */
    public FunctionResponse createTabulatedFunctionFromMath(String name, String mathFunctionType,
                                                            double fromX, double toX, int pointsCount) {
        long startTime = System.nanoTime();

        // Валидация (как в оригинале)
        if (fromX >= toX) {
            throw new IllegalArgumentException("fromX должен быть меньше toX");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("pointsCount должен быть минимум 2");
        }

        User currentUser = getCurrentUser();
        String functionName = (name != null && !name.trim().isEmpty()) ? name :
                mathFunctionType + "_[" + fromX + ";" + toX + "]_" + System.currentTimeMillis();

        // Создаём функцию
        Function function = new Function();
        function.setName(functionName);
        function.setExpression(String.format("ТАБУЛИРОВАННАЯ: %s(x) на [%s, %s], %d точек",
                mathFunctionType, fromX, toX, pointsCount));
        function.setUser(currentUser);
        function.setCreatedAt(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);

        // Генерируем точки (используем существующую логику!)
        List<Point> points = new ArrayList<>();
        double step = (toX - fromX) / (pointsCount - 1);

        for (int i = 0; i < pointsCount; i++) {
            double x = fromX + i * step;
            // ✅ ИСПОЛЬЗУЕМ ТВОЮ СУЩЕСТВУЮЩУЮ calculateMathFunction()!
            double y = calculateMathFunction(mathFunctionType, x, 1.0, 0.0, 0.0);

            Point point = new Point();
            point.setXValue(x);
            point.setYValue(y);
            point.setFunction(savedFunction);
            point.setUser(currentUser);
            points.add(point);
        }

        pointRepository.saveAll(points);
        savedFunction.setPoints(points);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        performanceMetrics.add(new PerformanceMetrics("TABULATED_FROM_MATH", durationMs, points.size(), "SPRING_DATA_JPA"));

        return convertToResponse(savedFunction);
    }


    private <T> T getFieldValue(Object obj, String fieldName, Class<T> type) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return type.cast(field.get(obj));
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            return null;
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private FunctionResponse convertToResponse(Function function) {
        if (function == null) return null;
        FunctionResponse response = new FunctionResponse();
        response.setId(function.getId());
        response.setName(function.getName());
        response.setExpression(function.getExpression());
        response.setCreatedAt(function.getCreatedAt());
        if (function.getPoints() != null) {
            List<com.example.LAB5.DTO.PointDTO> pointDTOs = function.getPoints().stream()
                    .map(point -> {
                        com.example.LAB5.DTO.PointDTO dto = new com.example.LAB5.DTO.PointDTO();
                        dto.setX(point.getXValue());
                        dto.setY(point.getYValue());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setPoints(pointDTOs);
        }
        if (function.getUser() != null) {
            response.setUserId(function.getUser().getId());
            response.setUsername(function.getUser().getUsername());
        }
        return response;
    }

    private FunctionDTO convertToDTO(Function function) {
        if (function == null) return null;
        FunctionDTO dto = new FunctionDTO();
        dto.setId(function.getId());
        dto.setName(function.getName());
        dto.setExpression(function.getExpression());
        dto.setCreatedAt(function.getCreatedAt());
        if (function.getPoints() != null) {
            List<com.example.LAB5.DTO.PointDTO> pointDTOs = function.getPoints().stream()
                    .map(point -> {
                        com.example.LAB5.DTO.PointDTO pointDTO = new com.example.LAB5.DTO.PointDTO();
                        pointDTO.setX(point.getXValue());
                        pointDTO.setY(point.getYValue());
                        return pointDTO;
                    })
                    .collect(Collectors.toList());
            dto.setPoints(pointDTOs);
        }
        return dto;
    }
}