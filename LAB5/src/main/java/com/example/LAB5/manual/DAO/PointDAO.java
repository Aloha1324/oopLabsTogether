package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PointDAO {
    private static final Logger logger = LoggerFactory.getLogger(PointDAO.class);

    // CREATE
    public Long createPoint(Long functionId, Double xValue, Double yValue) {
        // Сначала получаем user_id из функции
        Long userId = getUserIdByFunctionId(functionId);

        String sql = "INSERT INTO points (function_id, user_id, x_value, y_value) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, functionId);
            stmt.setLong(2, userId);
            stmt.setDouble(3, xValue);
            stmt.setDouble(4, yValue);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание точки не удалось");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Создана точка с ID: {} для функции {}", id, functionId);
                    return id;
                } else {
                    throw new SQLException("Создание точки не удалось, ID не получен");
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при создании точки для функции {}: x={}, y={}", functionId, xValue, yValue, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int createPointsBatch(List<Object[]> points) {
        String sql = "INSERT INTO points (function_id, user_id, x_value, y_value) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Object[] point : points) {
                Long functionId = (Long) point[0];
                Long userId = getUserIdByFunctionId(functionId);

                stmt.setLong(1, functionId);
                stmt.setLong(2, userId);
                stmt.setDouble(3, (Double) point[1]);
                stmt.setDouble(4, (Double) point[2]);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            int totalInserted = 0;
            for (int result : results) {
                if (result >= 0) totalInserted += result;
            }

            logger.info("Создано {} точек в batch режиме", totalInserted);
            return totalInserted;
        } catch (SQLException e) {
            logger.error("Ошибка при batch создании точек", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // Вспомогательный метод для получения user_id по function_id
    private Long getUserIdByFunctionId(Long functionId) {
        String sql = "SELECT user_id FROM functions WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("user_id");
            } else {
                throw new SQLException("Функция с ID " + functionId + " не найдена");
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении user_id для функции {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // READ
    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM points";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                points.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} точек", points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при получении всех точек", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Map<String, Object> findById(Long id) {
        String sql = "SELECT * FROM points WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> point = mapResultSetToMap(rs);
                logger.debug("Найдена точка по ID: {}", id);
                return point;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точки по ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByFunctionId(Long functionId) {
        String sql = "SELECT * FROM points WHERE function_id = ?";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} точек для функции {}", points.size(), functionId);
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по function_id: {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByFunctionIds(List<Long> functionIds) {
        if (functionIds == null || functionIds.isEmpty()) {
            logger.debug("Список functionIds пуст, возвращаем пустой список точек");
            return new ArrayList<>();
        }

        // Создаем строку с перечислением ID через запятую: "1,2,3,4"
        String ids = functionIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = "SELECT * FROM points WHERE function_id IN (" + ids + ")";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                points.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} точек для {} функций", points.size(), functionIds.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек для function_ids: {}", functionIds, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByFunctionIdAndXRange(Long functionId, Double minX, Double maxX) {
        String sql = "SELECT * FROM points WHERE function_id = ? AND x_value BETWEEN ? AND ?";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            stmt.setDouble(2, minX);
            stmt.setDouble(3, maxX);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} точек для функции {} в диапазоне x [{}, {}]",
                    points.size(), functionId, minX, maxX);
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по диапазону x", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByYValueGreaterThan(Double yValue) {
        String sql = "SELECT * FROM points WHERE y_value > ?";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, yValue);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} точек с y_value > {}", points.size(), yValue);
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по y_value > {}", yValue, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByXValue(Double xValue) {
        String sql = "SELECT * FROM points WHERE x_value = ?";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, xValue);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} точек с x_value = {}", points.size(), xValue);
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по x_value = {}", xValue, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> getPointsWithFunctionName() {
        String sql = "SELECT p.*, f.name as function_name FROM points p JOIN functions f ON p.function_id = f.id";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> point = mapResultSetToMap(rs);
                point.put("function_name", rs.getString("function_name"));
                points.add(point);
            }
            logger.debug("Найдено {} точек с именами функций", points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при получении точек с именами функций", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> getPointCountByFunction() {
        String sql = "SELECT function_id, COUNT(*) as point_count FROM points GROUP BY function_id";
        List<Map<String, Object>> counts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> count = new HashMap<>();
                count.put("function_id", rs.getLong("function_id"));
                count.put("point_count", rs.getInt("point_count"));
                counts.add(count);
            }
            return counts;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете точек по функциям", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Map<String, Object> getFunctionStatistics(Long functionId) {
        String sql = "SELECT MIN(x_value) as min_x, MAX(x_value) as max_x, AVG(y_value) as avg_y FROM points WHERE function_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("min_x", rs.getDouble("min_x"));
                stats.put("max_x", rs.getDouble("max_x"));
                stats.put("avg_y", rs.getDouble("avg_y"));
                return stats;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при получении статистики для функции {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> getPointsOrderedByX(Long functionId) {
        String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY x_value ASC";
        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} точек для функции {} (отсортировано по x)", points.size(), functionId);
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при получении точек отсортированных по x", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE
    public boolean updatePoint(Long id, Double xValue, Double yValue) {
        String sql = "UPDATE points SET x_value = ?, y_value = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, xValue);
            stmt.setDouble(2, yValue);
            stmt.setLong(3, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлена точка с ID: {}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении точки с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean updatePointYValue(Long id, Double yValue) {
        String sql = "UPDATE points SET y_value = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, yValue);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлено значение Y точки с ID: {}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении значения Y точки с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int updatePointsYValueByFunction(Long functionId, Double multiplier) {
        String sql = "UPDATE points SET y_value = y_value * ? WHERE function_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, multiplier);
            stmt.setLong(2, functionId);

            int affectedRows = stmt.executeUpdate();
            logger.info("Обновлено {} точек для функции {} (умножение на {})", affectedRows, functionId, multiplier);
            return affectedRows;
        } catch (SQLException e) {
            logger.error("Ошибка при массовом обновлении точек функции {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE
    public boolean deletePoint(Long id) {
        String sql = "DELETE FROM points WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                logger.info("Удалена точка с ID: {}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении точки с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int deletePointsByFunctionId(Long functionId) {
        String sql = "DELETE FROM points WHERE function_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            int affectedRows = stmt.executeUpdate();

            logger.info("Удалено {} точек функции {}", affectedRows, functionId);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек функции {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int deletePointsByFunctionIdAndXRange(Long functionId, Double maxX) {
        String sql = "DELETE FROM points WHERE function_id = ? AND x_value < ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            stmt.setDouble(2, maxX);
            int affectedRows = stmt.executeUpdate();

            logger.info("Удалено {} точек функции {} с x < {}", affectedRows, functionId, maxX);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек по диапазону x", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ УДАЛЕНИЯ ПО ПРЕФИКСУ

    /**
     * Удаление точек по префиксу имени пользователя
     * Используется для очистки тестовых данных
     * @param prefix префикс имени пользователя
     * @return количество удаленных точек
     */
    public int deletePointsByUsernamePrefix(String prefix) {
        String sql = "DELETE FROM points WHERE function_id IN " +
                "(SELECT id FROM functions WHERE user_id IN " +
                "(SELECT id FROM users WHERE username LIKE ?))";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            int affectedRows = stmt.executeUpdate();

            logger.info("Удалено {} точек по префиксу пользователя: {}", affectedRows, prefix);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек по префиксу пользователя: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Безопасное удаление точек по префиксу с проверкой существования
     * @param prefix префикс имени пользователя
     * @return количество удаленных точек, -1 в случае ошибки
     */
    public int safeDeletePointsByUsernamePrefix(String prefix) {
        String checkSql = "SELECT COUNT(*) as count FROM points WHERE function_id IN " +
                "(SELECT id FROM functions WHERE user_id IN " +
                "(SELECT id FROM users WHERE username LIKE ?))";
        String deleteSql = "DELETE FROM points WHERE function_id IN " +
                "(SELECT id FROM functions WHERE user_id IN " +
                "(SELECT id FROM users WHERE username LIKE ?))";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            // Проверяем количество точек для удаления
            checkStmt.setString(1, prefix + "%");
            ResultSet rs = checkStmt.executeQuery();

            int countToDelete = 0;
            if (rs.next()) {
                countToDelete = rs.getInt("count");
            }

            if (countToDelete == 0) {
                logger.debug("Нет точек для удаления с префиксом: {}", prefix);
                return 0;
            }

            // Выполняем удаление
            deleteStmt.setString(1, prefix + "%");
            int affectedRows = deleteStmt.executeUpdate();

            logger.info("Удалено {} точек по префиксу пользователя: {}", affectedRows, prefix);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Ошибка при безопасном удалении точек по префиксу: {}", prefix, e);
            return -1;
        }
    }

    /**
     * Удаление точек по префиксу теста (альтернативное название для удобства)
     * @param testPrefix префикс теста
     * @return количество удаленных точек
     */
    public int deletePointsByTestPrefix(String testPrefix) {
        return deletePointsByUsernamePrefix(testPrefix);
    }

    /**
     * Получение количества точек по префиксу пользователя
     * @param prefix префикс имени пользователя
     * @return количество точек
     */
    public int getPointsCountByUsernamePrefix(String prefix) {
        String sql = "SELECT COUNT(*) as count FROM points WHERE function_id IN " +
                "(SELECT id FROM functions WHERE user_id IN " +
                "(SELECT id FROM users WHERE username LIKE ?))";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                logger.debug("Найдено {} точек с префиксом пользователя: {}", count, prefix);
                return count;
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Ошибка при подсчете точек по префиксу: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Получение информации о точках по префиксу пользователя
     * @param prefix префикс имени пользователя
     * @return список точек с информацией
     */
    public List<Map<String, Object>> getPointsByUsernamePrefix(String prefix) {
        String sql = "SELECT p.*, f.name as function_name, u.username " +
                "FROM points p " +
                "JOIN functions f ON p.function_id = f.id " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE u.username LIKE ?";

        List<Map<String, Object>> points = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> point = mapResultSetToMap(rs);
                point.put("function_name", rs.getString("function_name"));
                point.put("username", rs.getString("username"));
                points.add(point);
            }

            logger.debug("Найдено {} точек с префиксом пользователя: {}", points.size(), prefix);
            return points;

        } catch (SQLException e) {
            logger.error("Ошибка при получении точек по префиксу: {}", prefix, e);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Полное удаление всех тестовых данных по префиксу (точки + функции + пользователи)
     * ВАЖНО: Этот метод должен использоваться только в тестах
     * @param prefix префикс теста
     * @return Map с результатами удаления
     */
    public Map<String, Integer> deleteAllTestDataByPrefix(String prefix) {
        Map<String, Integer> results = new HashMap<>();

        try {
            // 1. Удаляем точки
            int pointsDeleted = deletePointsByUsernamePrefix(prefix);
            results.put("points_deleted", pointsDeleted);

            // 2. Удаляем функции (этот метод должен быть в FunctionDAO)
            // int functionsDeleted = functionDAO.deleteFunctionsByUsernamePrefix(prefix);
            // results.put("functions_deleted", functionsDeleted);

            // 3. Удаляем пользователей (этот метод должен быть в UserDAO)
            // int usersDeleted = userDAO.deleteUsersByUsernamePrefix(prefix);
            // results.put("users_deleted", usersDeleted);

            logger.info("Полная очистка тестовых данных по префиксу {}: удалено {} точек",
                    prefix, pointsDeleted);

            return results;

        } catch (Exception e) {
            logger.error("Ошибка при полной очистке тестовых данных по префиксу: {}", prefix, e);
            results.put("error", -1);
            return results;
        }
    }

    // Вспомогательные методы

    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> point = new HashMap<>();
        point.put("id", rs.getLong("id"));
        point.put("function_id", rs.getLong("function_id"));
        point.put("user_id", rs.getLong("user_id"));
        point.put("x_value", rs.getDouble("x_value"));
        point.put("y_value", rs.getDouble("y_value"));
        point.put("created_at", rs.getTimestamp("created_at"));
        return point;
    }

    /**
     * Проверка существования точек по префиксу пользователя
     * @param prefix префикс имени пользователя
     * @return true если точки существуют, false если нет
     */
    public boolean hasPointsWithUsernamePrefix(String prefix) {
        return getPointsCountByUsernamePrefix(prefix) > 0;
    }

    /**
     * Удаление точек по ID пользователя
     * @param userId ID пользователя
     * @return количество удаленных точек
     */
    public int deletePointsByUserId(Long userId) {
        String sql = "DELETE FROM points WHERE function_id IN " +
                "(SELECT id FROM functions WHERE user_id = ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int affectedRows = stmt.executeUpdate();

            logger.info("Удалено {} точек пользователя с ID: {}", affectedRows, userId);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек пользователя с ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }
}