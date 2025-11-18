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

    public Long createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

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

    public Long createUserWithEmail(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Создан пользователь с ID: {}, username: {}, email: {}", id, username, email);
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
            logger.debug("Пользователь с ID {} не найден", id);
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
            logger.debug("Пользователь с username {} не найден", username);
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

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) as count FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке существования пользователя: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) as total_users FROM users";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt("total_users");
                logger.debug("Общее количество пользователей: {}", count);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете пользователей", e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean updateUser(Long id, String username, String password) {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setLong(3, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлен пользователь с ID: {}, новый username: {}", id, username);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean updateUserEmail(Long id, String email) {
        String sql = "UPDATE users SET email = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info("Обновлен email пользователя с ID: {}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении email пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                logger.info("Удален пользователь с ID: {}", id);
            } else {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя с ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    public boolean deleteUserByUsername(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                logger.info("Удален пользователь с username: {}", username);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя с username: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> user = new HashMap<>();
        user.put("id", rs.getLong("id"));
        user.put("username", rs.getString("username"));
        user.put("password", rs.getString("password"));
        user.put("email", rs.getString("email"));
        user.put("created_at", rs.getTimestamp("created_at"));
        user.put("updated_at", rs.getTimestamp("updated_at"));
        return user;
    }
}