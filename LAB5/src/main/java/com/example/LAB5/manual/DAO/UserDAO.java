package com.example.LAB5.manual.DAO;

import com.example.LAB5.manual.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    // CREATE
    public Long createUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Создан пользователь с ID: {}, username: {}", id, username);
                    return id;
                } else {
                    throw new SQLException("Создание пользователя не удалось, ID не получен");
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при создании пользователя: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // READ
    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM users";
        List<Map<String, Object>> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} пользователей", users.size());
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка при получении всех пользователей", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Map<String, Object> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> user = mapResultSetToMap(rs);
                logger.debug("Найден пользователь по ID {}: {}", id, user.get("username"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public Map<String, Object> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> user = mapResultSetToMap(rs);
                logger.debug("Найден пользователь по username: {}", username);
                return user;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по username: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Map<String, Object>> findByUsernameLike(String usernamePattern) {
        String sql = "SELECT * FROM users WHERE username LIKE ?";
        List<Map<String, Object>> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + usernamePattern + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToMap(rs));
            }
            logger.debug("Найдено {} пользователей с username содержащим '{}'", users.size(), usernamePattern);
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей по username pattern: {}", usernamePattern, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) as total_users FROM users";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total_users");
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете пользователей", e);
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE
    public boolean updateUser(Long id, String username, String passwordHash) {
        String sql = "UPDATE users SET username = ?, password_hash = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setLong(3, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлен пользователь с ID: {}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE
    public boolean deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                logger.info("Удален пользователь с ID: {}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> user = new HashMap<>();
        user.put("id", rs.getLong("id"));
        user.put("username", rs.getString("username"));
        user.put("password_hash", rs.getString("password_hash"));
        user.put("created_at", rs.getTimestamp("created_at"));
        return user;
    }
}