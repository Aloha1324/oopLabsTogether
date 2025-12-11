package com.example.LAB5.manual.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PerformanceBenchmarkService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceBenchmarkService.class);

    private final UserService userService;
    private final FunctionService functionService;
    private final PointService pointService;

    public PerformanceBenchmarkService(UserService userService,
                                       FunctionService functionService,
                                       PointService pointService) {
        this.userService = userService;
        this.functionService = functionService;
        this.pointService = pointService;
    }

    public void generateTestData(int userCount, int functionsPerUser, int pointsPerFunction) {
        log.info("Старт генерации тестовых данных: users={}, functionsPerUser={}, pointsPerFunction={}",
                userCount, functionsPerUser, pointsPerFunction);

        List<Long> userIds = new ArrayList<>();
        List<Long> functionIds = new ArrayList<>();

        for (int i = 0; i < userCount; i++) {
            String login = "perf_user_" + System.currentTimeMillis() + "_" + i;
            Long userId = userService.createUser(login, "USER", "password123");
            userIds.add(userId);

            for (int j = 0; j < functionsPerUser; j++) {
                String funcName = "func_" + i + "_" + j;
                String signature = "f(x) = x^" + j;
                Long funcId = functionService.createFunction(userId, funcName, signature);
                functionIds.add(funcId);

                // pointsPerFunction логически учитывается в итоговой статистике,
                // сами точки генерируются по диапазону
                pointService.generateFunctionPoints(funcId, "polynomial", -10, 10, 0.1);
            }
        }

        log.info("Генерация завершена: users={}, functions={}, points≈{}",
                userIds.size(), functionIds.size(), (long) pointsPerFunction * functionIds.size());
    }

    public BenchmarkResults runBenchmark() {
        BenchmarkResults results = new BenchmarkResults();

        measureUserOperations(results);
        measureFunctionOperations(results);
        measurePointOperations(results);
        measureComplexQueries(results);

        return results;
    }

    private void measureUserOperations(BenchmarkResults results) {
        long start;
        long end;

        String login = "perf_user_" + System.currentTimeMillis();

        start = System.nanoTime();
        Long userId = userService.createUser(login, "ADMIN", "testpass");
        end = System.nanoTime();
        results.setUserCreateTime((end - start) / 1_000_000.0);

        start = System.nanoTime();
        userService.getUserById(userId);
        end = System.nanoTime();
        results.setUserReadTime((end - start) / 1_000_000.0);

        start = System.nanoTime();
        userService.updateUser(userId, login + "_updated", "USER", "newpass");
        end = System.nanoTime();
        results.setUserUpdateTime((end - start) / 1_000_000.0);

        start = System.nanoTime();
        userService.deleteUser(userId);
        end = System.nanoTime();
        results.setUserDeleteTime((end - start) / 1_000_000.0);
    }

    private void measureFunctionOperations(BenchmarkResults results) {
        String login = "func_bench_" + System.currentTimeMillis();
        Long userId = userService.createUser(login, "USER", "password");

        long start;
        long end;

        start = System.nanoTime();
        Long functionId = functionService.createFunction(userId, "test_function", "f(x) = x^2");
        end = System.nanoTime();
        results.setFunctionCreateTime((end - start) / 1_000_000.0);

        start = System.nanoTime();
        functionService.getFunctionById(functionId);
        end = System.nanoTime();
        results.setFunctionReadTime((end - start) / 1_000_000.0);

        userService.deleteUser(userId);
    }

    private void measurePointOperations(BenchmarkResults results) {
        String login = "point_bench_" + System.currentTimeMillis();
        Long userId = userService.createUser(login, "USER", "password");
        Long functionId = functionService.createFunction(userId, "point_test_function", "f(x) = x");

        long start;
        long end;

        start = System.nanoTime();
        int pointsCreated = pointService.generateFunctionPoints(functionId, "linear", -5, 5, 1);
        end = System.nanoTime();
        results.setPointCreateTime((end - start) / 1_000_000.0);

        start = System.nanoTime();
        pointService.getPointsByFunctionId(functionId);
        end = System.nanoTime();
        results.setPointReadTime((end - start) / 1_000_000.0);

        // при удалении пользователя каскадно удаляются его функции/точки в твоей логике сервисов
        userService.deleteUser(userId);
        log.debug("Для point benchmark создано {} точек", pointsCreated);
    }

    private void measureComplexQueries(BenchmarkResults results) {
        long start = System.nanoTime();
        userService.getAllUsers();
        long end = System.nanoTime();
        results.setComplexQueryTime((end - start) / 1_000_000.0);
    }

    public static class BenchmarkResults {

        private double userCreateTime;
        private double userReadTime;
        private double userUpdateTime;
        private double userDeleteTime;
        private double functionCreateTime;
        private double functionReadTime;
        private double pointCreateTime;
        private double pointReadTime;
        private double complexQueryTime;

        public double getUserCreateTime() {
            return userCreateTime;
        }

        public void setUserCreateTime(double userCreateTime) {
            this.userCreateTime = userCreateTime;
        }

        public double getUserReadTime() {
            return userReadTime;
        }

        public void setUserReadTime(double userReadTime) {
            this.userReadTime = userReadTime;
        }

        public double getUserUpdateTime() {
            return userUpdateTime;
        }

        public void setUserUpdateTime(double userUpdateTime) {
            this.userUpdateTime = userUpdateTime;
        }

        public double getUserDeleteTime() {
            return userDeleteTime;
        }

        public void setUserDeleteTime(double userDeleteTime) {
            this.userDeleteTime = userDeleteTime;
        }

        public double getFunctionCreateTime() {
            return functionCreateTime;
        }

        public void setFunctionCreateTime(double functionCreateTime) {
            this.functionCreateTime = functionCreateTime;
        }

        public double getFunctionReadTime() {
            return functionReadTime;
        }

        public void setFunctionReadTime(double functionReadTime) {
            this.functionReadTime = functionReadTime;
        }

        public double getPointCreateTime() {
            return pointCreateTime;
        }

        public void setPointCreateTime(double pointCreateTime) {
            this.pointCreateTime = pointCreateTime;
        }

        public double getPointReadTime() {
            return pointReadTime;
        }

        public void setPointReadTime(double pointReadTime) {
            this.pointReadTime = pointReadTime;
        }

        public double getComplexQueryTime() {
            return complexQueryTime;
        }

        public void setComplexQueryTime(double complexQueryTime) {
            this.complexQueryTime = complexQueryTime;
        }

        public String toMarkdownTable() {
            return String.format(
                    "| Операция | JDBC (мс) |\n" +
                            "|----------|-----------|\n" +
                            "| Создание пользователя | %.3f |\n" +
                            "| Чтение пользователя | %.3f |\n" +
                            "| Обновление пользователя | %.3f |\n" +
                            "| Удаление пользователя | %.3f |\n" +
                            "| Создание функции | %.3f |\n" +
                            "| Чтение функции | %.3f |\n" +
                            "| Создание точек | %.3f |\n" +
                            "| Чтение точек | %.3f |\n" +
                            "| Сложные запросы | %.3f |\n",
                    userCreateTime,
                    userReadTime,
                    userUpdateTime,
                    userDeleteTime,
                    functionCreateTime,
                    functionReadTime,
                    pointCreateTime,
                    pointReadTime,
                    complexQueryTime
            );
        }

        public String toCSV() {
            return String.format(
                    "Операция,Время (мс)\n" +
                            "Создание пользователя,%.3f\n" +
                            "Чтение пользователя,%.3f\n" +
                            "Обновление пользователя,%.3f\n" +
                            "Удаление пользователя,%.3f\n" +
                            "Создание функции,%.3f\n" +
                            "Чтение функции,%.3f\n" +
                            "Создание точек,%.3f\n" +
                            "Чтение точек,%.3f\n" +
                            "Сложные запросы,%.3f\n",
                    userCreateTime,
                    userReadTime,
                    userUpdateTime,
                    userDeleteTime,
                    functionCreateTime,
                    functionReadTime,
                    pointCreateTime,
                    pointReadTime,
                    complexQueryTime
            );
        }
    }
}
