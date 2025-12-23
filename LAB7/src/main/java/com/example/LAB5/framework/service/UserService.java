package com.example.LAB5.framework.service;

import com.example.LAB5.framework.entity.User;
import com.example.LAB5.framework.repository.FunctionRepository;
import com.example.LAB5.framework.repository.PointRepository;
import com.example.LAB5.framework.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    // Конструкторное внедрение зависимостей
    @Autowired
    public UserService(UserRepository userRepository, FunctionRepository functionRepository, PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
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

        // Геттеры
        public String getOperationName() { return operationName; }
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

    private final java.util.List<PerformanceMetrics> performanceMetrics = new java.util.ArrayList<>();

    public User createUser(String username, String passwordHash) {
        long startTime = System.nanoTime();

        logger.info("Создание пользователя: username={}", username);

        // Проверяем, существует ли пользователь с таким именем
        if (userRepository.findByUsername(username).isPresent()) {
            logger.error("Пользователь с именем {} уже существует", username);
            throw new IllegalArgumentException("User with username " + username + " already exists");
        }

        User user = new User(username, passwordHash);
        User savedUser = userRepository.save(user);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("CREATE_USER", durationMs, 1, "SPRING_DATA_JPA"));

        logger.info("Создан пользователь с ID: {} за {} мс", savedUser.getId(), durationMs);
        return savedUser;
    }

    public Optional<User> getUserById(Long id) {
        long startTime = System.nanoTime();
        logger.debug("Поиск пользователя по ID: {}", id);

        Optional<User> result = userRepository.findById(id);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_USER_BY_ID", durationMs, result.isPresent() ? 1 : 0, "SPRING_DATA_JPA"));

        return result;
    }
    public User getUserByIdOrNull(Long id) {
        long startTime = System.nanoTime();
        logger.debug("Поиск пользователя по ID (OrNull): {}", id);

        Optional<User> result = userRepository.findById(id);
        User user = result.orElse(null);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_USER_BY_ID_OR_NULL", durationMs, user != null ? 1 : 0, "SPRING_DATA_JPA"));

        return user;
    }

    /**
     * Получение пользователя по имени с возвратом объекта или null
     * Для совместимости с FrameworkPerformanceComparisonService
     */
    public User getUserByUsernameOrNull(String username) {
        long startTime = System.nanoTime();
        logger.debug("Поиск пользователя по имени (OrNull): {}", username);

        Optional<User> result = userRepository.findByUsername(username);
        User user = result.orElse(null);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_USER_BY_USERNAME_OR_NULL", durationMs, user != null ? 1 : 0, "SPRING_DATA_JPA"));

        return user;
    }

    public Optional<User> findByUsername(String username) {
        long startTime = System.nanoTime();
        logger.debug("Поиск пользователя по имени (findByUsername): {}", username);

        Optional<User> result = userRepository.findByUsername(username);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("FIND_USER_BY_USERNAME", durationMs, result.isPresent() ? 1 : 0, "SPRING_DATA_JPA"));

        return result;
    }

    public Optional<User> getUserByUsername(String username) {
        long startTime = System.nanoTime();
        logger.debug("Поиск пользователя по имени: {}", username);

        Optional<User> result = userRepository.findByUsername(username);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_USER_BY_USERNAME", durationMs, result.isPresent() ? 1 : 0, "SPRING_DATA_JPA"));

        return result;
    }


    public List<User> getUsersByUsername(String username) {
        long startTime = System.nanoTime();
        logger.debug("Поиск пользователей по имени (частичное совпадение): {}", username);


        List<User> result = userRepository.findByUsernameContainingIgnoreCase(username);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_USERS_BY_USERNAME", durationMs, result.size(), "SPRING_DATA_JPA"));

        return result;
    }

    public List<User> getAllUsers() {
        long startTime = System.nanoTime();
        logger.debug("Получение всех пользователей");

        List<User> result = userRepository.findAll();

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_ALL_USERS", durationMs, result.size(), "SPRING_DATA_JPA"));

        return result;
    }

    public User updateUser(Long id, String username, String passwordHash) {
        long startTime = System.nanoTime();
        logger.info("Обновление пользователя с ID: {}", id);

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // Проверяем, не занято ли новое имя пользователя другим пользователем
            if (!user.getUsername().equals(username)) {
                Optional<User> userWithSameUsername = userRepository.findByUsername(username);
                if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
                    logger.error("Пользователь с именем {} уже существует", username);
                    return null;
                }
            }

            user.setUsername(username);
            user.setPassword(passwordHash);

            User updatedUser = userRepository.save(user);

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("UPDATE_USER", durationMs, 1, "SPRING_DATA_JPA"));

            logger.info("Пользователь с ID {} успешно обновлен за {} мс", id, durationMs);
            return updatedUser;
        }

        logger.warn("Пользователь с ID {} не найден для обновления", id);
        return null;
    }

    @Transactional
    public boolean deleteUser(Long id) {
        long startTime = System.nanoTime();
        logger.info("Удаление пользователя с ID: {}", id);

        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String username = user.getUsername();

                // Получаем статистику для метрик
                int functionsCount = functionRepository.findByUserId(id).size();
                int pointsCount = pointRepository.findByUserId(id).size();

                // 1. Сначала удаляем все точки пользователя
                pointRepository.deleteByUserId(id);
                logger.debug("Удалены точки пользователя {}", username);

                // 2. Затем удаляем все функции пользователя
                functionRepository.deleteByUserId(id);
                logger.debug("Удалены функции пользователя {}", username);

                // 3. Наконец удаляем самого пользователя
                userRepository.delete(user);

                long endTime = System.nanoTime();
                long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                performanceMetrics.add(new PerformanceMetrics("DELETE_USER", durationMs, 1 + functionsCount + pointsCount, "SPRING_DATA_JPA"));

                logger.info("Пользователь {} с ID {} и все связанные данные ({} функций, {} точек) удалены за {} мс",
                        username, id, functionsCount, pointsCount, durationMs);
                return true;
            }

            logger.warn("Пользователь с ID {} не найден для удаления", id);
            return false;

        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с ID {}: {}", id, e.getMessage());
            return false;
        }
    }

    public boolean validateUserCredentials(String username, String passwordHash) {
        long startTime = System.nanoTime();
        logger.debug("Проверка учетных данных для пользователя: {}", username);

        Optional<User> user = userRepository.findByUsername(username);
        boolean isValid = user.isPresent() && user.get().getPassword().equals(passwordHash);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("VALIDATE_CREDENTIALS", durationMs, 1, "SPRING_DATA_JPA"));

        return isValid;
    }

    // Дополнительные методы для тестирования производительности

    public int getTotalUsersCount() {
        long startTime = System.nanoTime();

        int count = (int) userRepository.count();

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("GET_TOTAL_USERS_COUNT", durationMs, 1, "SPRING_DATA_JPA"));

        return count;
    }

    public boolean isUsernameAvailable(String username) {
        long startTime = System.nanoTime();

        boolean isAvailable = userRepository.findByUsername(username).isEmpty();

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("CHECK_USERNAME_AVAILABILITY", durationMs, 1, "SPRING_DATA_JPA"));

        return isAvailable;
    }

    public UserStatistics getUserStatistics(Long userId) {
        long startTime = System.nanoTime();
        logger.debug("Получение статистики для пользователя с ID: {}", userId);

        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            int functionsCount = functionRepository.findByUserId(userId).size();
            int pointsCount = pointRepository.findByUserId(userId).size();

            UserStatistics stats = new UserStatistics(
                    userId,
                    user.get().getUsername(),
                    functionsCount,
                    pointsCount
            );

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            performanceMetrics.add(new PerformanceMetrics("GET_USER_STATISTICS", durationMs, 1, "SPRING_DATA_JPA"));

            logger.info("Статистика пользователя {}: {} функций, {} точек за {} мс",
                    user.get().getUsername(), functionsCount, pointsCount, durationMs);

            return stats;
        }

        logger.warn("Пользователь с ID {} не найден для статистики", userId);
        return null;
    }

    // Пакетное создание пользователей для тестирования
    public int createUsersBatch(List<User> users) {
        long startTime = System.nanoTime();
        logger.info("Пакетное создание {} пользователей", users.size());

        // Fix for isEmpty() and size() methods
        if (users == null || users.isEmpty()) {
            logger.warn("Attempted to create empty batch of users");
            return 0;
        }

        userRepository.saveAll(users);

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        performanceMetrics.add(new PerformanceMetrics("BATCH_CREATE_USERS", durationMs, users.size(), "SPRING_DATA_JPA"));

        logger.info("Пакетно создано {} пользователей за {} мс", users.size(), durationMs);
        return users.size();
    }

    // Методы для работы с метриками производительности
    public List<PerformanceMetrics> getPerformanceMetrics() {
        return new java.util.ArrayList<>(performanceMetrics);
    }

    public void clearPerformanceMetrics() {
        performanceMetrics.clear();
        logger.info("Метрики производительности пользователей очищены");
    }

    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== USER SERVICE PERFORMANCE REPORT (SPRING DATA JPA) ===\n");

        for (PerformanceMetrics metric : performanceMetrics) {
            report.append(metric.toString()).append("\n");
        }

        // Сводная статистика
        report.append("\n=== SUMMARY ===\n");
        report.append(String.format("Total operations: %d\n", performanceMetrics.size()));
        report.append(String.format("Total users processed: %d\n",
                performanceMetrics.stream().mapToInt(PerformanceMetrics::getRecordsProcessed).sum()));
        report.append(String.format("Total execution time: %d ms\n",
                performanceMetrics.stream().mapToLong(PerformanceMetrics::getExecutionTimeMs).sum()));

        return report.toString();
    }


    public static class UserStatistics {
        private final Long userId;
        private final String username;
        private final int functionsCount;
        private final int pointsCount;

        public UserStatistics(Long userId, String username, int functionsCount, int pointsCount) {
            this.userId = userId;
            this.username = username;
            this.functionsCount = functionsCount;
            this.pointsCount = pointsCount;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public int getFunctionsCount() { return functionsCount; }
        public int getPointsCount() { return pointsCount; }

        @Override
        public String toString() {
            return String.format(
                    "UserStatistics{user='%s', functions=%d, points=%d}",
                    username, functionsCount, pointsCount
            );
        }
    }
}
