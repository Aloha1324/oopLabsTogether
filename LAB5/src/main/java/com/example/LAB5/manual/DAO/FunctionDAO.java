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

    // DELETE
    public boolean deleteFunction(Long id) {
        String sql = "DELETE FROM functions WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                logger.info("Удалена функция с ID: {}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении функции с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean deleteFunctionsByUserId(Long userId) {
        String sql = "DELETE FROM functions WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                logger.info("Удалено {} функций пользователя с ID: {}", affectedRows, userId);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении функций пользователя с ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> function = new HashMap<>();
        function.put("id", rs.getLong("id"));
        function.put("name", rs.getString("name"));
        function.put("user_id", rs.getLong("user_id"));
        function.put("expression", rs.getString("expression"));
        function.put("created_at", rs.getTimestamp("created_at"));
        return function;
    }
}