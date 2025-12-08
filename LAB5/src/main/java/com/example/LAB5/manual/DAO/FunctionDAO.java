package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionDAO {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDAO.class);

    // Внедрение зависимости PointDAO для безопасного удаления
    private PointDAO pointDAO;

    public FunctionDAO() {
        this.pointDAO = new PointDAO();
    }

    // Для тестирования можно добавить конструктор с внедрением зависимости
    public FunctionDAO(PointDAO pointDAO) {
        this.pointDAO = pointDAO;
    }

    // CREATE
    public Long createFunction(String name, Long userId, String expression) {
        String sql = "INSERT INTO functions (name, user_id, expression) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setLong(2, userId);
            stmt.setString(3, expression);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание функции не удалось");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Создана функция с ID: {}, название: {}", id, name);
                    return id;
                } else {
                    throw new SQLException("Создание функции не удалось, ID не получен");
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при создании функции: {}", name, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // CREATE - ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
    public Long createFunctionWithValidation(String name, Long userId, String expression) {
        // Проверяем существование пользователя
        UserDAO userDAO = new UserDAO();
        Map<String, Object> user = userDAO.findById(userId);
        if (user == null) {
            throw new RuntimeException("Пользователь с ID " + userId + " не существует");
        }

        // Проверяем уникальность имени функции для пользователя
        if (isFunctionNameExistsForUser(name, userId)) {
            throw new RuntimeException("Функция с именем '" + name + "' уже существует у пользователя");
        }

        return createFunction(name, userId, expression);
    }

    public int[] createFunctionsBatch(List<Object[]> functions) {
        String sql = "INSERT INTO functions (name, user_id, expression) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Object[] function : functions) {
                stmt.setString(1, (String) function[0]);
                stmt.setLong(2, (Long) function[1]);
                stmt.setString(3, (String) function[2]);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            logger.info("Создано {} функций в batch режиме", results.length);
            return results;
        } catch (SQLException e) {
            logger.error("Ошибка при batch создании функций", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // READ
    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM functions";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                functions.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} функций", functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при получении всех функций", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> getAllFunctionsLimited(int limit) {
        List<Map<String, Object>> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    functions.add(mapResultSetToMap(rs));
                }
            }
            logger.debug("Получено {} функций с ограничением LIMIT {}", functions.size(), limit);
        } catch (SQLException e) {
            logger.error("Ошибка при получении функций с ограничением: {}", e.getMessage());
        }
        return functions;
    }

    public Map<String, Object> findById(Long id) {
        String sql = "SELECT * FROM functions WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> function = mapResultSetToMap(rs);
                logger.debug("Найдена функция по ID {}: {}", id, function.get("name"));
                return function;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функции по ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByUserId(Long userId) {
        String sql = "SELECT * FROM functions WHERE user_id = ?";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} функций для пользователя с ID {}", functions.size(), userId);
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по ID пользователя: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByName(String name) {
        String sql = "SELECT * FROM functions WHERE name LIKE ?";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} функций с именем содержащим '{}'", functions.size(), name);
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по имени: {}", name, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByExpression(String expressionPattern) {
        String sql = "SELECT * FROM functions WHERE expression LIKE ?";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + expressionPattern + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} функций с выражением содержащим '{}'", functions.size(), expressionPattern);
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по выражению: {}", expressionPattern, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> getFunctionsWithUsername() {
        String sql = "SELECT f.*, u.username FROM functions f JOIN users u ON f.user_id = u.id";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> function = mapResultSetToMap(rs);
                function.put("username", rs.getString("username"));
                functions.add(function);
            }
            logger.debug("Найдено {} функций с именами пользователей", functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при получении функций с именами пользователей", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> getFunctionCountByUser() {
        String sql = "SELECT user_id, COUNT(*) as function_count FROM functions GROUP BY user_id";
        List<Map<String, Object>> counts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> count = new HashMap<>();
                count.put("user_id", rs.getLong("user_id"));
                count.put("function_count", rs.getInt("function_count"));
                counts.add(count);
            }
            return counts;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете функций по пользователям", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int getTotalFunctionsCount() {
        String sql = "SELECT COUNT(*) as total_functions FROM functions";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt("total_functions");
                logger.debug("Общее количество функций: {}", count);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете функций", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // READ - ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
    public List<Map<String, Object>> findFunctionsByUserIdAndName(Long userId, String namePattern) {
        String sql = "SELECT * FROM functions WHERE user_id = ? AND name LIKE ?";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, "%" + namePattern + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} функций пользователя {} с именем содержащим '{}'",
                    functions.size(), userId, namePattern);
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по пользователю и имени", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findRecentFunctions(int limit) {
        String sql = "SELECT * FROM functions ORDER BY created_at DESC LIMIT ?";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} последних функций", functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске последних функций", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Map<String, Object> findFunctionByNameAndUserId(String name, Long userId) {
        String sql = "SELECT * FROM functions WHERE name = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> function = mapResultSetToMap(rs);
                logger.debug("Найдена функция '{}' пользователя {}", name, userId);
                return function;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функции по имени и пользователю", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findFunctionsWithPointCount() {
        String sql = "SELECT f.*, COUNT(p.id) as point_count " +
                "FROM functions f LEFT JOIN points p ON f.id = p.function_id " +
                "GROUP BY f.id, f.name, f.user_id, f.expression, f.created_at";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> function = mapResultSetToMap(rs);
                function.put("point_count", rs.getInt("point_count"));
                functions.add(function);
            }
            logger.debug("Найдено {} функций с количеством точек", functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при получении функций с количеством точек", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE
    public boolean updateFunction(Long id, String name, Long userId, String expression) {
        String sql = "UPDATE functions SET name = ?, user_id = ?, expression = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setLong(2, userId);
            stmt.setString(3, expression);
            stmt.setLong(4, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлена функция с ID: {}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении функции с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean updateFunctionExpression(Long id, String expression) {
        String sql = "UPDATE functions SET expression = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, expression);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлено выражение функции с ID: {}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении выражения функции с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE - ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
    public boolean updateFunctionName(Long id, String newName) {
        String sql = "UPDATE functions SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлено название функции с ID: {} на '{}'", id, newName);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении названия функции с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean updateFunctionUser(Long id, Long newUserId) {
        // Проверяем существование нового пользователя
        UserDAO userDAO = new UserDAO();
        Map<String, Object> user = userDAO.findById(newUserId);
        if (user == null) {
            throw new RuntimeException("Пользователь с ID " + newUserId + " не существует");
        }

        String sql = "UPDATE functions SET user_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, newUserId);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлен пользователь функции с ID: {} на пользователя {}", id, newUserId);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пользователя функции с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - БЕЗОПАСНЫЕ МЕТОДЫ

    /**
     * Безопасное удаление функции с предварительным удалением связанных точек
     * @param id ID функции для удаления
     * @return true если удаление успешно, false если функция не найдена
     */
    public boolean deleteFunction(Long id) {
        try {
            // Сначала проверяем существование функции
            Map<String, Object> function = findById(id);
            if (function == null) {
                logger.warn("Функция с ID {} не найдена для удаления", id);
                return false;
            }

            // Получаем информацию о функции для логирования
            String functionName = (String) function.get("name");

            // 1. Сначала удаляем все связанные точки
            int pointsDeleted = pointDAO.deletePointsByFunctionId(id);
            logger.debug("Удалено {} точек функции '{}' (ID: {})", pointsDeleted, functionName, id);

            // 2. Затем удаляем саму функцию
            String sql = "DELETE FROM functions WHERE id = ?";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);
                int affectedRows = stmt.executeUpdate();
                boolean deleted = affectedRows > 0;

                if (deleted) {
                    logger.info("Удалена функция '{}' с ID: {} (предварительно удалено {} точек)",
                            functionName, id, pointsDeleted);
                } else {
                    logger.warn("Функция с ID {} не была удалена", id);
                }
                return deleted;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при удалении функции с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Безопасное удаление всех функций пользователя
     * @param userId ID пользователя
     * @return количество удаленных функций
     */
    public int deleteFunctionsByUserId(Long userId) {
        try {
            // Сначала находим все функции пользователя
            List<Map<String, Object>> userFunctions = findByUserId(userId);
            if (userFunctions.isEmpty()) {
                logger.debug("У пользователя с ID {} нет функций для удаления", userId);
                return 0;
            }

            int totalDeleted = 0;

            // Удаляем каждую функцию безопасным способом
            for (Map<String, Object> function : userFunctions) {
                Long functionId = (Long) function.get("id");
                if (deleteFunction(functionId)) {
                    totalDeleted++;
                }
            }

            logger.info("Удалено {} функций пользователя с ID: {}", totalDeleted, userId);
            return totalDeleted;

        } catch (Exception e) {
            logger.error("Ошибка при удалении функций пользователя с ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Удаление функций по префиксу имени пользователя
     * Используется для очистки тестовых данных
     * @param prefix префикс имени пользователя
     * @return количество удаленных функций
     */
    public int deleteFunctionsByUsernamePrefix(String prefix) {
        try {
            // Сначала находим все функции с данным префиксом
            List<Map<String, Object>> functionsToDelete = findFunctionsByUsernamePrefix(prefix);
            if (functionsToDelete.isEmpty()) {
                logger.debug("Нет функций для удаления с префиксом: {}", prefix);
                return 0;
            }

            int totalDeleted = 0;

            // Безопасно удаляем каждую функцию
            for (Map<String, Object> function : functionsToDelete) {
                Long functionId = (Long) function.get("id");
                if (deleteFunction(functionId)) {
                    totalDeleted++;
                }
            }

            logger.info("Удалено {} функций по префиксу пользователя: {}", totalDeleted, prefix);
            return totalDeleted;

        } catch (Exception e) {
            logger.error("Ошибка при удалении функций по префиксу пользователя: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Безопасное удаление функций по префиксу с проверкой
     * @param prefix префикс имени пользователя
     * @return количество удаленных функций, -1 в случае ошибки
     */
    public int safeDeleteFunctionsByUsernamePrefix(String prefix) {
        try {
            // Проверяем существование функций
            int countToDelete = getFunctionCountByUsernamePrefix(prefix);
            if (countToDelete == 0) {
                logger.debug("Нет функций для удаления с префиксом: {}", prefix);
                return 0;
            }

            logger.debug("Найдено {} функций для удаления с префиксом: {}", countToDelete, prefix);

            // Выполняем удаление
            int deletedCount = deleteFunctionsByUsernamePrefix(prefix);

            if (deletedCount == countToDelete) {
                logger.info("Успешно удалено {} функций по префиксу пользователя: {}", deletedCount, prefix);
            } else {
                logger.warn("Удалено {} из {} функций по префиксу пользователя: {}",
                        deletedCount, countToDelete, prefix);
            }

            return deletedCount;

        } catch (Exception e) {
            logger.error("Ошибка при безопасном удалении функций по префиксу: {}", prefix, e);
            return -1;
        }
    }

    /**
     * Удаление функций по префиксу теста (удобный алиас)
     * @param testPrefix префикс теста
     * @return количество удаленных функций
     */
    public int deleteFunctionsByTestPrefix(String testPrefix) {
        return deleteFunctionsByUsernamePrefix(testPrefix);
    }

    // DELETE - ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
    public int deleteFunctionsByNamePattern(String namePattern) {
        String sql = "DELETE FROM functions WHERE name LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + namePattern + "%");
            int affectedRows = stmt.executeUpdate();

            logger.info("Удалено {} функций с именем содержащим '{}'", affectedRows, namePattern);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении функций по шаблону имени: {}", namePattern, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int deleteFunctionsByExpressionPattern(String expressionPattern) {
        String sql = "DELETE FROM functions WHERE expression LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + expressionPattern + "%");
            int affectedRows = stmt.executeUpdate();

            logger.info("Удалено {} функций с выражением содержащим '{}'", affectedRows, expressionPattern);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении функций по шаблону выражения: {}", expressionPattern, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Полное удаление всех данных пользователя (точки + функции)
     * @param userId ID пользователя
     * @return Map с результатами удаления
     */
    public Map<String, Integer> deleteAllUserData(Long userId) {
        Map<String, Integer> results = new HashMap<>();

        try {
            // 1. Сначала удаляем все точки пользователя
            int pointsDeleted = pointDAO.deletePointsByUserId(userId);
            results.put("points_deleted", pointsDeleted);

            // 2. Затем удаляем все функции пользователя
            int functionsDeleted = deleteFunctionsByUserId(userId);
            results.put("functions_deleted", functionsDeleted);

            logger.info("Полное удаление данных пользователя ID {}: {} точек, {} функций",
                    userId, pointsDeleted, functionsDeleted);

            return results;

        } catch (Exception e) {
            logger.error("Ошибка при полном удалении данных пользователя с ID: {}", userId, e);
            results.put("error", -1);
            return results;
        }
    }

    /**
     * Полное удаление всех тестовых данных по префиксу
     * @param prefix префикс теста
     * @return Map с результатами удаления
     */
    public Map<String, Integer> deleteAllTestDataByPrefix(String prefix) {
        Map<String, Integer> results = new HashMap<>();

        try {
            // 1. Сначала удаляем все точки
            int pointsDeleted = pointDAO.deletePointsByUsernamePrefix(prefix);
            results.put("points_deleted", pointsDeleted);

            // 2. Затем удаляем все функции
            int functionsDeleted = deleteFunctionsByUsernamePrefix(prefix);
            results.put("functions_deleted", functionsDeleted);

            logger.info("Полная очистка тестовых данных по префиксу {}: {} точек, {} функций",
                    prefix, pointsDeleted, functionsDeleted);

            return results;

        } catch (Exception e) {
            logger.error("Ошибка при полной очистке тестовых данных по префиксу: {}", prefix, e);
            results.put("error", -1);
            return results;
        }
    }

    // ВАЛИДАЦИЯ И ПРОВЕРКИ
    public boolean isFunctionNameExistsForUser(String name, Long userId) {
        String sql = "SELECT COUNT(*) as count FROM functions WHERE name = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке существования имени функции", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean validateFunctionExpression(String expression) {
        // Базовая валидация выражения
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        // Проверяем наличие запрещенных символов (базовая защита от SQL-инъекций)
        String[] forbiddenPatterns = {";", "--", "/*", "*/", "xp_", "sp_", "exec", "union", "select", "insert", "delete", "update", "drop", "create"};
        for (String pattern : forbiddenPatterns) {
            if (expression.toLowerCase().contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    // СУЩЕСТВУЮЩИЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    /**
     * Получение всех функций по префиксу имени пользователя
     * Используется для отладки и проверки данных
     */
    public List<Map<String, Object>> findFunctionsByUsernamePrefix(String prefix) {
        String sql = "SELECT f.*, u.username FROM functions f " +
                "JOIN users u ON f.user_id = u.id " +
                "WHERE u.username LIKE ?";
        List<Map<String, Object>> functions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> function = mapResultSetToMap(rs);
                function.put("username", rs.getString("username"));
                functions.add(function);
            }
            logger.debug("Найдено {} функций с префиксом пользователя: {}", functions.size(), prefix);
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по префиксу пользователя: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Проверка существования функций по префиксу имени пользователя
     */
    public boolean existsFunctionsByUsernamePrefix(String prefix) {
        String sql = "SELECT COUNT(*) as count FROM functions f " +
                "JOIN users u ON f.user_id = u.id " +
                "WHERE u.username LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке существования функций по префиксу: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Получение количества функций по префиксу имени пользователя
     */
    public int getFunctionCountByUsernamePrefix(String prefix) {
        String sql = "SELECT COUNT(*) as count FROM functions f " +
                "JOIN users u ON f.user_id = u.id " +
                "WHERE u.username LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                logger.debug("Найдено {} функций с префиксом пользователя: {}", count, prefix);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете функций по префиксу: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Получение ID пользователя по ID функции
     * @param functionId ID функции
     * @return ID пользователя или null если функция не найдена
     */
    public Long getUserIdByFunctionId(Long functionId) {
        String sql = "SELECT user_id FROM functions WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("user_id");
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при получении user_id для функции {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> function = new HashMap<>();
        function.put("id", rs.getLong("id"));
        function.put("name", rs.getString("name"));
        function.put("user_id", rs.getLong("user_id"));
        function.put("expression", rs.getString("expression"));
        return function;
    }
}