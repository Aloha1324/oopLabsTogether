package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DAO.FunctionDAO;
import com.example.LAB5.manual.DAO.PointDAO;
import com.example.LAB5.manual.DAO.UserDAO;
import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class FunctionService {

    private static final Logger log = LoggerFactory.getLogger(FunctionService.class);

    private final FunctionDAO functionDao;
    private final UserDAO userDao;
    private final PointDAO pointDao;

    public FunctionService() {
        this(new FunctionDAO(), new UserDAO(), new PointDAO());
    }

    public FunctionService(FunctionDAO functionDao, UserDAO userDao, PointDAO pointDao) {
        this.functionDao = functionDao;
        this.userDao = userDao;
        this.pointDao = pointDao;
    }

    public Long createFunction(Long userId, String name, String signature) {
        log.info("Запрос на создание функции: userId={}, name={}", userId, name);

        if (userDao.findById(userId).isEmpty()) {
            log.error("Пользователь с id {} не найден, функция не создаётся", userId);
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        FunctionDTO dto = new FunctionDTO(userId, name, signature);
        Long id = functionDao.createFunction(dto);
        log.info("Функция создана, id={}", id);
        return id;
    }

    public Optional<FunctionDTO> getFunctionById(Long id) {
        log.debug("Чтение функции по id {}", id);
        return functionDao.findById(id);
    }

    public List<FunctionDTO> getFunctionsByUserId(Long userId) {
        log.debug("Чтение функций пользователя {}", userId);
        return functionDao.findByUserId(userId);
    }

    public List<FunctionDTO> getFunctionsByName(String name) {
        log.debug("Поиск функций по имени '{}'", name);
        return functionDao.findByName(name);
    }

    public List<FunctionDTO> getAllFunctions() {
        log.debug("Чтение всех функций");
        return functionDao.findAll();
    }

    public List<FunctionDTO> getFunctionsWithPointCount() {
        log.debug("Чтение функций и вывод количества точек");
        List<FunctionDTO> functions = functionDao.findAll();

        for (FunctionDTO f : functions) {
            List<PointDTO> points = pointDao.findByFunctionId(f.getId());
            log.debug("Функция '{}' (id={}) содержит {} точек",
                    f.getName(), f.getId(), points.size());
        }

        return functions;
    }

    public boolean updateFunction(Long functionId, Long userId, String name, String signature) {
        log.info("Обновление функции id={}", functionId);

        Optional<FunctionDTO> current = functionDao.findById(functionId);
        if (current.isEmpty()) {
            log.warn("Функция с id {} не найдена для обновления", functionId);
            return false;
        }

        if (userDao.findById(userId).isEmpty()) {
            log.error("Невозможно обновить функцию: пользователь {} не существует", userId);
            return false;
        }

        FunctionDTO dto = current.get();
        dto.setUserId(userId);
        dto.setName(name);
        dto.setSignature(signature);

        boolean updated = functionDao.updateFunction(dto);
        if (updated) {
            log.info("Функция id={} успешно обновлена", functionId);
        }
        return updated;
    }

    public boolean updateFunctionSignature(Long functionId, String newSignature) {
        log.info("Обновление сигнатуры функции id={}", functionId);

        Optional<FunctionDTO> current = functionDao.findById(functionId);
        if (current.isEmpty()) {
            log.warn("Функция id={} не найдена для изменения сигнатуры", functionId);
            return false;
        }

        FunctionDTO dto = current.get();
        dto.setSignature(newSignature);

        boolean updated = functionDao.updateFunction(dto);
        if (updated) {
            log.info("Сигнатура функции id={} обновлена", functionId);
        }
        return updated;
    }

    public boolean deleteFunction(Long functionId) {
        log.info("Удаление функции id={}", functionId);

        int removedPoints = pointDao.deleteByFunctionId(functionId);
        log.info("Удалено {} точек функции id={}", removedPoints, functionId);

        boolean removedFunction = functionDao.deleteFunction(functionId);
        if (removedFunction) {
            log.info("Функция id={} успешно удалена вместе с точками", functionId);
        } else {
            log.warn("Функция id={} не найдена при удалении", functionId);
        }
        return removedFunction;
    }

    public int deleteFunctionsByUserId(Long userId) {
        log.info("Удаление всех функций пользователя id={}", userId);

        List<FunctionDTO> list = functionDao.findByUserId(userId);
        int deleted = 0;

        for (FunctionDTO f : list) {
            if (deleteFunction(f.getId())) {
                deleted++;
            }
        }

        log.info("Для пользователя id={} удалено функций: {}", userId, deleted);
        return deleted;
    }

    public boolean validateFunctionName(Long userId, String functionName) {
        log.debug("Проверка имени функции '{}' для пользователя {}", functionName, userId);
        return functionDao.findByUserId(userId).stream()
                .noneMatch(f -> f.getName().equalsIgnoreCase(functionName));
    }

    public FunctionStatistics getFunctionStatistics(Long functionId) {
        log.debug("Формирование статистики по функции id={}", functionId);

        Optional<FunctionDTO> functionOpt = functionDao.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Функция id={} не найдена для статистики", functionId);
            return null;
        }

        List<PointDTO> points = pointDao.findByFunctionId(functionId);
        double minX = points.stream().mapToDouble(PointDTO::getXValue).min().orElse(0);
        double maxX = points.stream().mapToDouble(PointDTO::getXValue).max().orElse(0);
        double minY = points.stream().mapToDouble(PointDTO::getYValue).min().orElse(0);
        double maxY = points.stream().mapToDouble(PointDTO::getYValue).max().orElse(0);
        double avgY = points.stream().mapToDouble(PointDTO::getYValue).average().orElse(0);

        FunctionDTO func = functionOpt.get();
        FunctionStatistics stats = new FunctionStatistics(
                functionId,
                func.getName(),
                points.size(),
                minX,
                maxX,
                minY,
                maxY,
                avgY
        );

        log.info("Статистика функции '{}': {} точек, x=[{}, {}], y=[{}, {}], avgY={}",
                func.getName(), points.size(), minX, maxX, minY, maxY, avgY);

        return stats;
    }

    public static class FunctionStatistics {

        private final Long functionId;
        private final String functionName;
        private final int pointCount;
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;
        private final double averageY;

        public FunctionStatistics(Long functionId,
                                  String functionName,
                                  int pointCount,
                                  double minX,
                                  double maxX,
                                  double minY,
                                  double maxY,
                                  double averageY) {
            this.functionId = functionId;
            this.functionName = functionName;
            this.pointCount = pointCount;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.averageY = averageY;
        }

        public Long getFunctionId() {
            return functionId;
        }

        public String getFunctionName() {
            return functionName;
        }

        public int getPointCount() {
            return pointCount;
        }

        public double getMinX() {
            return minX;
        }

        public double getMaxX() {
            return maxX;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getAverageY() {
            return averageY;
        }

        @Override
        public String toString() {
            return String.format(
                    "FunctionStatistics{name='%s', points=%d, x=[%.2f, %.2f], y=[%.2f, %.2f], avgY=%.2f}",
                    functionName, pointCount, minX, maxX, minY, maxY, averageY
            );
        }
    }
}
