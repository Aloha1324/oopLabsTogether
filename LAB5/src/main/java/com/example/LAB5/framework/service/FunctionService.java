package com.example.LAB5.framework.service;

import com.example.LAB5.framework.entity.Function;
import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.repository.FunctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class FunctionService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);

    private final FunctionRepository functionRepository;

    @Autowired
    public FunctionService(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    // Класс для измерения производительности
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

    private final List<PerformanceMetrics> performanceMetrics = new ArrayList<>();

    // === НЕОБХОДИМЫЕ МЕТОДЫ ДЛЯ FrameworkPerformanceComparisonService ===

    /**
     * Получение функции по ID с возвратом объекта или null
     * Для совместимости с FrameworkPerformanceComparisonService
     */
    public Function getFunctionByIdOrNull(Long id) {
        long startTime = System.nanoTime();
        logger.debug("Поиск функции по ID (OrNull): {}", id);

        Optional<Function> result = functionRepository.findById(id);
        Function function = result.orElse(null);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_FUNCTION_BY_ID_OR_NULL", durationMs, function != null ? 1 : 0, "SPRING_DATA_JPA"));

        logger.debug("Функция {} найдена за {} мс", id, durationMs);
        return function;
    }

    /**
     * Получение функции по ID (основной метод) - ТОЛЬКО ОДИН МЕТОД!
     */
    public Function getFunctionById(Long id) {
        long startTime = System.nanoTime();
        logger.debug("Получение функции по ID: {}", id);

        Optional<Function> functionOpt = functionRepository.findById(id);

        if (functionOpt.isPresent()) {
            Function function = functionOpt.get();

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("GET_FUNCTION_BY_ID", durationMs, 1, "SPRING_DATA_JPA"));
            logger.debug("Функция с ID {} найдена за {} мс", id, durationMs);

            return function;
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_FUNCTION_BY_ID", durationMs, 0, "SPRING_DATA_JPA"));
        logger.warn("Функция с ID {} не найдена (поиск занял {} мс)", id, durationMs);

        throw new RuntimeException("Function not found with id: " + id);
    }

    /**
     * Получение Optional функции по ID (без исключения)
     */
    public Optional<Function> findFunctionById(Long id) {
        long startTime = System.nanoTime();
        logger.debug("Поиск функции по ID (Optional): {}", id);

        Optional<Function> result = functionRepository.findById(id);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("FIND_FUNCTION_BY_ID", durationMs, result.isPresent() ? 1 : 0, "SPRING_DATA_JPA"));
        logger.debug("Поиск функции по ID {} завершен за {} мс (найдено: {})",
                id, durationMs, result.isPresent());

        return result;
    }

    /**
     * Создание функции
     */
    public Function createFunction(User user, String name, String expression) {
        long startTime = System.nanoTime();
        logger.info("Создание функции: name={}, user={}", name, user.getUsername());

        Function function = new Function();
        function.setName(name);
        function.setExpression(expression);
        function.setUser(user);
        function.setCreatedAt(LocalDateTime.now());

        Function savedFunction = functionRepository.save(function);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("CREATE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));

        logger.info("Создана функция с ID: {} за {} мс", savedFunction.getId(), durationMs);
        return savedFunction;
    }

    /**
     * Сохранение функции (добавлен недостающий метод)
     */
    public Function saveFunction(Function function) {
        long startTime = System.nanoTime();
        logger.info("Сохранение функции: {}", function.getName());

        Function savedFunction = functionRepository.save(function);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("SAVE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));

        logger.info("Функция сохранена с ID: {} за {} мс", savedFunction.getId(), durationMs);
        return savedFunction;
    }

    /**
     * Получение всех функций
     */
    public List<Function> getAllFunctions() {
        long startTime = System.nanoTime();
        logger.debug("Получение всех функций");

        List<Function> result = functionRepository.findAll();

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_ALL_FUNCTIONS", durationMs, result.size(), "SPRING_DATA_JPA"));

        logger.info("Получено {} функций за {} мс", result.size(), durationMs);
        return result;
    }

    /**
     * Получение функций по ID пользователя
     */
    public List<Function> getFunctionsByUserId(Long userId) {
        long startTime = System.nanoTime();
        logger.debug("Получение функций пользователя ID: {}", userId);

        List<Function> result = functionRepository.findByUserId(userId);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_FUNCTIONS_BY_USER_ID", durationMs, result.size(), "SPRING_DATA_JPA"));

        logger.debug("Получено {} функций пользователя ID {} за {} мс", result.size(), userId, durationMs);
        return result;
    }

    /**
     * Обновление функции
     */
    public Function updateFunction(Long id, String name, String expression) {
        long startTime = System.nanoTime();
        logger.info("Обновление функции с ID: {}", id);

        Optional<Function> existingFunction = functionRepository.findById(id);
        if (existingFunction.isPresent()) {
            Function function = existingFunction.get();
            function.setName(name);
            function.setExpression(expression);

            Function updatedFunction = functionRepository.save(function);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("UPDATE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));

            logger.info("Функция с ID {} успешно обновлена за {} мс", id, durationMs);
            return updatedFunction;
        }

        logger.warn("Функция с ID {} не найдена для обновления", id);
        return null;
    }

    /**
     * Удаление функции
     */
    public boolean deleteFunction(Long id) {
        long startTime = System.nanoTime();
        logger.info("Удаление функции с ID: {}", id);

        Optional<Function> function = functionRepository.findById(id);
        if (function.isPresent()) {
            functionRepository.delete(function.get());

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("DELETE_FUNCTION", durationMs, 1, "SPRING_DATA_JPA"));

            logger.info("Функция с ID {} удалена за {} мс", id, durationMs);
            return true;
        }

        logger.warn("Функция с ID {} не найдена для удаления", id);
        return false;
    }

    // === ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ===

    public List<Function> getFunctionsByName(String name) {
        return functionRepository.findByNameContainingIgnoreCase(name);
    }

    public int getTotalFunctionsCount() {
        return (int) functionRepository.count();
    }

    /**
     * Проверка существования функции по ID
     */
    public boolean functionExists(Long id) {
        long startTime = System.nanoTime();

        boolean exists = functionRepository.existsById(id);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("FUNCTION_EXISTS", durationMs, exists ? 1 : 0, "SPRING_DATA_JPA"));
        logger.debug("Проверка существования функции ID {}: {} (за {} мс)", id, exists, durationMs);

        return exists;
    }

    // Методы для работы с метриками
    public List<PerformanceMetrics> getPerformanceMetrics() {
        return new ArrayList<>(performanceMetrics);
    }

    public void clearPerformanceMetrics() {
        performanceMetrics.clear();
        logger.info("Метрики производительности функций очищены");
    }

    // === STREAMING МЕТОДЫ (опционально) ===

    @Transactional(readOnly = true)
    public List<Function> getAllFunctionsStreaming() {
        long startTime = System.nanoTime();
        logger.debug("Получение всех функций через streaming");

        List<Function> result;

        try {
            // Если есть метод streamAll в репозитории
            try (Stream<Function> functionStream = functionRepository.findAll().stream()) {
                result = functionStream.collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Ошибка при streaming функций: {}", e.getMessage());
            // Fallback to standard method
            return getAllFunctions();
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_ALL_FUNCTIONS_STREAMING", durationMs, result.size(), "SPRING_DATA_JPA"));

        logger.info("Получено {} функций через streaming за {} мс", result.size(), durationMs);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Function> getAllFunctionsStreamingWithBatch(int batchSize) {
        long startTime = System.nanoTime();
        logger.debug("Получение всех функций через streaming с batch размером: {}", batchSize);

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
                    if (currentCount % 5000 == 0) {
                        logger.debug("Обработано {} функций...", currentCount);
                    }
                }
            });

            if (!batch.isEmpty()) {
                result.addAll(batch);
            }

        } catch (Exception e) {
            logger.error("Ошибка при streaming функций с batch: {}", e.getMessage());
            return getAllFunctionsStreaming();
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_ALL_FUNCTIONS_STREAMING_BATCH", durationMs, result.size(), "SPRING_DATA_JPA"));

        logger.info("Получено {} функций через streaming с batch за {} мс", result.size(), durationMs);
        return result;
    }

    /**
     * Обработка функций в streaming режиме с использованием FunctionProcessor
     */
    public StreamingProcessingResult processFunctionsStreaming(FunctionProcessor processor) {
        long startTime = System.nanoTime();
        logger.debug("Начало streaming обработки функций");

        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try (Stream<Function> functionStream = functionRepository.findAll().stream()) {
            functionStream.forEach(function -> {
                try {
                    processor.process(function);
                    processedCount.incrementAndGet();

                    if (processedCount.get() % 1000 == 0) {
                        logger.debug("Обработано {} функций...", processedCount.get());
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    logger.error("Ошибка при обработке функции ID {}: {}", function.getId(), e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка при streaming обработке функций: {}", e.getMessage());
        }

        long endTime = System.nanoTime();
        long processingTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        StreamingProcessingResult result = new StreamingProcessingResult(
                processedCount.get(),
                errorCount.get(),
                processingTime
        );

        performanceMetrics.add(new PerformanceMetrics("PROCESS_FUNCTIONS_STREAMING", processingTime, processedCount.get(), "SPRING_DATA_JPA"));

        logger.info("Streaming обработка завершена: {}", result);
        return result;
    }

    // Интерфейс для обработки функций в streaming режиме
    @FunctionalInterface
    public interface FunctionProcessor {
        void process(Function function);
    }

    // Класс для результатов streaming обработки
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
}
